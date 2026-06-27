package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.github.alexmodguy.alexscaves.client.render.compat.EntityRenderCompat;
import com.github.alexmodguy.alexscaves.client.render.entity.CustomBookEntityRenderer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
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

    private void renderEntity(Entity entityIn, PoseStack matrixStack, MultiBufferSource bufferIn, int packedLight) {
        EntityRenderDispatcher manager = Minecraft.getInstance().getEntityRenderDispatcher();
        try {
            var renderer = manager.getRenderer(entityIn);
            if (renderer instanceof CustomBookEntityRenderer customBookEntityRenderer && sepia) {
                customBookEntityRenderer.setSepiaFlag(true);
            }
            matrixStack.mulPose(Axis.YP.rotationDegrees(180));
            matrixStack.mulPose(Axis.ZP.rotationDegrees(180));
            EntityRenderCompat.render(entityIn, 0.0D, 0.0D, 0.0D, 0.0F, GuiCompatPartialTick(), matrixStack, bufferIn, packedLight);
            if (renderer instanceof CustomBookEntityRenderer customBookEntityRenderer && sepia) {
                customBookEntityRenderer.setSepiaFlag(false);
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

    private static float GuiCompatPartialTick() {
        return Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
    }
}
