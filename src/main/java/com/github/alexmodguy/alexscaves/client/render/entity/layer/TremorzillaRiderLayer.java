package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.TremorzillaModel;
import com.github.alexmodguy.alexscaves.client.render.entity.TremorzillaRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.world.entity.Entity;

public class TremorzillaRiderLayer extends RenderLayer121X<TremorzillaEntity, TremorzillaModel> {

    public TremorzillaRiderLayer(TremorzillaRenderer render) {
        super(render);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, TremorzillaEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float bodyYaw = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * partialTicks;
        if (entity.isVehicle()) {
            float swimProgress = entity.getSwimAmount(partialTicks);
            float burnProgress = entity.getBeamProgress(partialTicks);
            for (Entity passenger : entity.getPassengers()) {
                if (passenger == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    continue;
                }
                poseStack.pushPose();
                getParentModel().translateToNeck(poseStack);
                poseStack.translate(0, 0.5F - burnProgress * 0.5F - swimProgress * 0.5F, 0.35F - burnProgress * 0.5F - swimProgress * 0.5F);
                poseStack.mulPose(Axis.XN.rotationDegrees(190F - burnProgress * 40));
                poseStack.mulPose(Axis.YN.rotationDegrees(360 - bodyYaw));
                AlexsCaves.PROXY.releaseRenderingEntity(passenger.getUUID());
                renderPassenger(passenger, 0, 0, 0, 0, partialTicks, poseStack, bufferIn, packedLightIn);
                AlexsCaves.PROXY.blockRenderingEntity(passenger.getUUID());
                poseStack.popPose();
            }

        }
    }

    public static <E extends Entity> void renderPassenger(E entityIn, double x, double y, double z, float yaw, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        net.minecraft.client.renderer.entity.EntityRenderer<? super E, ?> raw = manager.getRenderer(entityIn);
        // AC mobs use the legacy immediate bridge (draws straight into the captured buffer at the perch pose).
        if (raw instanceof EntityRenderer121X r121) {
            r121.render(entityIn, yaw, partialTicks, matrixStack, bufferIn, packedLight);
            return;
        }
        // Players / vanilla entities go through the real 26.1 extract-submit pipeline. matrixStack is already
        // at the perch, so submit with a zero offset; the dispatcher looks up the renderer + render offset.
        if (bufferIn instanceof com.github.alexmodguy.alexscaves.client.render.compat.SubmitNodeBufferSource cap && cap.cameraState() != null) {
            try {
                manager.submit(manager.extractEntity(entityIn, partialTicks), cap.cameraState(), 0.0D, 0.0D, 0.0D, matrixStack, cap.liveCollector());
            } catch (Throwable t) {
                // Better a missing perch than a crash mid-world-render.
            }
        }
    }

}
