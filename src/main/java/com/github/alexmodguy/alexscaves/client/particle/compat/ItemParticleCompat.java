package com.github.alexmodguy.alexscaves.client.particle.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * 26.1: BreakingItemParticle's constructor now takes a {@link TextureAtlasSprite} (it dropped the
 * 1.21.5-era ItemStackRenderState parameter). Resolve the item's particle sprite up front so the
 * legacy {@code super(level, x, y, z, ItemParticleCompat.createRenderState(stack))} rewrite still
 * type-checks against the new constructor.
 */
public class ItemParticleCompat {

    private ItemParticleCompat() {
    }

    public static TextureAtlasSprite createRenderState(ItemStack itemStack) {
        ItemStackRenderState renderState = new ItemStackRenderState();
        Minecraft.getInstance().getItemModelResolver().updateForTopItem(renderState, itemStack, ItemDisplayContext.GROUND, Minecraft.getInstance().level, null, 0);
        Material.Baked baked = renderState.pickParticleMaterial(RandomSource.create());
        return baked != null ? baked.sprite() : null;
    }
}
