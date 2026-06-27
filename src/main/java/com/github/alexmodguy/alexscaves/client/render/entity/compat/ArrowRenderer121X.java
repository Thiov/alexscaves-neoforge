package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;

public abstract class ArrowRenderer121X<T extends AbstractArrow> extends EntityRenderer121X<T> {

    protected ArrowRenderer121X(EntityRendererProvider.Context context) {
        super(context);
    }
}
