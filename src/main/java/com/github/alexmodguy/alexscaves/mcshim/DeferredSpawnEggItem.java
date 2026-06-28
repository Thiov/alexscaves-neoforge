package com.github.alexmodguy.alexscaves.mcshim;
import net.neoforged.neoforge.common.*;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class DeferredSpawnEggItem extends SpawnEggItem {
    private final Supplier<? extends EntityType<? extends Mob>> type;
    private final int primaryColor;
    private final int secondaryColor;

    public DeferredSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, int primaryColor, int secondaryColor, Properties properties) {
        // 26.1: SpawnEggItem no longer takes the EntityType in its constructor — it's resolved from the
        // DataComponents.ENTITY_DATA component, which Item.Properties.spawnEgg(type) stamps. Stamping it (the
        // cave_dweller-proven pattern) makes vanilla getType(stack)/byId/spawnsEntity + egg colour tinting work.
        // type.get() is safe here: this runs during ITEM registration, after ENTITY_TYPE registration.
        super(properties.spawnEgg(type.get()));
        this.type = type;
        this.primaryColor = primaryColor;
        this.secondaryColor = secondaryColor;
    }

    public EntityType<? extends Mob> getType(@Nullable Object context) {
        return type.get();
    }

    public int getPrimaryColor() {
        return primaryColor;
    }

    public int getSecondaryColor() {
        return secondaryColor;
    }


    public EntityType<?> getType(HolderLookup.Provider registries, ItemStack stack) {
        return type.get();
    }

    // 26.1: vanilla SpawnEggItem resolves the entity type from a data component (static getType),
    // which AC's eggs never set, so the default useOn/use spawn nothing. Spawn our known type directly.
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos clickedPos = context.getClickedPos();
        Direction face = context.getClickedFace();
        BlockState clickedState = level.getBlockState(clickedPos);
        BlockPos spawnPos = clickedState.getCollisionShape(level, clickedPos).isEmpty() ? clickedPos : clickedPos.relative(face);
        Entity spawned = type.get().spawn(serverLevel, stack, context.getPlayer(), spawnPos,
            EntitySpawnReason.SPAWN_ITEM_USE, true, !clickedPos.equals(spawnPos) && face == Direction.UP);
        if (spawned != null) {
            Player player = context.getPlayer();
            if (player == null || !player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResult.PASS;
        }
        BlockPos pos = hit.getBlockPos();
        if (!(level.getBlockState(pos).getBlock() instanceof LiquidBlock)) {
            return InteractionResult.PASS;
        }
        Entity spawned = type.get().spawn(serverLevel, stack, player, pos,
            EntitySpawnReason.SPAWN_ITEM_USE, false, false);
        if (spawned != null && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResult.SUCCESS;
    }
}
