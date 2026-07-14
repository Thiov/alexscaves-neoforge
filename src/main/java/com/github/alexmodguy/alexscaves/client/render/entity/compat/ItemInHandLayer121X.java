package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ItemInHandLayer121X<T extends LivingEntity, M extends BasicEntityModel<T> & ArmedModel>
        extends RenderLayer121X<T, M> {
    protected final ItemInHandRenderer itemInHandRenderer;

    public ItemInHandLayer121X(RenderLayerParent121X<T, M> parent, ItemInHandRenderer itemInHandRenderer) {
        super(parent);
        this.itemInHandRenderer = itemInHandRenderer;
    }


    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float limbSwing,
            float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        // Mirror vanilla ItemInHandLayer#render: draw the item held in each hand. The 26.1 render-state
        // bridge left this as a no-op on NeoForge, so held items (Licowitch sugar staff, Gingerbread man's
        // item, Deep One weapons, etc.) never rendered. Fabric already had this; NeoForge did not.
        this.renderArmWithItem(entity, entity.getMainHandItem(), ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                entity.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.RIGHT : HumanoidArm.LEFT, poseStack,
                bufferSource, packedLight);
        this.renderArmWithItem(entity, entity.getOffhandItem(), ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                entity.getMainArm() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT, poseStack,
                bufferSource, packedLight);
    }

    protected void renderArmWithItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext,
            HumanoidArm humanoidArm, PoseStack poseStack, MultiBufferSource multiBufferSource, int packedLight) {
        if (!itemStack.isEmpty()) {
            poseStack.pushPose();
            // ArmedModel#translateToHand in 26.1 takes a render state; the ported models ignore it and
            // delegate to their legacy (arm, poseStack) overload, so null is safe here.
            this.getParentModel().translateToHand(null, humanoidArm, poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
            boolean flag = humanoidArm == HumanoidArm.LEFT;
            poseStack.translate((flag ? -1 : 1) / 16.0F, 0.125F, -0.625F);
            ItemInHandCompat.renderItem(this.itemInHandRenderer, livingEntity, itemStack, displayContext, poseStack,
                    multiBufferSource, packedLight);
            poseStack.popPose();
        }
    }
}
