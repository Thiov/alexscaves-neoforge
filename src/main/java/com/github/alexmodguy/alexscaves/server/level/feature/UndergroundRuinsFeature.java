package com.github.alexmodguy.alexscaves.server.level.feature;

import com.github.alexmodguy.alexscaves.server.level.feature.config.UndergroundRuinsFeatureConfiguration;
import com.github.alexmodguy.alexscaves.server.misc.ACFluidHelper;
import com.google.common.collect.Lists;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.util.ArrayList;
import java.util.List;

public class UndergroundRuinsFeature extends Feature<UndergroundRuinsFeatureConfiguration> {

    public UndergroundRuinsFeature(Codec<UndergroundRuinsFeatureConfiguration> codec) {
        super(codec);
    }

    
    public boolean place(FeaturePlaceContext<UndergroundRuinsFeatureConfiguration> context) {
        RandomSource randomSource = context.random();
        WorldGenLevel level = context.level();
        BlockPos chunkCenter = context.origin().atY(level.getMinY() + 3);
        List<BlockPos> genPos = new ArrayList<>();
        int surface = level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, chunkCenter.getX(), chunkCenter.getZ()) - 5;
        int replaced = 0;
        while (chunkCenter.getY() < surface) {
            BlockPos next = chunkCenter.above();
            BlockState currentState = level.getBlockState(chunkCenter);
            BlockState nextState = level.getBlockState(next);
            if (!canReplace(currentState, replaced) && canReplace(nextState, replaced + 1)) {
                genPos.add(chunkCenter);
            }
            replaced++;
            chunkCenter = next;
        }
        if (genPos.isEmpty()) {
            return false;
        }
        BlockPos blockPos = genPos.size() <= 1 ? genPos.get(0) : genPos.get(randomSource.nextInt(genPos.size() - 1));
        if (!canGenerateAt(level, blockPos)) {
            return false;
        }
        Rotation rotation = Rotation.getRandom(randomSource);
        UndergroundRuinsFeatureConfiguration config = context.config();
        StructureTemplateManager structureTemplateManager = level.getLevel().getServer().getStructureManager();
        StructureTemplate structureTemplate = structureTemplateManager.getOrCreate(config.structures.get(randomSource.nextInt(config.structures.size())));
        ChunkPos chunkPos = net.minecraft.world.level.ChunkPos.containing(blockPos);
        BoundingBox boundingBox = new BoundingBox(chunkPos.getMinBlockX() - 16, level.getMinY(), chunkPos.getMinBlockZ() - 16, chunkPos.getMaxBlockX() + 16, level.getMaxY(), chunkPos.getMaxBlockZ() + 16);
        StructurePlaceSettings settings = modifyPlacementSettings(new StructurePlaceSettings().setRotation(rotation).setBoundingBox(boundingBox).setRandom(randomSource));
        Vec3i size = structureTemplate.getSize(rotation);
        BlockPos placementPos = blockPos.offset(-size.getX() / 2, 0, -size.getZ() / 2);
        int replaceDown = 0;
        while (skipsOver(level.getBlockState(placementPos), replaceDown) && placementPos.getY() < level.getMinY()) {
            placementPos = placementPos.below();
            replaceDown++;
        }
        placementPos = placementPos.below(calculateSinkBy(level, placementPos, structureTemplate, config.sinkBy));
        BlockPos transformedPlacement = structureTemplate.getZeroPositionWithTransform(placementPos, Mirror.NONE, rotation);
        if (structureTemplate.placeInWorld(level, transformedPlacement, transformedPlacement, settings, randomSource, 18)) {
            for (StructureTemplate.StructureBlockInfo blockInfo : StructureTemplate.processBlockInfos(level, transformedPlacement, transformedPlacement, settings, getDataMarkers(structureTemplate, transformedPlacement, rotation, false))) {
                String marker = blockInfo.nbt().getString("metadata").orElse("");
                if (marker.equals("loot_chest")) {
                    level.setBlock(blockInfo.pos(), Blocks.CAVE_AIR.defaultBlockState(), 3);
                    if (level.getBlockEntity(blockInfo.pos().below()) instanceof RandomizableContainerBlockEntity container) {
                        container.setLootTable(context.config().chestLoot, randomSource.nextLong());
                    }
                } else {
                    processMarker(marker, level, blockInfo.pos(), randomSource);
                }
            }
            processBoundingBox(level, structureTemplate.getBoundingBox(settings, transformedPlacement), randomSource);
        }
        return true;
    }

    protected int calculateSinkBy(WorldGenLevel level, BlockPos blockPos, StructureTemplate structureTemplate, int sinkByIn) {
        return sinkByIn;
    }

    public void processBoundingBox(WorldGenLevel level, BoundingBox boundingBox, RandomSource randomSource) {
    }

    public StructurePlaceSettings modifyPlacementSettings(StructurePlaceSettings structurePlaceSettings) {
        return structurePlaceSettings;
    }

    public void processMarker(String marker, WorldGenLevel level, BlockPos pos, RandomSource randomSource) {
    }

    protected boolean canGenerateAt(WorldGenLevel level, BlockPos blockPos) {
        return true;
    }

    protected boolean canReplace(BlockState state, int already) {
        return (state.isAir() || state.canBeReplaced()) && (!ACFluidHelper.isAcid(state.getFluidState()) || already < 3);
    }

    protected boolean skipsOver(BlockState state, int already) {
        return canReplace(state, already);
    }

    private static List<StructureTemplate.StructureBlockInfo> getDataMarkers(StructureTemplate structureTemplate, BlockPos blockPos, Rotation rotation, boolean keepJigsaws) {
        List<StructureTemplate.StructureBlockInfo> list = structureTemplate.filterBlocks(blockPos, new StructurePlaceSettings().setRotation(rotation), Blocks.STRUCTURE_BLOCK, keepJigsaws);
        List<StructureTemplate.StructureBlockInfo> dataMarkers = Lists.newArrayList();
        for (StructureTemplate.StructureBlockInfo blockInfo : list) {
            if (blockInfo.nbt() != null) {
                StructureMode structureMode = StructureMode.valueOf(blockInfo.nbt().getString("mode").orElse("DATA"));
                if (structureMode == StructureMode.DATA) {
                    dataMarkers.add(blockInfo);
                }
            }
        }
        return dataMarkers;
    }
}
