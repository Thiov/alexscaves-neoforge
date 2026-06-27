package com.github.alexmodguy.alexscaves.server.block;

import com.mojang.serialization.MapCodec;
import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.HologramProjectorBlockEntity;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class HologramProjectorBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
    public static final MapCodec<HologramProjectorBlock> CODEC = simpleCodec((properties) -> new HologramProjectorBlock());
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public HologramProjectorBlock() {
        super(Properties.of().mapColor(DyeColor.WHITE).strength(1.0F, 5.0F).sound(SoundType.METAL).lightLevel((i) -> 10));
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    
    public BlockState updateShape(BlockState state, net.minecraft.world.level.LevelReader levelAccessor, net.minecraft.world.level.ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState state1, net.minecraft.util.RandomSource randomSource) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(blockPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelAccessor));
        }
        return state.canSurvive(levelAccessor, blockPos) ? super.updateShape(state, levelAccessor, scheduledTickAccess, blockPos, direction, blockPos1, state1, randomSource) : Blocks.AIR.defaultBlockState();
    }

    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        ItemStack heldItem = player.getMainHandItem();
        if (worldIn.getBlockEntity(pos) instanceof HologramProjectorBlockEntity projectorBlockEntity && !player.isShiftKeyDown() && heldItem.is(ACItemRegistry.HOLOCODER.get())) {
            CompoundTag entityTag = null;
            EntityType entityType = null;
            boolean flag = false;
            // In 1.21, Holocoder stores entity data in CUSTOM_DATA with "BoundEntityTag" key
            CustomData customData = heldItem.get(DataComponents.CUSTOM_DATA);
            if (customData != null && !customData.isEmpty()) {
                CompoundTag tag = customData.copyTag();
                if (com.github.alexmodguy.alexscaves.server.misc.NbtCompat.contains(tag, "BoundEntityTag")) {
                    CompoundTag entity = com.github.alexmodguy.alexscaves.server.misc.NbtCompat.getCompound(tag, "BoundEntityTag");
                    Optional<EntityType<?>> optional = com.github.alexmodguy.alexscaves.server.entity.util.EntityCompat.entityTypeBy(entity);
                    if (optional.isPresent()) {
                        entityType = optional.get();
                        entityTag = entity;
                        flag = true;
                    }
                }
            }
            if (!flag) {
                entityType = EntityType.PLAYER;
                CompoundTag playerTag = new CompoundTag();
                com.github.alexmodguy.alexscaves.server.misc.NbtCompat.putUUID(playerTag, "UUID", player.getUUID());
                playerTag.putString("id", BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.PLAYER).toString());
                entityTag = playerTag;
            }
            projectorBlockEntity.setEntity(entityType, entityTag, player.getYHeadRot());
            worldIn.playSound((Player) null, pos, ACSoundRegistry.HOLOGRAM_STOP.get(), SoundSource.BLOCKS);
            if (!player.isCreative()) {
                heldItem.shrink(1);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }


    
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HologramProjectorBlockEntity(pos, state);
    }

    @javax.annotation.Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACBlockEntityRegistry.HOLOGRAM_PROJECTOR.get(), HologramProjectorBlockEntity::tick);
    }

    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> blockStateBuilder) {
        blockStateBuilder.add(WATERLOGGED);
    }
}
