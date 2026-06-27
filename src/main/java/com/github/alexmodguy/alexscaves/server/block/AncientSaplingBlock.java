package com.github.alexmodguy.alexscaves.server.block;

import com.github.alexmodguy.alexscaves.server.block.grower.AncientTreeGrower;
import com.github.alexmodguy.alexscaves.server.misc.RegistryCompat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class AncientSaplingBlock extends SaplingBlock {

    public AncientSaplingBlock(TreeGrower grower, Properties properties) {
        super(grower, properties);
    }

    
    public void advanceTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        if (state.getValue(STAGE) == 0) {
            level.setBlock(pos, state.cycle(STAGE), 4);
        } else if (!tryGrowThreeByThreeMegaTree(level, pos, state, random)) {
            this.treeGrower.growTree(level, level.getChunkSource().getGenerator(), pos, state, random);
        }
    }

    private boolean tryGrowThreeByThreeMegaTree(ServerLevel level, BlockPos pos, BlockState state, RandomSource random) {
        Holder<ConfiguredFeature<?, ?>> holder = level.registryAccess()
            .lookupOrThrow(Registries.CONFIGURED_FEATURE)
            .get(AncientTreeGrower.GIANT_ANCIENT_TREE.identifier())
            .orElse(null);
        if (holder == null) {
            return false;
        }

        ChunkGenerator chunkGenerator = level.getChunkSource().getGenerator();
        for (int xOffset = 0; xOffset >= -2; xOffset--) {
            for (int zOffset = 0; zOffset >= -2; zOffset--) {
                if (isThreeByThreeSapling(state, level, pos, xOffset, zOffset)) {
                    BlockPos originPos = pos.offset(xOffset, 0, zOffset);
                    ConfiguredFeature<?, ?> configuredFeature = holder.value();
                    BlockState airState = Blocks.AIR.defaultBlockState();
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            level.setBlock(originPos.offset(i, 0, j), airState, 4);
                        }
                    }
                    BlockPos treePos = originPos.offset(1, 0, 1);
                    if (configuredFeature.place(level, chunkGenerator, random, treePos)) {
                        return true;
                    }
                    for (int i = 0; i < 3; i++) {
                        for (int j = 0; j < 3; j++) {
                            level.setBlock(originPos.offset(i, 0, j), state, 4);
                        }
                    }
                    return false;
                }
            }
        }
        return false;
    }

    private static boolean isThreeByThreeSapling(BlockState state, BlockGetter level, BlockPos pos, int xOffset, int zOffset) {
        Block block = state.getBlock();
        BlockPos originPos = pos.offset(xOffset, 0, zOffset);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (!level.getBlockState(originPos.offset(i, 0, j)).is(block)) {
                    return false;
                }
            }
        }
        return true;
    }
}
