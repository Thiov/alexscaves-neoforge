package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.layered.*;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.server.item.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.EquipmentClientInfo;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import com.github.alexmodguy.alexscaves.mcshim.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ACArmorRenderProperties implements IClientItemExtensions {

    private static final Identifier DARKNESS_ARMOR_GLOW = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/darkness_armor_glow.png");
    private static boolean init;
    public static PrimordialArmorModel PRIMORDIAL_ARMOR_MODEL;
    public static HazmatArmorModel HAZMAT_ARMOR_MODEL;
    public static DivingArmorModel DIVING_ARMOR_MODEL;
    public static DarknessArmorModel DARKNESS_ARMOR_MODEL;
    public static RainbounceArmorModel RAINBOUNCE_ARMOR_MODEL;
    public static GingerbreadArmorModel GINGERBREAD_ARMOR_MODEL;


    public static void initializeModels() {
        init = true;
        PRIMORDIAL_ARMOR_MODEL = new PrimordialArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.PRIMORDIAL_ARMOR));
        HAZMAT_ARMOR_MODEL = new HazmatArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.HAZMAT_ARMOR));
        DIVING_ARMOR_MODEL = new DivingArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.DIVING_ARMOR));
        DARKNESS_ARMOR_MODEL = new DarknessArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.DARKNESS_ARMOR));
        RAINBOUNCE_ARMOR_MODEL = new RainbounceArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.RAINBOUNCE_ARMOR));
        GINGERBREAD_ARMOR_MODEL = new GingerbreadArmorModel(Minecraft.getInstance().getEntityModels().bakeLayer(ACModelLayers.GINGERBREAD_ARMOR));
    }


    /**
     * 26.1.2 real {@link IClientItemExtensions} armor-model hook. Unlike the deferred/unused state the
     * client-rendering spec assumed, {@code EquipmentLayerRenderer.renderLayers} in THIS build DOES call
     * {@code IClientItemExtensions.of(stack).getGenericArmorModel(stack, layerType, model)} (verified in the
     * merged 26.1.2 sources), whose default delegates here. So returning AC's custom 3D model here makes the
     * custom armor render — no mixin required.
     *
     * <p>The {@code layerType} only distinguishes HUMANOID vs HUMANOID_LEGGINGS, but AC's per-slot part
     * visibility (and the diving hat-overlay fix) needs the exact {@link EquipmentSlot}; we derive it from the
     * stack's {@link Equippable} component (each AC armor item is bound to one slot). Part visibility / texture
     * selection live in {@link ACCustomArmorModels}, which caches one instance per (armor, slot).
     */
    @Override
    public Model getHumanoidArmorModel(ItemStack itemStack, EquipmentClientInfo.LayerType layerType, Model original) {
        EquipmentSlot slot = slotOf(itemStack, layerType);
        if (slot == null) {
            return original;
        }
        HumanoidModel<?> model = ACCustomArmorModels.modelFor(itemStack.getItem(), slot);
        return model != null ? model : original;
    }

    /**
     * Resolve the worn {@link EquipmentSlot} for the stack. Prefer the stack's {@link Equippable} component
     * (authoritative); fall back to the layer type for the leggings vs the head/chest/feet distinction.
     */
    private static EquipmentSlot slotOf(ItemStack itemStack, EquipmentClientInfo.LayerType layerType) {
        Equippable equippable = itemStack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            return equippable.slot();
        }
        return layerType == EquipmentClientInfo.LayerType.HUMANOID_LEGGINGS ? EquipmentSlot.LEGS : null;
    }

    public static void renderCustomArmor(PoseStack poseStack, MultiBufferSource multiBufferSource, int light, ItemStack itemStack, ArmorItem armorItem, Model armorModel, boolean legs, Identifier texture) {
        // In 1.21, armorItem.getMaterial() returns Holder<ArmorMaterial>, so compare with .getHolder()
        if(armorItem.getMaterial() == ACItemRegistry.DARKNESS_ARMOR_MATERIAL.getHolder()){
            // 26.1: ItemRenderer.getArmorFoilBuffer is gone; the enchant glint overlay is dropped here.
            VertexConsumer vertexconsumer1 = multiBufferSource.getBuffer(net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(texture));
            armorModel.renderToBuffer(poseStack, vertexconsumer1, light, OverlayTexture.NO_OVERLAY, -1);
            VertexConsumer vertexconsumer2 = multiBufferSource.getBuffer(ACRenderTypes.getEyesAlphaEnabled(DARKNESS_ARMOR_GLOW));
            armorModel.renderToBuffer(poseStack, vertexconsumer2, 240, OverlayTexture.NO_OVERLAY, -1);
        }else if(armorItem.getMaterial() == ACItemRegistry.RAINBOUNCE_ARMOR_MATERIAL.getHolder()){
            VertexConsumer vertexconsumer1 = multiBufferSource.getBuffer(ACRenderTypes.getTeslaBulb(texture));
            armorModel.renderToBuffer(poseStack, vertexconsumer1, 240, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
