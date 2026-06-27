package com.github.alexmodguy.alexscaves.server.block;

import org.jetbrains.annotations.Nullable;

import com.github.alexmodguy.alexscaves.server.block.blockentity.AmbersolBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;

public class AmbersolBlock extends BaseEntityBlock {

    public AmbersolBlock() {
        super(Properties.of().mapColor(MapColor.COLOR_YELLOW).requiresCorrectToolForDrops().strength(3F, 10.0F).randomTicks().sound(ACSoundTypes.AMBER).lightLevel((i) -> 15).emissiveRendering((state, level, pos) -> true));
    }

    
    public MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    public static BlockPos fillWithLights(BlockPos current, LevelAccessor level) {
        current = current.below();
        while (current.getY() > level.getMinY() && AmbersolLightBlock.testSkylight(level, level.getBlockState(current), current)) {
            if (level.getBlockState(current).isAir()) {
                level.setBlock(current, ACBlockRegistry.AMBERSOL_LIGHT.get().defaultBlockState(), 3);
            }
            current = current.below();
        }
        return current;
    }

    
    protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState state1, RandomSource randomSource) {
        if (levelReader instanceof LevelAccessor levelAccessor) {
            fillWithLights(blockPos, levelAccessor);
        }
        return state;
    }

    
    protected void randomTick(BlockState state, ServerLevel serverLevel, BlockPos pos, RandomSource randomSource) {
        fillWithLights(pos, serverLevel);
    }

    
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        fillWithLights(pos, level);
    }

    
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity livingEntity, ItemStack stack) {
        fillWithLights(pos, level);
    }

    
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AmbersolBlockEntity(pos, state);
    }
}
