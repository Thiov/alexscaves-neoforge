package com.github.alexmodguy.alexscaves.client.model.baked;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 26.1.2: there is NO {@code net.neoforged.neoforge.client.model.BakedModelWrapper} and NO vanilla
 * {@code BakedModel} (the model pipeline moved to {@code BlockStateModel}/{@code ItemModel}; see the
 * port-internal {@link BakedModel} stub). The original 1.21.1 behaviour wrapped each baked model whose id
 * matched a fullbright list and forced its quads to max light. That model-map hook no longer exists in
 * {@link net.neoforged.neoforge.client.event.ModelEvent.ModifyBakingResult} (it now exposes
 * {@code getBakingResult().blockStateModels()}/{@code itemStackModels()}, keyed by BlockState/Identifier,
 * with no per-id BakedModel to wrap), so this wrapper is unreferenced on NeoForge.
 *
 * <p>It is kept as a plain delegate over the port-internal {@link BakedModel} stub so the class still
 * compiles; the emissive-block-models config option degrades to a no-op (see ClientProxy.bakeModels).
 *
 * <p>TODO 26.1.2: re-implement emissive/fullbright block models against the 26.1 {@code BlockStateModel}
 * pipeline (e.g. a custom {@code BlockStateModel}/quad-emitter) if the feature is wanted.
 */
public class BakedModelShadeLayerFullbright implements BakedModel {

    private final BakedModel originalModel;

    public BakedModelShadeLayerFullbright(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return originalModel.getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return originalModel.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return originalModel.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return originalModel.usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return originalModel.isCustomRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return originalModel.getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return originalModel.getTransforms();
    }
}
