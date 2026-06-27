package com.github.alexmodguy.alexscaves.client.render.blockentity;

import com.github.alexmodguy.alexscaves.client.model.SauropodBaseModel;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.blockentity.compat.BlockEntityRenderer121X;
import com.github.alexmodguy.alexscaves.client.render.compat.RenderTargetCompat;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexmodguy.alexscaves.server.block.blockentity.AmberMonolithBlockEntity;
import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class AmberMonolithBlockRenderer<T extends AmberMonolithBlockEntity> implements BlockEntityRenderer121X<T> {

    protected final RandomSource random = RandomSource.create();

    public AmberMonolithBlockRenderer(BlockEntityRendererProvider.Context rendererDispatcherIn) {
    }

    
    public void render(T amber, float partialTicks, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Entity currentEntity = amber.getDisplayEntity(Minecraft.getInstance().level);
        float age = amber.tickCount + partialTicks;
        float spin = amber.getRotation(partialTicks);
        poseStack.pushPose();
        poseStack.translate(0.5F, 1.65F, 0.5F);
        if (currentEntity != null) {
            float f = 0.45F;
            float f1 = Math.max(currentEntity.getBbWidth(), currentEntity.getBbHeight());
            if ((double) f1 > 1.0D) {
                f /= f1 * 1.5F;
            }
            poseStack.translate(0, f * 1.5F - 1.25F + (float) (Math.cos(age * 0.05) * 0.05F), 0);
            poseStack.scale(f, f, f);
            poseStack.mulPose(Axis.YP.rotationDegrees(spin));
            renderEntityInAmber(currentEntity, partialTicks, poseStack, bufferIn, 1.0F);
        }
        poseStack.popPose();
    }

    public static <E extends Entity> void renderEntityInAmber(E entityIn, float partialTicks, PoseStack matrixStack, MultiBufferSource bufferIn, float transparency) {
        Object render = null;
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            Object rawRenderer = manager.getRenderer(entityIn);
            render = rawRenderer;
            float xRot = entityIn.getXRot();
            float xRotOld = entityIn.xRotO;
            float yRot = entityIn.getYRot();
            float yRotOld = entityIn.yRotO;
            float yBodyRot = 0;
            float yBodyRotOld = 0;
            float headRot = 0;
            float headRotOld = 0;
            if (entityIn instanceof LivingEntity living) {
                headRot = living.yHeadRot;
                headRotOld = living.yHeadRotO;
                yBodyRot = living.yBodyRot;
                yBodyRotOld = living.yBodyRotO;
                living.yHeadRot = 0;
                living.yHeadRotO = 0;
                living.yBodyRot = 0;
                living.yBodyRotO = 0;
                entityIn.setXRot(0);
                entityIn.xRotO = 0;
                entityIn.setYRot(0);
                entityIn.yRotO = 0;
                if (render instanceof LivingEntityRenderer121X<?, ?> compatRenderer && compatRenderer.getModel() != null) {
                    renderCompatLivingModel(living, compatRenderer.getModel(), getTextureLocation(compatRenderer, living), matrixStack, bufferIn, partialTicks, transparency);
                }
                entityIn.setXRot(xRot);
                entityIn.xRotO = xRotOld;
                entityIn.setYRot(yRot);
                entityIn.yRotO = yRotOld;
                living.yHeadRot = headRot;
                living.yHeadRotO = headRotOld;
                living.yBodyRot = yBodyRot;
                living.yBodyRotO = yBodyRotOld;
            }
            RenderTargetCompat.bindWrite(Minecraft.getInstance().getMainRenderTarget());
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.forThrowable(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            entityIn.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", render);
            crashreportcategory1.setDetail("Delta", partialTicks);
            throw new ReportedException(crashreport);
        }
    }

    @SuppressWarnings("unchecked")
    private static void renderCompatLivingModel(LivingEntity living, BasicEntityModel<?> baseModel, net.minecraft.resources.Identifier texture,
                                                PoseStack matrixStack, MultiBufferSource bufferIn, float partialTicks, float transparency) {
        BasicEntityModel model = (BasicEntityModel) baseModel;
        VertexConsumer vertexConsumer = bufferIn.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture));
        matrixStack.pushPose();
        model.young = living.isBaby();
        model.riding = living.isPassenger();
        model.attackTime = living.getAttackAnim(partialTicks);
        setAmberFlags(model, true);
        model.prepareMobModel(living, 0.0F, 0.0F, partialTicks);
        model.setupAnim(living, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F);
        setAmberFlags(model, false);
        matrixStack.scale(living.getScale(), -living.getScale(), living.getScale());
        model.renderToBuffer(matrixStack, vertexConsumer, 240, OverlayTexture.NO_OVERLAY, ColorUtil.packColor(0.3F, 0.16F, 0.2F, transparency));
        matrixStack.popPose();
    }

    private static void setAmberFlags(Object model, boolean enabled) {
        if (model instanceof SauropodBaseModel<?> sauropodBaseModel) {
            sauropodBaseModel.straighten = enabled;
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static net.minecraft.resources.Identifier getTextureLocation(LivingEntityRenderer121X<?, ?> renderer, LivingEntity entity) {
        return ((LivingEntityRenderer121X) renderer).getTextureLocation(entity);
    }
}
