package com.github.alexmodguy.alexscaves.server.block;

import javax.annotation.Nullable;

import com.github.alexmodguy.alexscaves.server.block.blockentity.ACBlockEntityRegistry;
import com.github.alexmodguy.alexscaves.server.block.blockentity.VolcanicCoreBlockEntity;
import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.MapColor;

public class VolcanicCoreBlock extends BaseEntityBlock {
    public static final MapCodec<VolcanicCoreBlock> CODEC = simpleCodec((properties) -> new VolcanicCoreBlock());

    
    public MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public VolcanicCoreBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().lightLevel((state) -> 8).strength(55.0F, 1200.0F).isValidSpawn((state, getter, pos, entityType) -> entityType.fireImmune()).emissiveRendering((state, getter, pos) -> true).sound(ACSoundTypes.FLOOD_BASALT));
    }

    
    public void stepOn(Level level, BlockPos blockPos, BlockState blockState, Entity entity) {
        if (!entity.isSteppingCarefully() && entity instanceof LivingEntity livingEntity) {
            var frostWalker = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FROST_WALKER);
            if (EnchantmentHelper.getEnchantmentLevel(frostWalker, livingEntity) == 0) {
                entity.hurtOrSimulate(level.damageSources().hotFloor(), 1.0F);
                entity.igniteForSeconds(3);
            }
        }
        super.stepOn(level, blockPos, blockState, entity);
    }

    
    protected BlockState updateShape(BlockState state, LevelReader levelReader, ScheduledTickAccess scheduledTickAccess, BlockPos blockPos, Direction direction, BlockPos blockPos1, BlockState blockState, RandomSource randomSource) {
        if (levelReader instanceof LevelAccessor levelAccessor && !this.scanForLava(levelAccessor, blockPos)) {
            scheduledTickAccess.scheduleTick(blockPos, this, 60 + randomSource.nextInt(40));
        }
        return state;
    }

    
    protected void tick(BlockState state, ServerLevel serverLevel, BlockPos blockPos, RandomSource randomSource) {
        if (!this.scanForLava(serverLevel, blockPos)) {
            serverLevel.setBlock(blockPos, ACBlockRegistry.FLOOD_BASALT.get().defaultBlockState(), 2);
        }
    }

    protected boolean scanForLava(BlockGetter blockGetter, BlockPos blockPos) {
        int adjacent = 0;
        for (Direction direction : Direction.values()) {
            FluidState fluidState = blockGetter.getFluidState(blockPos.relative(direction));
            if (fluidState.is(FluidTags.LAVA)) {
                adjacent++;
            }
        }
        return adjacent > 3;
    }

    @Nullable
    
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!this.scanForLava(context.getLevel(), context.getClickedPos())) {
            context.getLevel().scheduleTick(context.getClickedPos(), this, 60 + context.getLevel().getRandom().nextInt(40));
        }
        return this.defaultBlockState();
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> entityType) {
        return level.isClientSide() ? null : createTickerHelper(entityType, ACBlockEntityRegistry.VOLCANIC_CORE.get(), VolcanicCoreBlockEntity::tick);
    }

    
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VolcanicCoreBlockEntity(pos, state);
    }
}
