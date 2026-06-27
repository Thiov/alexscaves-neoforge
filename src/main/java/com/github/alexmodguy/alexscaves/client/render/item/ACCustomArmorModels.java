package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.layered.ACModelLayers;
import com.github.alexmodguy.alexscaves.client.model.layered.DarknessArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.DivingArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.GingerbreadArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.HazmatArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.PrimordialArmorModel;
import com.github.alexmodguy.alexscaves.client.model.layered.RainbounceArmorModel;
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
 * Loader-neutral AC custom-armor model registry (extracted from the Fabric {@code ACFabricArmorRenderer}).
 * One dedicated model instance is cached per (armor-type, slot); part visibility is set once at creation
 * (setupAnim only mutates rotations, never visibility) so the cached instances stay correct under 26.1's
 * deferred submit pipeline.
 *
 * <p>HAT-OVERLAY FIX: {@link #setPartVisibility} deliberately leaves {@code model.hat} hidden for the HEAD
 * slot. AC helmets build their own head geometry, and every AC armor model already neutralizes the inherited
 * inflated vanilla "hat" box to empty geometry in its createArmorLayer (see DivingArmorModel/DarknessArmorModel);
 * keeping hat hidden here ensures the diving-helmet bronze visor / porthole renders instead of being covered.
 */
public final class ACCustomArmorModels {

    private static final Map<String, HumanoidModel<?>> SLOT_MODELS = new HashMap<>();

    private ACCustomArmorModels() {
    }

    @SuppressWarnings("rawtypes")
    public static HumanoidModel modelFor(Item item, EquipmentSlot slot) {
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

    public static String typeKey(Item item) {
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
                // model.hat stays hidden — see class javadoc (hat-overlay / diving-porthole fix).
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

    public static Identifier textureFor(Item item, EquipmentSlot slot) {
        // AC's custom 3D armor models use one skin (_0) for the whole model; the vanilla-style _1 leggings
        // layer doesn't map to the custom model's UVs (renders white), so use _0 for all slots.
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
