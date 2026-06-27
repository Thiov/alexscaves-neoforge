package com.github.alexmodguy.alexscaves.client.gui.book.widget;

import com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat;
import com.google.gson.annotations.Expose;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.math.Axis;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import com.github.alexmodguy.alexscaves.mcshim.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

public class ItemWidget extends BookWidget {

    @Expose
    private String item;
    @Expose
    private String nbt;
    @Expose
    private boolean sepia;

    @Expose(serialize = false, deserialize = false)
    private ItemStack actualItem = null;

    public ItemWidget(int displayPage, String item, String nbt, boolean sepia, int x, int y, float scale) {
        super(displayPage, Type.ITEM, x, y, scale);
        this.item = item;
        this.nbt = nbt;
        this.sepia = sepia;
    }

    public void render(PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, float partialTicks, boolean onFlippingPage) {
        if (actualItem == null && item != null) {
            actualItem = BuiltInRegistries.ITEM.getOptional(Identifier.parse(item))
                .map(ItemStack::new)
                .orElse(ItemStack.EMPTY);
            if (nbt != null && !nbt.isEmpty() && !actualItem.isEmpty()) {
                try {
                    CompoundTag parsedTag = TagParser.parseCompoundFully(nbt);
                    CustomData.update(DataComponents.CUSTOM_DATA, actualItem, existingTag -> existingTag.merge(parsedTag));
                } catch (CommandSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }

        float scale = 16.0F * getScale();
        poseStack.pushPose();
        poseStack.translate(getX(), getY(), 50);
        renderItem(actualItem, poseStack, bufferSource, sepia, scale);
        poseStack.popPose();
    }

    public static void renderItem(ItemStack itemStack, PoseStack poseStack, MultiBufferSource.BufferSource bufferSource, boolean sepia, float scale) {
        if (itemStack == null || itemStack.isEmpty()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        poseStack.pushPose();
        try {
            poseStack.scale(scale, scale, scale);
            if (!sepia) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180F));
                poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
            } else {
                poseStack.mulPose(Axis.ZN.rotationDegrees(180F));
                poseStack.scale(-1F, 1F, 1F);
            }
            ItemRenderCompat.drawItem(
                itemStack,
                ItemDisplayContext.GUI,
                240,
                OverlayTexture.NO_OVERLAY,
                poseStack,
                bufferSource,
                minecraft.level
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        poseStack.popPose();
    }

    public static void renderSepiaItem(PoseStack poseStack, BakedModel bakedModel, ItemStack itemStack, MultiBufferSource.BufferSource bufferSource) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemRenderCompat.drawItem(
            itemStack,
            ItemDisplayContext.GUI,
            240,
            OverlayTexture.NO_OVERLAY,
            poseStack,
            bufferSource,
            minecraft.level
        );
    }
}
