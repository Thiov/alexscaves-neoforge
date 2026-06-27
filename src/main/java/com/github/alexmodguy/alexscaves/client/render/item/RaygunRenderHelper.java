package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.github.alexthe666.citadel.client.shader.PostEffectRegistry;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class RaygunRenderHelper {

    private static final Identifier RAYGUN_RAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_ray.png");
    private static final Identifier RAYGUN_BLUE_RAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_blue_ray.png");
    // widthMul widens the beam billboard (a wider, fainter copy = a glow halo); alpha is the vertex alpha
    // (255 for the solid core, lower for the surrounding glow). Drawn as a targeted glow around the beam
    // since 26.1's screen-space post-process glow (the old IRRADIATED_SHADER) is unavailable here.
    private static void renderRay(PoseStack poseStack, MultiBufferSource bufferSource, Vec3 vec3, float offset, boolean blue, float widthMul, int alpha, boolean glow) {
        float f2 = -1.0F * (offset * 0.25F % 1.0F);
        poseStack.pushPose();
        float length = (float) (vec3.length());
        vec3 = vec3.normalize();
        float f5 = (float) Math.acos(vec3.y);
        float f6 = (float) Math.atan2(vec3.z, vec3.x);
        poseStack.mulPose(Axis.YP.rotationDegrees(((Mth.PI / 2F) - f6) * Mth.RAD_TO_DEG));
        poseStack.mulPose(Axis.XP.rotationDegrees(f5 * Mth.RAD_TO_DEG));
        poseStack.mulPose(Axis.YP.rotationDegrees(offset * 3.0F));
        float v = -1.0F + f2;
        float v1 = length * 1F + v;
        float endWidth = 1.3F * widthMul;
        float startMiddle = 0;
        Identifier tex = blue ? RAYGUN_BLUE_RAY : RAYGUN_RAY;
        VertexConsumer ivertexbuilder = bufferSource.getBuffer(glow ? ACRenderTypes.getRaygunRayGlow(tex) : ACRenderTypes.getRaygunRay(tex, false));
        PoseStack.Pose matrixstack$entry = poseStack.last();
        poseStack.pushPose();
        Matrix4f matrix4f = matrixstack$entry.pose();
        Matrix3f matrix3f = matrixstack$entry.normal();

        vertex(ivertexbuilder, matrix4f, matrix3f, startMiddle, 0.0F, 0, alpha, 0.5F, v);
        vertex(ivertexbuilder, matrix4f, matrix3f, -endWidth, length, 0, alpha, 0.0F, v1);
        vertex(ivertexbuilder, matrix4f, matrix3f, endWidth, length, 0, alpha, 1.0F, v1);

        vertex(ivertexbuilder, matrix4f, matrix3f, 0, 0.0F, startMiddle, alpha, 0.5F, v);
        vertex(ivertexbuilder, matrix4f, matrix3f, 0, length, endWidth, alpha, 1F, v1);
        vertex(ivertexbuilder, matrix4f, matrix3f, 0, length, -endWidth, alpha, 0F, v1);
        poseStack.popPose();
        poseStack.popPose();
    }

    private static void vertex(VertexConsumer consumer, Matrix4f pose, Matrix3f normal, float x, float y, float z, int alpha, float u, float v) {
        consumer.addVertex(pose, x, y, z).setColor(255, 255, 255, alpha).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(240).setNormal(0.0F, 1.0F, 0.0F);
    }

    public static void renderRaysFor(LivingEntity entity,  Vec3 rayFrom, PoseStack poseStack, MultiBufferSource bufferSource, float partialTick, boolean firstPerson, int firstPersonPass) {
        if (entity.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof RaygunItem && entity.isUsingItem()) {
            ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);
            float useRaygunAmount = Math.min(entity.getTicksUsingItem(partialTick), 5F) / 5F;
            float ageInTicks = entity.tickCount + partialTick;
            Vec3 rayPosition = RaygunItem.getLerpedRayPosition(stack, partialTick);
            boolean blue = false;
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                blue = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.GAMMA_RAY, stack) > 0;
            }
            if (rayPosition != null && entity.getTicksUsingItem() >= 5) {
                Vec3 gunPos = getGunOffset(entity, partialTick, firstPerson, entity.getMainArm() == HumanoidArm.LEFT);
                Vec3 vec3 = rayPosition.subtract(rayFrom.add(gunPos));
                poseStack.pushPose();
                poseStack.translate(gunPos.x, gunPos.y, gunPos.z);
                if (firstPersonPass == 0 || firstPersonPass == 1) {
                    if (AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get()) {
                        RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 3.2F, 70, true);
                        RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 1.9F, 120, true);
                    }
                    RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 1.0F, 255, false);
                }
                poseStack.popPose();
            }
        }
        if (entity.getItemInHand(InteractionHand.OFF_HAND).getItem() instanceof RaygunItem && entity.isUsingItem()) {
            ItemStack stack = entity.getItemInHand(InteractionHand.OFF_HAND);
            float useRaygunAmount = Math.min(entity.getTicksUsingItem(partialTick), 5F) / 5F;
            float ageInTicks = entity.tickCount + partialTick;
            Vec3 rayPosition = RaygunItem.getLerpedRayPosition(stack, partialTick);
            boolean blue = false;
            Level level = Minecraft.getInstance().level;
            if (level != null) {
                blue = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.GAMMA_RAY, stack) > 0;
            }
            if (rayPosition != null && entity.getTicksUsingItem() >= 5) {
                Vec3 gunPos = getGunOffset(entity, partialTick, firstPerson, entity.getMainArm() == HumanoidArm.RIGHT);
                Vec3 vec3 = rayPosition.subtract(rayFrom.add(gunPos));
                poseStack.pushPose();
                poseStack.translate(gunPos.x, gunPos.y, gunPos.z);
                if (firstPersonPass == 0 || firstPersonPass == 1) {
                    if (AlexsCaves.CLIENT_CONFIG.radiationGlowEffect.get()) {
                        RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 3.2F, 70, true);
                        RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 1.9F, 120, true);
                    }
                    RaygunRenderHelper.renderRay(poseStack, bufferSource, vec3, ageInTicks, blue, 1.0F, 255, false);
                }
                poseStack.popPose();
            }
        }
    }

    private static Vec3 getGunOffset(LivingEntity entity, float partialTicks, boolean firstPerson, boolean left) {
        int i = left ? -1 : 1;
        if(firstPerson){
            int fov = Minecraft.getInstance().getEntityRenderDispatcher().options.fov().get().intValue();
            double d7 = 1000.0D / (double) fov;
            // 26.1's Camera.getNearPlane takes the FOV (in degrees); the port had hardcoded 1.0F, which
            // collapses the near plane to a point so the gun offset pointed straight ahead and the beam
            // appeared to come from the screen centre. Pass the real FOV so the offset lands on the gun.
            Vec3 vec3 = Minecraft.getInstance().getEntityRenderDispatcher().camera.getNearPlane((float) fov).getPointOnPlane((float)i * 0.35F, -0.25F);
            float f = entity.getAttackAnim(partialTicks);
            float f1 = Mth.sin(Mth.sqrt(f) * (float) Math.PI);
            vec3 = vec3.scale(d7);
            vec3 = vec3.yRot(f1 * 0.5F);
            vec3 = vec3.xRot(-f1 * 0.7F);
            return vec3;
        }else{
            float yBodyRot = Mth.lerp(partialTicks, entity.yBodyRotO, entity.yBodyRot);
            Vec3 offset = new Vec3(entity.getBbWidth() * -0.5F * i, entity.getBbHeight() * 0.8F, 0).yRot((float) Math.toRadians(-yBodyRot));
            Vec3 armViewExtra = entity.getViewVector(partialTicks).normalize().scale(1.5F);
            return offset.add(armViewExtra);
        }
    }
}
