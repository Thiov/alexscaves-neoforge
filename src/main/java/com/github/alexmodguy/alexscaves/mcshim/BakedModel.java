package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.resources.model.*;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Port-internal stand-in for the removed vanilla BakedModel. 26.1 replaced baked models with
 * BlockStateModel; the mod's custom item/BER rendering is routed through the render compat helpers
 * instead, so this interface only needs to compile (ItemOverrides was removed with no replacement).
 */
public interface BakedModel {
    List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random);

    boolean useAmbientOcclusion();

    boolean isGui3d();

    boolean usesBlockLight();

    boolean isCustomRenderer();

    TextureAtlasSprite getParticleIcon();

    ItemTransforms getTransforms();
}
