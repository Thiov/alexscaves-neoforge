package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.layered.ACModelLayers;
import com.github.alexmodguy.alexscaves.client.model.layered.DarknessArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.DivingArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.GingerbreadArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.HazmatArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.PrimordialArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.RainbounceArmorModel;
import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.DarknessArmorItem;
import com.github.alexmodguy.alexscaves.server.item.DivingArmorItem;
import com.github.alexmodguy.alexscaves.server.item.GingerbreadArmorItem;
import com.github.alexmodguy.alexscaves.server.item.HazmatArmorItem;
import com.github.alexmodguy.alexscaves.server.item.PrimordialArmorItem;
import com.github.alexmodguy.alexscaves.server.item.RainbounceBootsItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.Map;

/**
 * 26.1.2 NOTE: this class is superseded by {@link ACCustomArmorModels} (the loader-neutral per-(armor,slot)
 * model/texture/part-visibility registry) and is no longer referenced. On NeoForge there is NO Fabric
 * {@code ArmorRenderer.register}; AC custom 3D armor is driven via {@code IClientItemExtensions
 * .getHumanoidArmorModel} (registered through {@code RegisterClientExtensionsEvent}; see
 * {@link ACArmorRenderProperties}), which {@code EquipmentLayerRenderer.renderLayers} invokes in 26.1.2.
 *
 * <p>It is kept only for reference and still documents the authoritative per-slot part-visibility behaviour:
 * AC's custom 3D models use 128x128 / 64x64 textures (NOT the vanilla 64x32 humanoid layout), and a single
 * shared model instance mutated per slot would render every piece with the last slot's visibility under the
 * deferred submit pipeline — hence one dedicated instance PER (armor, slot) with visibility set once at
 * creation. The HAT-OVERLAY fix (model.hat stays hidden for HEAD so the diving porthole shows) lives in
 * {@link #setPartVisibility}. The live implementation of all of this is in {@link ACCustomArmorModels}.
 *
 * <p>TODO 26.1.2: delete this file; {@link ACCustomArmorModels} is the single source of truth.
 */
public class ACFabricArmorRenderer {

    private static final Map<String, HumanoidModel<?>> SLOT_MODELS = new HashMap<>();

    @SuppressWarnings("rawtypes")
    private static HumanoidModel modelFor(Item item, EquipmentSlot slot) {
        String type = typeKey(item);
        if (type == null) {
            return null;
        }
        String key = type + "#" + slot.name();
        HumanoidModel<?> model = SLOT_MODELS.get(key);
        if (model == null) {
            model = build(type);
            if (model == null) {
                return null;
            }
            setPartVisibility(model, slot);
            SLOT_MODELS.put(key, model);
        }
        return (HumanoidModel) model;
    }

    private static String typeKey(Item item) {
        if (item instanceof PrimordialArmorItem) return "primordial";
        if (item instanceof HazmatArmorItem) return "hazmat";
        if (item instanceof DivingArmorItem) return "diving";
        if (item instanceof DarknessArmorItem) return "darkness";
        if (item instanceof RainbounceBootsItem) return "rainbounce";
        if (item instanceof GingerbreadArmorItem) return "gingerbread";
        return null;
    }

    private static HumanoidModel<?> build(String type) {
        EntityModelSet set = Minecraft.getInstance().getEntityModels();
        return switch (type) {
            case "primordial" -> new PrimordialArmorModel(set.bakeLayer(ACModelLayers.PRIMORDIAL_ARMOR));
            case "hazmat" -> new HazmatArmorModel(set.bakeLayer(ACModelLayers.HAZMAT_ARMOR));
            case "diving" -> new DivingArmorModel(set.bakeLayer(ACModelLayers.DIVING_ARMOR));
            case "darkness" -> new DarknessArmorModel(set.bakeLayer(ACModelLayers.DARKNESS_ARMOR));
            case "rainbounce" -> new RainbounceArmorModel(set.bakeLayer(ACModelLayers.RAINBOUNCE_ARMOR));
            case "gingerbread" -> new GingerbreadArmorModel(set.bakeLayer(ACModelLayers.GINGERBREAD_ARMOR));
            default -> null;
        };
    }

    private static void setPartVisibility(HumanoidModel<?> model, EquipmentSlot slot) {
        model.head.visible = false;
        model.hat.visible = false;
        model.body.visible = false;
        model.rightArm.visible = false;
        model.leftArm.visible = false;
        model.rightLeg.visible = false;
        model.leftLeg.visible = false;
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                // model.hat stays hidden (set false above). The inherited vanilla "hat" overlay that
                // createMesh auto-adds as a child of head is neutralized to empty geometry in every AC armor
                // model (see e.g. DivingArmorModel) because AC helmets build their own head geometry; leaving
                // that opaque overlay on is what hid the diving porthole. Do not re-enable it here.
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    private static Identifier textureFor(Item item, EquipmentSlot slot) {
        // AC's custom 3D armor models use one skin (_0) for the whole model; the vanilla-style _1
        // leggings layer doesn't map to the custom model's UVs (rendered white), so use _0 for all slots.
        String path;
        if (item instanceof PrimordialArmorItem) {
            path = "primordial_armor_0";
        } else if (item instanceof HazmatArmorItem) {
            path = "hazmat_suit_0";
        } else if (item instanceof DivingArmorItem) {
            path = "diving_suit_0";
        } else if (item instanceof GingerbreadArmorItem) {
            path = "gingerbread_armor_0";
        } else if (item instanceof DarknessArmorItem) {
            path = "darkness_armor";
        } else if (item instanceof RainbounceBootsItem) {
            path = "rainbounce_boots";
        } else {
            path = "darkness_armor";
        }
        return Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/armor/" + path + ".png");
    }
}
