package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.client.render.entity.LivingEntityRendererAccessor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LivingEntityRenderer121X.class)
public abstract class LivingEntityRendererMixin extends EntityRenderer121X implements LivingEntityRendererAccessor {

    @Shadow protected abstract void scale(LivingEntity living, PoseStack poseStack, float f);
    @Shadow protected abstract boolean addLayer(RenderLayer121X<?, ?> renderLayer);

    protected LivingEntityRendererMixin(EntityRendererProvider.Context context) {
        super(context);
    }

    public void scaleForHologram(LivingEntity entity, PoseStack poseStack, float partialTicks) {
        this.scale(entity, poseStack, partialTicks);
    }

    
    public void addACLayer(RenderLayer121X<?, ?> layer) {
        this.addLayer(layer);
    }
}
