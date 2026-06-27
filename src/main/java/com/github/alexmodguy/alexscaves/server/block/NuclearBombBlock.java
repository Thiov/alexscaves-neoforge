package com.github.alexmodguy.alexscaves.server.block;

import javax.annotation.Nullable;

import com.github.alexmodguy.alexscaves.server.entity.ACEntityRegistry;
import com.github.alexmodguy.alexscaves.server.entity.item.NuclearBombEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

public class NuclearBombBlock extends Block {

    public NuclearBombBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(8, 1001).sound(ACSoundTypes.NUCLEAR_BOMB));
    }

    public boolean onCaughtFire(BlockState state, Level level, BlockPos blockPos, @Nullable net.minecraft.core.Direction face, @Nullable LivingEntity igniter) {
        if (!level.isClientSide()) {
            NuclearBombEntity bomb = ACEntityRegistry.NUCLEAR_BOMB.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
            bomb.setPos((double) blockPos.getX() + 0.5D, (double) blockPos.getY(), (double) blockPos.getZ() + 0.5D);
            level.addFreshEntity(bomb);
            level.playSound((Player) null, bomb.getX(), bomb.getY(), bomb.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS, 1.0F, 1.0F);
            level.gameEvent(igniter, GameEvent.PRIME_FUSE, blockPos);
        }
        return true;
    }

    
    protected void onPlace(BlockState state, Level level, BlockPos blockPos, BlockState blockState, boolean b) {
        if (!blockState.is(state.getBlock()) && level.hasNeighborSignal(blockPos)) {
            onCaughtFire(state, level, blockPos, null, null);
            level.removeBlock(blockPos, false);
        }
    }

    
    protected void neighborChanged(BlockState state, Level level, BlockPos blockPos, Block block, @Nullable net.minecraft.world.level.redstone.Orientation orientation, boolean forced) {
        if (level.hasNeighborSignal(blockPos)) {
            onCaughtFire(state, level, blockPos, null, null);
            level.removeBlock(blockPos, false);
        }
    }

    
    protected void onProjectileHit(Level level, BlockState state, BlockHitResult blockHitResult, Projectile projectile) {
        if (level instanceof ServerLevel serverLevel) {
            BlockPos blockPos = blockHitResult.getBlockPos();
            Entity entity = projectile.getOwner();
            if (projectile.isOnFire() && projectile.mayInteract(serverLevel, blockPos)) {
                onCaughtFire(state, serverLevel, blockPos, null, entity instanceof LivingEntity livingEntity ? livingEntity : null);
                serverLevel.removeBlock(blockPos, false);
            }
        }
    }

    
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    
    protected InteractionResult useItemOn(ItemStack itemStack, BlockState state, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult result) {
        if (!itemStack.is(Items.FLINT_AND_STEEL) && !itemStack.is(Items.FIRE_CHARGE)) {
            return super.useItemOn(itemStack, state, level, blockPos, player, hand, result);
        }

        onCaughtFire(state, level, blockPos, result.getDirection(), player);
        level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 11);
        Item item = itemStack.getItem();
        if (!player.isCreative()) {
            if (itemStack.is(Items.FLINT_AND_STEEL)) {
                itemStack.hurtAndBreak(1, player, hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            } else {
                itemStack.shrink(1);
            }
        }

        player.awardStat(Stats.ITEM_USED.get(item));
        return EntityCompat.sidedSuccess(level);
    }

    
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }
}
