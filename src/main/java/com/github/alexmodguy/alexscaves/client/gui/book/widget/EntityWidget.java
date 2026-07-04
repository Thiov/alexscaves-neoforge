package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.github.alexmodguy.alexscaves.client.model.ACAdvancedEntityModel;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.compat.EntityRenderCompat;
import com.github.alexmodguy.alexscaves.client.render.entity.CustomBookEntityRenderer;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.LivingEntityRenderer121X;
import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;

public class EntityWidget extends BookWidget {

    @Expose
    @SerializedName("entity_id")
    private String entityId;
    @Expose
    private String nbt;
    @Expose
    private boolean sepia;
    @Expose
    @SerializedName("rot_x")
    private float rotX;
    @Expose
    @SerializedName("rot_y")
    private float rotY;
    @Expose
    @SerializedName("rot_z")
    private float rotZ;

    @Expose(serialize = false, deserialize = false)
    private Entity actualRenderEntity = null;

    public EntityWidget(int displayPage, String entityId, boolean sepia, String entityNBT, int x, int y, float scale) {
        this(displayPage, Type.ENTITY, entityId, sepia, entityNBT, x, y, scale);
    }

    public EntityWidget(int displayPage, Type type, String entityId, boolean sepia, String entityNBT, int x, int y, float scale) {
        super(displayPage, type, x, y, scale);
        this.entityId = entityId;
        this.sepia = sepia;
        this.nbt = entityNBT;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, boolean onFlippingPage) {
        if (actualRenderEntity == null) {
            EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.getValue(Identifier.parse(entityId));
            if (type != null && Minecraft.getInstance().level != null) {
                actualRenderEntity = type.create(Minecraft.getInstance().level, EntitySpawnReason.COMMAND);
                if (actualRenderEntity instanceof LivingEntity && nbt != null && !nbt.isEmpty()) {
                    try {
                        TagParser.parseCompoundFully(nbt);
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        if (actualRenderEntity == null) {
            return;
        }
        // IMPORTANT: leave the preview entity completely untouched (tickCount stays 0, old pos/rot at
        // defaults), exactly like upstream's never-ticked book entity. Dozens of AC models derive
        // "partialTicks = ageInTicks - entity.tickCount" in setupAnim; the static path passes ageInTicks=1,
        // so a synced tickCount turns that into a huge negative value and flings parts off-page (a synced
        // tickCount made the Gammaroach preview vanish via its death-animation offset).
        float entityScale = 100.0F * getScale();
        float entityBBSize = Math.max(actualRenderEntity.getBbWidth(), actualRenderEntity.getBbHeight());
        if ((double) entityBBSize > 1.0D) {
            entityScale /= entityBBSize * 1.5F;
        }
        poseStack.pushPose();
        poseStack.translate(getX(), getY(), 120);
        poseStack.scale(entityScale, entityScale, entityScale);
        poseStack.mulPose(Axis.XP.rotationDegrees(rotX));
        poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotZ));
        renderEntity(actualRenderEntity, poseStack, bufferSource, 240);
        poseStack.popPose();
    }

    protected boolean isSepia() {
        return sepia;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void renderEntity(Entity entityIn, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            var renderer = manager.getRenderer(entityIn);
            if (renderer instanceof CustomBookEntityRenderer customBookEntityRenderer) {
                // Upstream's CustomBookEntityRenderer branch verbatim: full dispatcher render, flipped into
                // GUI space (the dispatcher internally applies scale(-1,-1,1), so the 180° flips are needed
                // HERE and only here).
                if (sepia) {
                    customBookEntityRenderer.setSepiaFlag(true);
                }
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                // partialTick = 0 (NOT the live delta): upstream passed a fixed 0.0F here. A live partial-tick
                // makes ageInTicks = tickCount + partialTick sawtooth 0->1 every frame on the never-ticked
                // preview entity, jittering the idle head-bob/limb animations these renderers still run.
                EntityRenderCompat.render(entityIn, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, matrixStack, bufferIn, packedLight);
                if (sepia) {
                    customBookEntityRenderer.setSepiaFlag(false);
                }
            } else if (entityIn instanceof LivingEntity living && renderer instanceof LivingEntityRenderer121X livingRenderer) {
                // Regular mobs: upstream's direct static-model render, verbatim — and critically with NO
                // 180° flips. Vanilla/Citadel models are authored +Y-down, which already matches the page's
                // GUI space; the flips only exist to undo the dispatcher's internal scale(-1,-1,1). Applying
                // them here mirrored every direct-rendered mob upside down (Grottoceratops) and pushed
                // ground-anchored models upward out of the frame (Notor/Trilocaris invisible). The static
                // pose (ageInTicks=1, no head yaw) also means zero frame-to-frame jitter, like the original.
                BasicEntityModel model = livingRenderer.getModel();
                if (model instanceof ACAdvancedEntityModel acm) {
                    acm.young = living.isBaby();
                }
                VertexConsumer vertexConsumer = bufferIn.getBuffer(
                        ACRenderTypes.getBookWidget(livingRenderer.getTextureLocation(living), sepia));
                matrixStack.pushPose();
                model.setupAnim(living, 0.0F, 0.0F, 1.0F, 0.0F, 0.0F);
                matrixStack.scale(living.getScale(), living.getScale(), living.getScale());
                model.renderToBuffer(matrixStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, -1);
                matrixStack.popPose();
            } else {
                // Non-living / non-legacy renderers: dispatcher submit path (needs the same flips as above).
                matrixStack.mulPose(Axis.YP.rotationDegrees(180));
                matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
                // partialTick = 0 (NOT the live delta): upstream passed a fixed 0.0F here. A live partial-tick
                // makes ageInTicks = tickCount + partialTick sawtooth 0->1 every frame on the never-ticked
                // preview entity, jittering the idle head-bob/limb animations these renderers still run.
                EntityRenderCompat.render(entityIn, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F, matrixStack, bufferIn, packedLight);
            }
        } catch (Throwable throwable3) {
            CrashReport crashreport = CrashReport.forThrowable(throwable3, "Rendering entity in world");
            CrashReportCategory crashreportcategory = crashreport.addCategory("Entity being rendered");
            entityIn.fillCrashReportCategory(crashreportcategory);
            CrashReportCategory crashreportcategory1 = crashreport.addCategory("Renderer details");
            crashreportcategory1.setDetail("Assigned renderer", manager.getRenderer(entityIn));
            throw new ReportedException(crashreport);
        }
    }
}
