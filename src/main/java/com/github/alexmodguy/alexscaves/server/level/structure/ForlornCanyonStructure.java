package com.github.alexmodguy.alexscaves.server.level.structure;

import com.github.alexmodguy.alexscaves.server.level.biome.ACBiomeRegistry;
import com.github.alexmodguy.alexscaves.server.level.structure.piece.ForlornCanyonStructurePiece;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;

public class ForlornCanyonStructure extends AbstractCaveGenerationStructure {

    private static final int BOWL_WIDTH_RADIUS = 100;
    private static final int BOWL_HEIGHT_RADIUS = 60;
    public static final int BOWL_Y_CENTER = -10;

    public static final MapCodec<ForlornCanyonStructure> CODEC = simpleCodec((settings) -> new ForlornCanyonStructure(settings));

    public ForlornCanyonStructure(StructureSettings settings) {
        super(settings, ACBiomeRegistry.FORLORN_HOLLOWS);
    }

    
    protected StructurePiece createPiece(BlockPos offset, BlockPos center, int heightBlocks, int widthBlocks, RandomState randomState) {
        return new ForlornCanyonStructurePiece(offset, center, heightBlocks, widthBlocks);
    }

    
    public int getGenerateYHeight(WorldgenRandom random, int x, int y) {
        return BOWL_Y_CENTER;
    }

    
    public int getWidthRadius(WorldgenRandom random) {
        return 100;
    }

    
    public int getHeightRadius(WorldgenRandom random, int seaLevel) {
        return 90;
    }

    
    public StructureType<?> type() {
        return ACStructureRegistry.FORLORN_CANYON.get();
    }
}
