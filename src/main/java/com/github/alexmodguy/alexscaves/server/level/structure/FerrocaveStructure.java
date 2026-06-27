package com.github.alexmodguy.alexscaves.server.level.structure;

import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.FerrocaveStructurePiece;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class FerrocaveStructure extends AbstractCaveGenerationStructure {

    public static final MapCodec<FerrocaveStructure> CODEC = simpleCodec((settings) -> new FerrocaveStructure(settings));

    public FerrocaveStructure(StructureSettings settings) {
        super(settings, ACBiomeRegistry.MAGNETIC_CAVES);
    }

    
    protected StructurePiece createPiece(BlockPos offset, BlockPos center, int heightBlocks, int widthBlocks, RandomState randomState) {
        return new FerrocaveStructurePiece(offset, center, heightBlocks, widthBlocks);
    }

    
    public int getGenerateYHeight(WorldgenRandom random, int x, int y) {
        return random.nextInt(40) - 20;
    }

    
    public int getWidthRadius(WorldgenRandom random) {
        return 45 + random.nextInt(25);
    }

    
    public int getHeightRadius(WorldgenRandom random, int seaLevel) {
        return 80 + random.nextInt(25);
    }

    
    public StructureType<?> type() {
        return ACStructureRegistry.FERROCAVE.get();
    }
}
