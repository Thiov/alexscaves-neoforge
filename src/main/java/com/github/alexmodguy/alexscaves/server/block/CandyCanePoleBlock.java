package com.github.alexmodguy.alexscaves.server.block;

import java.util.function.Function;

import com.github.alexmodguy.alexscaves.server.misc.ACItemCompat;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CrossCollisionBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

public class CandyCanePoleBlock extends CrossCollisionBlock {

    public CandyCanePoleBlock() {
        super(2.0F, 2.0F, 16.0F, 16.0F, 16.0F, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).noOcclusion().noCollision().pushReaction(PushReaction.DESTROY).instabreak().sound(ACSoundTypes.HARD_CANDY).dynamicShape());
        this.registerDefaultState(this.defaultBlockState().setValue(WATERLOGGED, false).setValue(NORTH, false).setValue(EAST, false).setValue(SOUTH, false).setValue(WEST, false));
    }

    
    public MapCodec<? extends CrossCollisionBlock> codec() {
        return MapCodec.unit(this);
    }

    
    protected Function<BlockState, VoxelShape> makeShapes(float poleWidth, float connectorWidth, float poleHeight, float connectorYMin, float connectorYMax) {
        float f = 8.0F - poleWidth;
        float f1 = 8.0F + poleWidth;
        float f2 = 8.0F - connectorWidth;
        float f3 = 8.0F + connectorWidth;
        float upper = connectorYMax - 4.0F;
        VoxelShape pole = Block.box(f, 0.0D, f, f1, poleHeight, f1);
        VoxelShape north = Block.box(f2, upper, 0.0D, f3, connectorYMax, f3);
        VoxelShape south = Block.box(f2, upper, f2, f3, connectorYMax, 16.0D);
        VoxelShape west = Block.box(0.0D, upper, f2, f3, connectorYMax, f3);
        VoxelShape east = Block.box(f2, upper, f2, 16.0D, connectorYMax, f3);
        VoxelShape eastWest = Shapes.or(north, east);
        VoxelShape northSouth = Shapes.or(south, west);
        VoxelShape[] shapes = new VoxelShape[]{Shapes.empty(), south, west, northSouth, north, Shapes.or(south, north), Shapes.or(west, north), Shapes.or(northSouth, north), east, Shapes.or(south, east), Shapes.or(west, east), Shapes.or(northSouth, east), eastWest, Shapes.or(south, eastWest), Shapes.or(west, eastWest), Shapes.or(northSouth, eastWest)};
        for (int i = 0; i < shapes.length; ++i) {
            shapes[i] = Shapes.or(pole, shapes[i]);
        }
        return state -> shapes[getShapeIndex(state)];
    }

    private static int getShapeIndex(BlockState state) {
        int index = 0;
        if (state.getValue(NORTH)) {
            index |= 1;
        }
        if (state.getValue(EAST)) {
            index |= 2;
        }
        if (state.getValue(SOUTH)) {
            index |= 4;
        }
        if (state.getValue(WEST)) {
            index |= 8;
        }
        return index;
    }

    public boolean connectsTo(BlockGetter level, BlockPos ourPos, BlockPos posInQuestion) {
        BlockState stateAbove = level.getBlockState(ourPos.above());
        BlockState theirState = level.getBlockState(posInQuestion);
        BlockState stateAboveThem = level.getBlockState(posInQuestion.above());
        return theirState.getBlock() instanceof CandyCanePoleBlock && !(stateAbove.getBlock() instanceof CandyCanePoleBlock) && !(stateAboveThem.getBlock() instanceof CandyCanePoleBlock);
    }

    
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockGetter blockGetter = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        return super.getStateForPlacement(context)
            .setValue(NORTH, this.connectsTo(blockGetter, blockPos, blockPos.north()))
            .setValue(EAST, this.connectsTo(blockGetter, blockPos, blockPos.east()))
            .setValue(SOUTH, this.connectsTo(blockGetter, blockPos, blockPos.south()))
            .setValue(WEST, this.connectsTo(blockGetter, blockPos, blockPos.west()))
            .setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
    }

    
    protected BlockState updateShape(BlockState blockState, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos ourPos, Direction direction, BlockPos updatePos, BlockState blockState1, RandomSource randomSource) {
        if (blockState.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(ourPos, Fluids.WATER, Fluids.WATER.getTickDelay(levelReader));
        }
        return direction.getAxis().getPlane() == Direction.Plane.HORIZONTAL ? blockState.setValue(PROPERTY_BY_DIRECTION.get(direction), this.connectsTo(levelReader, ourPos, updatePos)) : blockState;
    }

    
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
    }

    public static BooleanProperty getPropertyByDirection(Direction direction) {
        return PROPERTY_BY_DIRECTION.get(direction);
    }

    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction, boolean simulate) {
        ItemStack itemStack = context.getItemInHand();
        if (!ACItemCompat.canPerformAction(itemStack, toolAction)) {
            return null;
        }
        if (ItemAbilities.AXE_STRIP == toolAction && this == ACBlockRegistry.CANDY_CANE_POLE.get()) {
            return ACBlockRegistry.STRIPPED_CANDY_CANE_POLE.get().defaultBlockState().setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(NORTH, state.getValue(NORTH)).setValue(EAST, state.getValue(EAST)).setValue(WEST, state.getValue(WEST)).setValue(SOUTH, state.getValue(SOUTH));
        }
        return null;
    }
}
