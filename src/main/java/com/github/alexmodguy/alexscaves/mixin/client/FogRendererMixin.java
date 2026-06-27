package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.misc.ACFluidHelper;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import com.github.alexmodguy.alexscaves.server.potion.DeepsightEffect;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.client.renderer.fog.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FogType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 26.1: the fog pipeline was rewritten. FogRenderer moved to net.minecraft.client.renderer.fog and
 * no longer exposes static fogRed/Green/Blue fields or setupColor/setupFog with the old shapes.
 * Color is now written into a Vector4f by computeFogColor(...) and distances are returned in a
 * FogData by setupFog(...). This retargets Alex's Caves' fog overrides onto those two methods.
 */
@Mixin(FogRenderer.class)
public abstract class FogRendererMixin {

    @Inject(method = "computeFogColor", at = @At("TAIL"))
    private void ac_computeFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistanceChunks,
                                    float bossColorModifier, Vector4f color, CallbackInfo ci) {
        Entity cameraEntity = camera.entity();
        if (cameraEntity == null) {
            return;
        }
        BlockState blockState = level.getBlockState(camera.blockPosition());
        if (blockState.is(ACBlockRegistry.PRIMAL_MAGMA.get()) || blockState.is(ACBlockRegistry.FISSURE_PRIMAL_MAGMA.get())) {
            color.set(1.0F, 0.4F, 0.0F, color.w);
            return;
        }
        if (cameraEntity.isEyeInFluid(ACFluidHelper.ACID)) {
            color.set(0.0F, 1.0F, 0.0F, color.w);
            return;
        }
        if (cameraEntity.isEyeInFluid(ACFluidHelper.PURPLE_SODA)) {
            color.set(0.6F, 0.1F, 0.85F, color.w);
            return;
        }
        if (camera.getFluidInCamera() == FogType.NONE && AlexsCaves.CLIENT_CONFIG.biomeSkyFogOverrides.get()) {
            float override = ClientProxy.acSkyOverrideAmount;
            float setR = color.x;
            float setG = color.y;
            float setB = color.z;
            boolean changed = false;
            if (override > 0.0F) {
                Vec3 sampledFog = ClientProxy.getLastSampledFogColor();
                setR += (float) ((sampledFog.x - setR) * override);
                setG += (float) ((sampledFog.y - setG) * override);
                setB += (float) ((sampledFog.z - setB) * override);
                changed = true;
            }
            float primordialBossAmount = AlexsCaves.PROXY.getPrimordialBossActiveAmount(partialTick);
            if (primordialBossAmount > 0.0F) {
                setR += (0.8F - setR) * primordialBossAmount;
                setG += (0.2F - setG) * primordialBossAmount;
                setB += (0.15F - setB) * primordialBossAmount;
                changed = true;
            }
            if (changed) {
                color.set(setR, setG, setB, color.w);
            }
            return;
        }
        if (camera.getFluidInCamera() == FogType.WATER && AlexsCaves.CLIENT_CONFIG.biomeWaterFogOverrides.get()) {
            float override = ClientProxy.acSkyOverrideAmount;
            if (override > 0.0F) {
                Vec3 sampledWaterFog = ClientProxy.getLastSampledWaterFogColor();
                color.set(
                    color.x + (float) ((sampledWaterFog.x - color.x) * override),
                    color.y + (float) ((sampledWaterFog.y - color.y) * override),
                    color.z + (float) ((sampledWaterFog.z - color.z) * override),
                    color.w
                );
            }
        }
    }

    @Inject(method = "setupFog", at = @At("RETURN"))
    private void ac_setupFog(Camera camera, int renderDistance, DeltaTracker deltaTracker, float bossColorModifier,
                             ClientLevel clientLevel, CallbackInfoReturnable<FogData> cir) {
        FogData fogData = cir.getReturnValue();
        if (fogData == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Entity cameraEntity = minecraft.getCameraEntity();
        if (cameraEntity == null || minecraft.level == null) {
            return;
        }
        float partialTick = deltaTracker.getGameTimeDeltaPartialTick(false);
        float defaultFarPlaneDistance = fogData.renderDistanceEnd;
        float defaultNearPlaneDistance = fogData.renderDistanceStart;
        BlockState blockState = minecraft.level.getBlockState(camera.blockPosition());
        if (cameraEntity.isEyeInFluid(ACFluidHelper.ACID)) {
            float farness = 10.0F;
            if (minecraft.player != null && minecraft.player.hasEffect(ACEffectRegistry.DEEPSIGHT)) {
                farness *= 1.0F + 1.5F * DeepsightEffect.getIntensity(minecraft.player, partialTick);
            }
            fogData.environmentalStart = 0.0F;
            fogData.environmentalEnd = farness;
            fogData.renderDistanceStart = 0.0F;
            fogData.renderDistanceEnd = farness;
            return;
        }
        if (cameraEntity.isEyeInFluid(ACFluidHelper.PURPLE_SODA)) {
            float farness = 20.0F;
            float nearness = -8.0F;
            if (minecraft.player != null && minecraft.player.hasEffect(ACEffectRegistry.DEEPSIGHT)) {
                float deepsight = DeepsightEffect.getIntensity(minecraft.player, partialTick);
                farness *= 1.0F + 1.5F * deepsight;
                nearness *= 1.0F - deepsight;
            }
            fogData.environmentalStart = nearness;
            fogData.environmentalEnd = farness;
            fogData.renderDistanceStart = nearness;
            fogData.renderDistanceEnd = farness;
            return;
        }
        if (blockState.is(ACBlockRegistry.PRIMAL_MAGMA.get()) || blockState.is(ACBlockRegistry.FISSURE_PRIMAL_MAGMA.get())) {
            float farness = 2.0F;
            if (minecraft.player != null && minecraft.player.hasEffect(ACEffectRegistry.DEEPSIGHT)) {
                farness *= 1.0F + 1.5F * DeepsightEffect.getIntensity(minecraft.player, partialTick);
            }
            fogData.environmentalStart = 0.0F;
            fogData.environmentalEnd = farness;
            fogData.renderDistanceStart = 0.0F;
            fogData.renderDistanceEnd = farness;
            return;
        }
        if (camera.getFluidInCamera() == FogType.WATER && AlexsCaves.CLIENT_CONFIG.biomeWaterFogOverrides.get()) {
            float farness = ClientProxy.getLastSampledWaterFogFarness();
            if (minecraft.player != null && minecraft.player.hasEffect(ACEffectRegistry.DEEPSIGHT)) {
                farness *= 1.0F + 1.5F * DeepsightEffect.getIntensity(minecraft.player, partialTick);
            }
            if (Math.abs(farness - 1.0F) > 0.01F) {
                fogData.environmentalEnd = defaultFarPlaneDistance * farness;
                fogData.renderDistanceEnd = defaultFarPlaneDistance * farness;
            }
            return;
        }
        if (camera.getFluidInCamera() == FogType.NONE && AlexsCaves.CLIENT_CONFIG.biomeSkyFogOverrides.get()) {
            float nearness = ClientProxy.getLastSampledFogNearness();
            float primordialBossAmount = AlexsCaves.PROXY.getPrimordialBossActiveAmount(partialTick);
            if (primordialBossAmount > 0.0F) {
                nearness *= 1.0F - primordialBossAmount * 0.75F;
            }
            if (Math.abs(nearness - 1.0F) > 0.01F || primordialBossAmount > 0.0F) {
                fogData.environmentalStart = defaultNearPlaneDistance * nearness;
                fogData.renderDistanceStart = defaultNearPlaneDistance * nearness;
            }
        }
    }
}
