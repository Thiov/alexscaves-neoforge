package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashSet;
import java.util.Set;

/**
 * Defensive guard: 26.1.2's {@code EntityRenderDispatcher} hard-NPEs if an entity reaches the render
 * pipeline with no registered renderer ({@code getRenderer(...)} returns null) — e.g. a transient entity
 * created before/around a renderer-reload, or an item-pickup extraction. That crashes the whole client
 * during world rendering. Instead, skip such entities (they render invisibly) and log the type once.
 */
@Mixin(EntityRenderDispatcher.class)
public abstract class EntityRenderDispatcherMixin {

    @Shadow
    public abstract <T extends Entity> EntityRenderer<? super T, ?> getRenderer(T entity);

    private static final Set<String> AC_LOGGED_MISSING = new HashSet<>();

    private static void ac_logOnce(Entity entity) {
        if (AC_LOGGED_MISSING.add(String.valueOf(entity.getType()))) {
            AlexsCaves.LOGGER.warn("[alexscaves] No entity renderer for {} ({}); rendering it invisibly to avoid a client crash.",
                    entity.getType(), entity.getClass().getName());
        }
    }

    @Inject(
            method = "shouldRender(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/culling/Frustum;DDD)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void ac_shouldRenderNullGuard(E entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (getRenderer(entity) == null) {
            ac_logOnce(entity);
            cir.setReturnValue(false);
        }
    }

    @Inject(
            method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;",
            at = @At("HEAD"),
            cancellable = true
    )
    private <E extends Entity> void ac_extractEntityNullGuard(E entity, float partialTick, CallbackInfoReturnable<EntityRenderState> cir) {
        if (getRenderer(entity) == null) {
            ac_logOnce(entity);
            cir.setReturnValue(new EntityRenderState());
        }
    }
}
