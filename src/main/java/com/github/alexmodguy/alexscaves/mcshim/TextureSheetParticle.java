package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.particle.*;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public abstract class TextureSheetParticle extends SingleQuadParticle {

    protected TextureSheetParticle(ClientLevel level, double x, double y, double z) {
        super(level, x, y, z, (TextureAtlasSprite) null);
    }

    protected TextureSheetParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd) {
        super(level, x, y, z, xd, yd, zd, (TextureAtlasSprite) null);
    }

    public void pickSprite(SpriteSet spriteSet) {
        this.setSprite(spriteSet.get(this.random));
    }

    public int getLightColor(float partialTick) {
        return super.getLightCoords(partialTick);
    }

    
    protected int getLightCoords(float partialTick) {
        // route the engine's light query to the overridable getLightColor (AC particles override that);
        // self-calling getLightCoords here was infinite recursion → "Rendering Particle" StackOverflow.
        return this.getLightColor(partialTick);
    }

    
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }
}
