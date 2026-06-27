package com.github.alexmodguy.alexscaves.server.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class AncientLeavesBlock extends LeavesBlock {
    public static final MapCodec<AncientLeavesBlock> CODEC = simpleCodec(AncientLeavesBlock::new);

    public AncientLeavesBlock(BlockBehaviour.Properties properties) {
        super(0.0F, properties);
    }

    
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    
    protected void spawnFallingLeavesParticle(Level level, BlockPos pos, RandomSource randomSource) {
    }
}
