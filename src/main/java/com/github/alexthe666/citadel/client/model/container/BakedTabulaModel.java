package com.github.alexthe666.citadel.client.model.container;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.math.Transformation;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import com.github.alexmodguy.alexscaves.mcshim.ItemOverrides;
import net.minecraft.client.resources.model.cuboid.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public class BakedTabulaModel implements BakedModel {
    private final ImmutableList<BakedQuad> quads;
    private final TextureAtlasSprite particle;
    private final ImmutableMap<ItemDisplayContext, Transformation> transforms;

    public BakedTabulaModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle, ImmutableMap<ItemDisplayContext, Transformation> transforms) {
        this.quads = quads;
        this.particle = particle;
        this.transforms = transforms;
    }

    
    public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_) {
        return this.quads;
    }

    
    public boolean useAmbientOcclusion() {
        return true;
    }

    
    public boolean isGui3d() {
        return false;
    }

    
    public boolean usesBlockLight() {
        return false;
    }

    
    public boolean isCustomRenderer() {
        return false;
    }

    
    public TextureAtlasSprite getParticleIcon() {
        return this.particle;
    }

    
    public ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    
    public ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
