package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.client.model.SubterranodonModel;
import com.github.alexmodguy.alexscaves.client.render.entity.SubterranodonRenderer;
import com.github.alexmodguy.alexscaves.server.entity.living.SubterranodonEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayer121X;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/**
 * 26.1 DEGRADATION: EntityRenderDispatcher's immediate-mode render(entity, x,y,z, yaw, poseStack,
 * buffer, light) was removed in favor of the extract/submit render-state pipeline, which cannot be
 * driven from inside a captured layer pass without a CameraRenderState. The passenger render is
 * therefore a no-op: riders are not drawn perched on a flying Subterranodon (they still render at
 * their own world position via the normal entity pass). All positioning math is preserved so this
 * can be restored once a submit-pipeline bridge for nested entities exists.
 */
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
        // No-op: immediate-mode nested entity rendering was removed in 26.1 (see class doc).
    }
}
