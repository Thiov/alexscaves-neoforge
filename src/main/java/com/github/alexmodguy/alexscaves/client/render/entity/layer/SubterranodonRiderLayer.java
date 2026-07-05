package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.client.model.SubterranodonModel;
import com.github.alexmodguy.alexscaves.client.render.entity.SubterranodonRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.SubterranodonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public class SubterranodonRiderLayer extends RenderLayer121X<SubterranodonEntity, SubterranodonModel> {

    public SubterranodonRiderLayer(SubterranodonRenderer render) {
        super(render);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, SubterranodonEntity entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        float bodyYaw = entity.yBodyRotO + (entity.yBodyRot - entity.yBodyRotO) * partialTicks;
        if (entity.isVehicle()) {
            float flight = entity.getFlyProgress(partialTicks) - entity.getHoverProgress(partialTicks);
            float flightRoll = flight * entity.getFlightRoll(partialTicks);
            Vec3 offset = new Vec3(0, 0.25F, 0.5F);
            Vec3 centerLegPos = getParentModel().getLegPosition(true, offset).add(getParentModel().getLegPosition(false, offset)).scale(0.5F);
            for (Entity passenger : entity.getPassengers()) {
                if (passenger == Minecraft.getInstance().player && Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                    continue;
                }
                poseStack.pushPose();
                poseStack.translate(centerLegPos.x, centerLegPos.y + passenger.getBbHeight() - 1.25F * flight, centerLegPos.z + 2 * flight);
                poseStack.mulPose(Axis.XP.rotationDegrees(70F * flight));
                poseStack.mulPose(Axis.XN.rotationDegrees(180F));
                poseStack.mulPose(Axis.YN.rotationDegrees(360 - bodyYaw + flightRoll));
                renderPassenger(passenger, 0, 0, 0, 0, partialTicks, poseStack, bufferIn, packedLightIn);
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
        // Players / vanilla entities go through the real 26.1 extract→submit pipeline. matrixStack is already
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
