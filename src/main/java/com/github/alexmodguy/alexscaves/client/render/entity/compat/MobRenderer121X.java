package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.Mob;

public abstract class MobRenderer121X<T extends Mob, M extends BasicEntityModel<T>> extends LivingEntityRenderer121X<T, M> {

    protected MobRenderer121X(EntityRendererProvider.Context context, M model, float shadowRadius) {
        super(context, model, shadowRadius);
    }
}
