package com.github.alexmodguy.alexscaves.server.block;

import org.jetbrains.annotations.Nullable;

import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.EnigmaticEngineBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class EnigmaticEngineBlock extends BaseEntityBlock {

    public EnigmaticEngineBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_ORANGE).requiresCorrectToolForDrops().strength(6F, 12.0F).sound(SoundType.COPPER));
    }

    
    public MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return createTickerHelper(entityType, ACBlockEntityRegistry.ENIGMATIC_ENGINE.get(), EnigmaticEngineBlockEntity::tick);
    }

    @Nullable
    
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnigmaticEngineBlockEntity(pos, state);
    }

    public boolean attemptAssembly(LevelAccessor levelAccessor, BlockPos blockPos) {
        if (levelAccessor.getBlockEntity(blockPos) instanceof EnigmaticEngineBlockEntity blockEntity) {
            return blockEntity.attemptAssembly();
        }
        return false;
    }

    
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState state, @Nullable LivingEntity living, ItemStack itemStack) {
        attemptAssembly(level, blockPos);
    }

    
    protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState state1, RandomSource randomSource) {
        if (levelReader instanceof LevelAccessor levelAccessor && attemptAssembly(levelAccessor, blockPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }
}
