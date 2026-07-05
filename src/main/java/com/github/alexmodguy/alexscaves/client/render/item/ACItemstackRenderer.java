package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.gui.book.widget.ItemWidget;
import com.github.alexmodguy.alexscaves.client.model.*;
import com.github.alexmodguy.alexscaves.client.render.ACRenderTypes;
import com.github.alexmodguy.alexscaves.client.render.ColorUtil;
import com.github.alexmodguy.alexscaves.client.render.misc.CaveMapRenderHelper;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentHelper;
import com.github.alexmodguy.alexscaves.server.enchantment.ACEnchantmentRegistry;
import com.github.alexmodguy.alexscaves.server.item.*;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import com.github.alexmodguy.alexscaves.mixin.client.ItemInHandRendererAccessor;
import net.minecraft.client.multiplayer.ClientLevel;
import com.github.alexmodguy.alexscaves.mcshim.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class ACItemstackRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Identifier GALENA_GAUNTLET_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/galena_gauntlet.png");
    private static final Identifier GALENA_GAUNTLET_RED_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/galena_gauntlet_red.png");
    private static final Identifier GALENA_GAUNTLET_BLUE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/galena_gauntlet_blue.png");
    private static final GalenaGauntletModel GALENA_GAUNTLET_RIGHT_MODEL = new GalenaGauntletModel(false);
    private static final GalenaGauntletModel GALENA_GAUNTLET_LEFT_MODEL = new GalenaGauntletModel(true);
    private static final Identifier RESISTOR_SHIELD_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/resistor_shield.png");
    private static final Identifier RESISTOR_SHIELD_RED_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/resistor_shield_red.png");
    private static final Identifier RESISTOR_SHIELD_BLUE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/resistor_shield_blue.png");
    private static final ResistorShieldModel RESISTOR_SHIELD_MODEL = new ResistorShieldModel();
    private static final Identifier PRIMITIVE_CLUB_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/primitive_club.png");
    private static final PrimitiveClubModel PRIMITIVE_CLUB_MODEL = new PrimitiveClubModel();
    private static final Identifier LIMESTONE_SPEAR_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/limestone_spear.png");
    private static final LimestoneSpearModel LIMESTONE_SPEAR_MODEL = new LimestoneSpearModel();
    private static final Identifier EXTINCTION_SPEAR_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/extinction_spear.png");
    private static final ExtinctionSpearModel EXTINCTION_SPEAR_MODEL = new ExtinctionSpearModel();
    private static final Identifier SIREN_LIGHT_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/siren_light.png");
    private static final Identifier SIREN_LIGHT_COLOR_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/siren_light_color.png");
    private static final SirenLightModel SIREN_LIGHT_MODEL = new SirenLightModel();
    private static final Identifier RAYGUN_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun.png");
    private static final Identifier RAYGUN_ACTIVE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_active.png");
    private static final Identifier RAYGUN_BLUE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_blue.png");
    private static final Identifier RAYGUN_BLUE_ACTIVE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/raygun/raygun_blue_active.png");
    private static final RaygunModel RAYGUN_MODEL = new RaygunModel();
    private static final Identifier SEA_STAFF_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/sea_staff.png");
    private static final SeaStaffModel SEA_STAFF_MODEL = new SeaStaffModel();
    private static final Identifier ORTHOLANCE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/deep_one/ortholance.png");
    private static final OrtholanceModel ORTHOLANCE_MODEL = new OrtholanceModel();
    private static final Identifier COPPER_VALVE_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/copper_valve.png");
    private static final CopperValveModel COPPER_VALVE_MODEL = new CopperValveModel();
    private static final BeholderModel BEHOLDER_MODEL = new BeholderModel();
    private static final Identifier BEHOLDER_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/beholder.png");
    private static final Identifier BEHOLDER_TEXTURE_EYE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/beholder_eye.png");
    private static final Identifier DREADBOW_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/dreadbow.png");
    private static final Identifier DREADBOW_TEXTURE_EYE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/dreadbow_eye.png");
    private static final Identifier DREADBOW_TEXTURE_EYE_PERFECT = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/dreadbow_eye_perfect.png");
    private static final DreadbowModel DREADBOW_MODEL = new DreadbowModel();
    private static final GobthumperModel GOBTHUMPER_MODEL = new GobthumperModel();
    private static final Identifier GOBTHUMPER_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gobthumper.png");
    private static final Identifier GOBTHUMPER_JELLY_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/gobthumper_jelly.png");
    private static final Identifier SHOT_GUM_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/shot_gum.png");
    private static final Identifier SHOT_GUM_GLASS_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/shot_gum_glass.png");
    private static final ShotGumModel SHOT_GUM_MODEL = new ShotGumModel();
    private static final Identifier SUGAR_STAFF_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/sugar_staff.png");
    private static final SugarStaffModel SUGAR_STAFF_MODEL = new SugarStaffModel();
    private static final Identifier FROSTMINT_SPEAR_TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/frostmint_spear.png");
    private static final FrostmintSpearModel FROSTMINT_SPEAR_MODEL = new FrostmintSpearModel();

    public static boolean sepiaFlag = false;

    public ACItemstackRenderer() {
        super();
    }


    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext transformType, PoseStack poseStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
        ClientLevel level = Minecraft.getInstance().level;
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        boolean heldIn3d = transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
        boolean left = transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;

        if (itemStackIn.is(ACItemRegistry.CAVE_MAP.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.CAVE_MAP_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            boolean done = CaveMapItem.isFilled(itemStackIn) && !CaveMapItem.isLoading(itemStackIn);
            if(done){
                spriteItem = new ItemStack(ACItemRegistry.CAVE_MAP_FILLED_SPRITE.get());
            }else if(CaveMapItem.isLoading(itemStackIn)){
                spriteItem = new ItemStack(ACItemRegistry.CAVE_MAP_LOADING_SPRITE.get());
            }
            if (transformType.firstPerson() && done) {
                Player player = Minecraft.getInstance().player;
                ItemStack offhandHeldItem = player.getItemInHand(InteractionHand.OFF_HAND);
                boolean renderingSmallMap = !offhandHeldItem.isEmpty();
                boolean offhand = offhandHeldItem.equals(itemStackIn);
                poseStack.pushPose();
                if(renderingSmallMap){
                    poseStack.translate(left ? 0.5F : -0.5F, 0.35, 0);
                    CaveMapRenderHelper.renderOneHandedCaveMap(poseStack, bufferIn, combinedLightIn, 0, offhand ? player.getMainArm().getOpposite() : player.getMainArm(), 0, itemStackIn);
                }else{
                    poseStack.translate(left ? 0.55F : -0.55F, 0.525F, 0.75F);
                    CaveMapRenderHelper.renderTwoHandedCaveMap(poseStack, bufferIn, combinedLightIn, partialTick, 0, 0, itemStackIn);
                }
                poseStack.popPose();
            } else if(heldIn3d && AlexsCaves.CLIENT_CONFIG.caveMapsVisibleInThirdPerson.get() && done){
                poseStack.translate(left ? 0.15F : -0.15F, 0.25F, 0.05F);
                poseStack.scale(1.5F, 1.5F, 1.5F);
                CaveMapRenderHelper.renderCaveMap(poseStack, bufferIn, combinedLightIn, itemStackIn, true);
            }else{
                renderStaticItemSpriteWithLighting(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACItemRegistry.GALENA_GAUNTLET.get())) {
            poseStack.pushPose();
            poseStack.translate(0, 0F, 0);
            poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            poseStack.mulPose(Axis.YP.rotationDegrees(-180));
            float openAmount = heldUseTime(itemStackIn, partialTick, 5F) / 5F;
            float closeAmount = 1F - openAmount;
            float ageInTicks = Minecraft.getInstance().player == null ? 0F : Minecraft.getInstance().player.tickCount + partialTick;
            if (left || transformType == ItemDisplayContext.GUI) {
                GALENA_GAUNTLET_LEFT_MODEL.setupAnim(null, openAmount, 0, ageInTicks, 0, 0);
                GALENA_GAUNTLET_LEFT_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(GALENA_GAUNTLET_TEXTURE), GALENA_GAUNTLET_TEXTURE, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
                GALENA_GAUNTLET_LEFT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(GALENA_GAUNTLET_BLUE_TEXTURE), GALENA_GAUNTLET_BLUE_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, openAmount));
                GALENA_GAUNTLET_LEFT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(GALENA_GAUNTLET_RED_TEXTURE), GALENA_GAUNTLET_RED_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, closeAmount));
            } else {
                GALENA_GAUNTLET_RIGHT_MODEL.setupAnim(null, openAmount, 0, ageInTicks, 0, 0);
                GALENA_GAUNTLET_RIGHT_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(GALENA_GAUNTLET_TEXTURE), GALENA_GAUNTLET_TEXTURE, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
                GALENA_GAUNTLET_RIGHT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(GALENA_GAUNTLET_BLUE_TEXTURE), GALENA_GAUNTLET_BLUE_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, openAmount));
                GALENA_GAUNTLET_RIGHT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(GALENA_GAUNTLET_RED_TEXTURE), GALENA_GAUNTLET_RED_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, closeAmount));
            }
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.RESISTOR_SHIELD.get())) {
            poseStack.pushPose();
            poseStack.translate(0, 0.25F, 0.125F);
            float useTime = heldUseTime(itemStackIn, partialTick, 10F);
            float useProgress = Math.min(10F, useTime) / 10F;
            float useProgressTurn = Math.min(useProgress * 4F, 1F);
            float useProgressUp = (float) Math.sin(useProgress * Math.PI);
            float switchProgress = ResistorShieldItem.getLerpedSwitchTime(itemStackIn, partialTick) / 5F;
            float leftOffset = left ? -1F : 1F;
            if (transformType.firstPerson()) {
                // 26.1 lowers the held item by inverseArmHeight*0.6 while hands are busy (this item's anim is
                // NONE, no custom arm transform), so the deployed shield sat too low; raise it back as it deploys.
                poseStack.translate(useProgressTurn * 0.2F * leftOffset, useProgressUp + useProgress * 0.6F, 0);
                poseStack.mulPose(Axis.XP.rotationDegrees(useProgressTurn * -10));
            } else if (heldIn3d) {
                poseStack.translate(useProgressTurn * 0.4F * leftOffset, useProgress * -0.1F, useProgressTurn * -0.2F);
                poseStack.mulPose(Axis.ZP.rotationDegrees(useProgressTurn * 10 * leftOffset));
                poseStack.mulPose(Axis.YP.rotationDegrees(useProgressTurn * 80 * leftOffset));
            }
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            RESISTOR_SHIELD_MODEL.setupAnim(null, useProgress, switchProgress, 0, 0, 0);
            RESISTOR_SHIELD_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(RESISTOR_SHIELD_TEXTURE), RESISTOR_SHIELD_TEXTURE, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
            RESISTOR_SHIELD_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(RESISTOR_SHIELD_RED_TEXTURE), RESISTOR_SHIELD_RED_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, switchProgress));
            RESISTOR_SHIELD_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(RESISTOR_SHIELD_BLUE_TEXTURE), RESISTOR_SHIELD_BLUE_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, 1.0F - switchProgress));
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.PRIMITIVE_CLUB.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.PRIMITIVE_CLUB_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -1.15F, -0.1F);
                if (transformType.firstPerson()) {
                    poseStack.translate(0, 0.1F, 0);
                    poseStack.scale(0.8F, 0.8F, 0.8F);
                }
                VertexConsumer vertexconsumer = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(PRIMITIVE_CLUB_TEXTURE), itemStackIn.hasFoil());
                PRIMITIVE_CLUB_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACItemRegistry.LIMESTONE_SPEAR.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.LIMESTONE_SPEAR_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                // TRIDENT use animation (= the original's 1.21.1 UseAnim.SPEAR, which 26.1 renamed to TRIDENT).
                // 3rd-person windup uses the raw (un-flipped) model so the TRIDENT arm-cock points the spearhead
                // up-and-back over the shoulder (correct tip direction); every other case takes the XP(-180) flip
                // path. 1st person uses the upstream 0.5 and lets the vanilla TRIDENT windup animate on top.
                if (isWindingUp(itemStackIn) && !transformType.firstPerson()) {
                    poseStack.translate(0, -0.7F, 0.1F);
                } else {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                    poseStack.translate(0, -0.85F, -0.1F);
                    if (transformType.firstPerson()) {
                        if (isWindingUp(itemStackIn)) {
                            // While charging, push the spear DOWN (larger +Y after the XP-180 flip) and AWAY from
                            // the camera (+Z after the flip) so it isn't too high or too close to the face.
                            poseStack.translate(0, 1.7F, 0.5F);
                        } else {
                            poseStack.translate(0, 0.5F, 0F);
                        }
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                }
                VertexConsumer vertexconsumer = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(LIMESTONE_SPEAR_TEXTURE), itemStackIn.hasFoil());
                LIMESTONE_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACItemRegistry.EXTINCTION_SPEAR.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.EXTINCTION_SPEAR_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                // TRIDENT use animation (= the original's 1.21.1 UseAnim.SPEAR, which 26.1 renamed to TRIDENT).
                // 3rd-person windup uses the raw (un-flipped) model so the TRIDENT arm-cock points the spearhead
                // up-and-back over the shoulder (correct tip direction); every other case takes the XP(-180) flip
                // path. 1st person uses the upstream 0.5 and lets the vanilla TRIDENT windup animate on top.
                if (isWindingUp(itemStackIn) && !transformType.firstPerson()) {
                    poseStack.translate(0, -0.7F, 0.1F);
                } else {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                    poseStack.translate(0, -0.85F, -0.1F);
                    if (transformType.firstPerson()) {
                        if (isWindingUp(itemStackIn)) {
                            // While charging, push the spear DOWN (larger +Y after the XP-180 flip) and AWAY from
                            // the camera (+Z after the flip) so it isn't too high or too close to the face.
                            poseStack.translate(0, 1.7F, 0.5F);
                        } else {
                            poseStack.translate(0, 0.5F, 0F);
                        }
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                }
                EXTINCTION_SPEAR_MODEL.resetToDefaultPose();
                VertexConsumer vertexconsumer1 = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(EXTINCTION_SPEAR_TEXTURE), itemStackIn.hasFoil());
                EXTINCTION_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer1, 240, combinedOverlayIn, -1);
                VertexConsumer vertexconsumer2 = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(EXTINCTION_SPEAR_TEXTURE), itemStackIn.hasFoil());
                EXTINCTION_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer2, 240, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACBlockRegistry.SIREN_LIGHT.get().asItem())) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            SIREN_LIGHT_MODEL.resetToDefaultPose();
            SIREN_LIGHT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(SIREN_LIGHT_TEXTURE), SIREN_LIGHT_TEXTURE), combinedLightIn, combinedOverlayIn, -1);
            SIREN_LIGHT_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(SIREN_LIGHT_COLOR_TEXTURE), SIREN_LIGHT_COLOR_TEXTURE), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 0.0F, 1.0F, 0.0F));
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.RAYGUN.get())) {
            float ageInTicks = Minecraft.getInstance().player == null ? 0F : Minecraft.getInstance().player.tickCount + partialTick;
            float useAmount = heldUseTime(itemStackIn, partialTick, 5F) / 5F;
            float pulseAlpha = useAmount * (0.25F + 0.25F * (float) (1F + Math.sin(ageInTicks * 0.8F)));
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.pushPose();
            poseStack.scale(0.9F, 0.9F, 0.9F);
            RAYGUN_MODEL.setupAnim(null, useAmount, ageInTicks,  0, 0, 0);
            boolean gamma = false;
            if (level != null) {
                gamma = ACEnchantmentHelper.getEnchantmentLevel(level, ACEnchantmentRegistry.GAMMA_RAY, itemStackIn) > 0;
            }
            Identifier texture = gamma ? RAYGUN_BLUE_TEXTURE : RAYGUN_TEXTURE;
            Identifier textureActive = gamma ? RAYGUN_BLUE_ACTIVE_TEXTURE : RAYGUN_ACTIVE_TEXTURE;
            RAYGUN_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(texture), texture, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
            RAYGUN_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, ACRenderTypes.getEyesAlphaEnabled(textureActive), textureActive), combinedLightIn, combinedOverlayIn, ColorUtil.packColor(1.0F, 1.0F, 1.0F, pulseAlpha));
            poseStack.popPose();
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.SEA_STAFF.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.SEA_STAFF_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -0.5F, 0);
                if (transformType.firstPerson()) {
                    poseStack.scale(0.6F, 0.6F, 0.6F);
                }
                VertexConsumer vertexconsumer = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(SEA_STAFF_TEXTURE), itemStackIn.hasFoil());
                SEA_STAFF_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACItemRegistry.ORTHOLANCE.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.ORTHOLANCE_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                if (isWindingUp(itemStackIn) && transformType.firstPerson()) {
                    // Drop the couched lance, pull it left into view, and push it a bit forward (smaller +Z).
                    poseStack.translate(-0.22F, -0.55F, 0.0F);
                }
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -1.1F, 0);
                if (isWindingUp(itemStackIn)) {
                    if (transformType.firstPerson()) {
                        // 1st person: XP(-90) couches the lance forward (tip pointing away from the camera).
                        poseStack.mulPose(Axis.XP.rotationDegrees(-90F));
                    } else {
                        // 3rd person the hand frame is rotated, so XP just spins the (vertical) lance — try YP to
                        // tip it forward (couched). EXPERIMENTAL axis; refine from the in-game picture.
                        poseStack.mulPose(Axis.YP.rotationDegrees(90F));
                    }
                }
                if (transformType.firstPerson()) {
                    poseStack.scale(0.6F, 1F, 0.6F);
                }
                VertexConsumer vertexconsumer = getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(ORTHOLANCE_TEXTURE), ORTHOLANCE_TEXTURE, itemStackIn.hasFoil());
                ORTHOLANCE_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACBlockRegistry.COPPER_VALVE.get().asItem())) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            COPPER_VALVE_MODEL.resetToDefaultPose();
            COPPER_VALVE_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(COPPER_VALVE_TEXTURE), COPPER_VALVE_TEXTURE), combinedLightIn, combinedOverlayIn, -1);
            poseStack.popPose();
        }

        if (itemStackIn.is(ACBlockRegistry.BEHOLDER.get().asItem())) {
            float ageInTicks = Minecraft.getInstance().player == null ? 0F : Minecraft.getInstance().player.tickCount + partialTick;
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            BEHOLDER_MODEL.setupAnim(null, 0.0F, 45F, ageInTicks, 0, 0);
            BEHOLDER_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(BEHOLDER_TEXTURE),BEHOLDER_TEXTURE), combinedLightIn, combinedOverlayIn, -1);
            BEHOLDER_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.eyes(BEHOLDER_TEXTURE_EYE), BEHOLDER_TEXTURE_EYE), combinedLightIn, combinedOverlayIn, -1);
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.DREADBOW.get())) {
            float ageInTicks = Minecraft.getInstance().player == null ? 0F : Minecraft.getInstance().player.tickCount + partialTick;
            float pullAmount = DreadbowItem.getPullingAmount(Minecraft.getInstance().level, itemStackIn, partialTick);
            // Only show pulled state when player is actively using a dreadbow
            boolean isPlayerUsingDreadbow = Minecraft.getInstance().player != null && Minecraft.getInstance().player.isUsingItem() && Minecraft.getInstance().player.getUseItem().is(ACItemRegistry.DREADBOW.get());
            if (!isPlayerUsingDreadbow) {
                pullAmount = 0;
            }
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(pullAmount >= 0.8F ? ACItemRegistry.DREADBOW_PULLING_2_SPRITE.get() : pullAmount >= 0.5F ? ACItemRegistry.DREADBOW_PULLING_1_SPRITE.get() : pullAmount > 0.0F ? ACItemRegistry.DREADBOW_PULLING_0_SPRITE.get() : ACItemRegistry.DREADBOW_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                if (transformType.firstPerson()) {
                    // 26.1's ItemInHandRenderer lowers BOW items by inverseArmHeight*0.6 (down) while the hand
                    // is busy (drawing), which 1.21.1 didn't — the bow visibly dropped as you drew. The dreadbow's
                    // first-person display transform is identity (no rotation/scale), so renderByItem's frame IS
                    // the arm frame in matching units: add inverseArmHeight*0.6 back along +Y to cancel the drop
                    // so the dreadbow stays at a CONSTANT height held AND while charging, like the original.
                    poseStack.translate(left ? -0.1F : 0.1F, 0.1F + inverseArmHeight(partialTick) * 0.6F, -0.1F);
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    poseStack.mulPose(Axis.XP.rotationDegrees(15));
                }else{
                    poseStack.translate(left ? 0.1F : -0.1F, -0.45F, 0.35F - pullAmount * 0.3F);
                    poseStack.mulPose(Axis.YP.rotationDegrees(left ? 7 : -7));
                }
                EntityType type = DreadbowItem.getTypeOfArrow(itemStackIn);
                DREADBOW_MODEL.setupAnim(null, pullAmount, ageInTicks,  0, 0, 0);
                // 26.1 removed EntityRenderDispatcher#render(...immediate...); the pulled-arrow preview
                // entity render is skipped (it cannot be routed through the submit pipeline here).
                VertexConsumer vertexconsumer = getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(DREADBOW_TEXTURE), DREADBOW_TEXTURE, itemStackIn.hasFoil());
                DREADBOW_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                Identifier eyeTexture = DreadbowItem.getPerfectShotTicks(itemStackIn) > 0 ? DREADBOW_TEXTURE_EYE_PERFECT : DREADBOW_TEXTURE_EYE;
                DREADBOW_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.eyes(eyeTexture), eyeTexture), combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACBlockRegistry.GOBTHUMPER.get().asItem())) {
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            GOBTHUMPER_MODEL.setupAnim(null, 0.0F, 0.0F, 0.0F, 0, 0);
            GOBTHUMPER_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(GOBTHUMPER_TEXTURE),GOBTHUMPER_TEXTURE), combinedLightIn, combinedOverlayIn, -1);
            GOBTHUMPER_MODEL.renderToBuffer(poseStack, getVertexConsumer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(GOBTHUMPER_JELLY_TEXTURE), GOBTHUMPER_JELLY_TEXTURE), combinedLightIn, combinedOverlayIn, -1);
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.SHOT_GUM.get())) {
            float shootProgress = ShotGumItem.getLerpedShootTime(itemStackIn, partialTick) / 5F;
            poseStack.pushPose();
            poseStack.translate(0.5F, 1.5F, 0.5F);
            poseStack.mulPose(Axis.XP.rotationDegrees(-180));
            poseStack.mulPose(Axis.YP.rotationDegrees(180));
            poseStack.pushPose();
            poseStack.scale(0.8F, 0.8F, 0.8F);

            SHOT_GUM_MODEL.setupAnim(null, shootProgress, ShotGumItem.getGumballsLeft(itemStackIn),  ShotGumItem.getLerpedCrankAngle(itemStackIn, partialTick), 0, 0);
            SHOT_GUM_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(SHOT_GUM_TEXTURE), SHOT_GUM_TEXTURE, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
            SHOT_GUM_MODEL.renderToBuffer(poseStack, getVertexConsumerFoil(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityTranslucent(SHOT_GUM_GLASS_TEXTURE), SHOT_GUM_GLASS_TEXTURE, itemStackIn.hasFoil()), combinedLightIn, combinedOverlayIn, -1);
            poseStack.popPose();
            poseStack.popPose();
        }

        if (itemStackIn.is(ACItemRegistry.SUGAR_STAFF.get())) {
            float ageInTicks = Minecraft.getInstance().player == null ? 0F : Minecraft.getInstance().player.tickCount + partialTick;
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.SUGAR_STAFF_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                poseStack.translate(0, -1.0F, 0);
                if (transformType.firstPerson()) {
                    poseStack.translate(0, 0.4F, 0);
                    poseStack.scale(0.6F, 0.6F, 0.6F);
                }
                VertexConsumer vertexconsumer = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.armorCutoutNoCull(SUGAR_STAFF_TEXTURE), itemStackIn.hasFoil());
                SUGAR_STAFF_MODEL.setupAnim(null, 0.0F, 0.0F,  ageInTicks, 0, 0);
                SUGAR_STAFF_MODEL.renderToBuffer(poseStack, vertexconsumer, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }

        if (itemStackIn.is(ACItemRegistry.FROSTMINT_SPEAR.get())) {
            poseStack.translate(0.5F, 0.5F, 0.5F);
            ItemStack spriteItem = new ItemStack(ACItemRegistry.FROSTMINT_SPEAR_SPRITE.get());
            spriteItem.applyComponents(itemStackIn.getComponents());
            if (heldIn3d) {
                poseStack.pushPose();
                // During windup the item model swaps to item/frostmint_spear_throwing.json in the original; the
                // vanilla TRIDENT use animation (= the original's 1.21.1 UseAnim.SPEAR, renamed to TRIDENT in 26.1)
                // provides the arm-cock over the shoulder, and this branch reproduces that model's own display
                // transforms exactly (translation values are /16 blocks; apply order translate -> Xrot -> Yrot ->
                // Zrot -> scale, matching vanilla ItemTransform.apply). The XP(-180) flip is kept because it IS the
                // throwing model's thirdperson rotation [-180,0,0]. Non-windup keeps the upstream held pose.
                if (isWindingUp(itemStackIn)) {
                    if (transformType.firstPerson()) {
                        // frostmint_spear_throwing.json firstperson_*hand: no XP(-180) flip; the display rotation is
                        // [-20, +-10, 15] with translation [2,-5,-7] (right) / [3.5,-4,-8] (left), in /16 blocks.
                        if (left) {
                            poseStack.translate(3.5F / 16F, -4F / 16F, -8F / 16F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(-20F));
                            poseStack.mulPose(Axis.YP.rotationDegrees(-10F));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(15F));
                        } else {
                            poseStack.translate(2F / 16F, -5F / 16F, -7F / 16F);
                            poseStack.mulPose(Axis.XP.rotationDegrees(-20F));
                            poseStack.mulPose(Axis.YP.rotationDegrees(10F));
                            poseStack.mulPose(Axis.ZP.rotationDegrees(15F));
                        }
                    } else {
                        // frostmint_spear_throwing.json thirdperson_*hand: rotation [-180,0,0], translation [0,-4,2]
                        // (/16 blocks). Order is translate then rotate, matching vanilla ItemTransform.apply.
                        poseStack.translate(0F, -4F / 16F, 2F / 16F);
                        poseStack.mulPose(Axis.XP.rotationDegrees(-180F));
                    }
                    // Reverse the spearhead: the port renders the plain handheld sprite, which lacks the original
                    // throwing model's built-in geometry flip, so the tip came out the wrong way. An in-plane 180
                    // spin at the end fixes the direction while keeping the pose position (stays visible).
                    poseStack.mulPose(Axis.ZP.rotationDegrees(180F));
                } else {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-180));
                    poseStack.translate(0, -0.85F, -0.1F);
                    if (transformType.firstPerson()) {
                        poseStack.translate(0, 0.5F, 0F);
                        poseStack.scale(0.75F, 0.75F, 0.75F);
                    }
                }
                FROSTMINT_SPEAR_MODEL.resetToDefaultPose();
                VertexConsumer vertexconsumer1 = getArmorFoilBuffer(bufferIn, net.minecraft.client.renderer.rendertype.RenderTypes.entityCutout(FROSTMINT_SPEAR_TEXTURE), itemStackIn.hasFoil());
                FROSTMINT_SPEAR_MODEL.renderToBuffer(poseStack, vertexconsumer1, combinedLightIn, combinedOverlayIn, -1);
                poseStack.popPose();
            } else {
                renderStaticItemSprite(spriteItem, transformType, combinedLightIn, combinedOverlayIn, poseStack, bufferIn, level);
            }
        }
    }

    private void renderStaticItemSprite(ItemStack spriteItem, ItemDisplayContext transformType, int combinedLightIn, int combinedOverlayIn, PoseStack poseStack, MultiBufferSource bufferIn, ClientLevel level) {
        if(sepiaFlag){
            ItemWidget.renderSepiaItem(poseStack, null, spriteItem, Minecraft.getInstance().renderBuffers().bufferSource());
        }else{
            com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat.drawItem(spriteItem, transformType, transformType == ItemDisplayContext.GROUND ? combinedLightIn : 240, combinedOverlayIn, poseStack, bufferIn, level);
        }
    }

    private void renderStaticItemSpriteWithLighting(ItemStack spriteItem, ItemDisplayContext transformType, int combinedLightIn, int combinedOverlayIn, PoseStack poseStack, MultiBufferSource bufferIn, ClientLevel level) {
        if(sepiaFlag){
            ItemWidget.renderSepiaItem(poseStack, null, spriteItem, Minecraft.getInstance().renderBuffers().bufferSource());
        }else{
            com.github.alexmodguy.alexscaves.client.render.compat.ItemRenderCompat.drawItem(spriteItem, transformType, transformType != ItemDisplayContext.GUI ? combinedLightIn : 240, combinedOverlayIn, poseStack, bufferIn, level);
        }
    }

    /** True when the local player is actively using (winding up) an item of the same type as {@code stack}. */
    private static boolean isWindingUp(ItemStack stack) {
        Player player = Minecraft.getInstance().player;
        return player != null && player.isUsingItem() && player.getUseItem().getItem() == stack.getItem();
    }

    // 26.1: the per-item UseTime NBT is written only in the client branch of Item.inventoryTick, but
    // inventoryTick is server-only now, so the old getLerped*Time accessors stay 0 on the client and the
    // weapons' active glow / deploy animations never play. Derive the use progress from the synced use-tick
    // count of the local player's held item instead, capped to the original NBT max (local player only).
    // 26.1's ItemInHandRenderer lowers BOW/TRIDENT/NONE first-person items by inverseArmHeight*0.6
    // (via applyItemArmTransform) while the hand is busy — height = 1 - lerp(pt, oMainHandHeight, mainHandHeight).
    // Mirror that value so renderByItem can add the drop back and keep items at a constant height while using.
    private static float inverseArmHeight(float partialTick) {
        if (Minecraft.getInstance().gameRenderer.itemInHandRenderer instanceof ItemInHandRendererAccessor acc) {
            return 1.0F - Mth.lerp(partialTick, acc.getOMainHandHeight(), acc.getMainHandHeight());
        }
        return 0F;
    }

    private static float heldUseTime(ItemStack stack, float partialTick, float maxTicks) {
        Player player = Minecraft.getInstance().player;
        if (player != null && player.isUsingItem() && player.getUseItem().getItem() == stack.getItem()) {
            return Math.min(player.getTicksUsingItem(partialTick), maxTicks);
        }
        return 0F;
    }

    // 26.1: the enchant-glint foil overlay helpers (ItemRenderer.get*FoilBuffer) were removed; these
    // now resolve a plain buffer for the given render type (glint dropped).
    private static VertexConsumer getArmorFoilBuffer(MultiBufferSource bufferIn, RenderType renderType, boolean foil) {
        return bufferIn.getBuffer(renderType);
    }

    private static VertexConsumer getVertexConsumerFoil(MultiBufferSource bufferIn, RenderType _default, Identifier resourceLocation, boolean foil){
        return sepiaFlag ? bufferIn.getBuffer(ACRenderTypes.getBookWidget(resourceLocation, true)) : bufferIn.getBuffer(_default);
    }
    private static VertexConsumer getVertexConsumer(MultiBufferSource bufferIn, RenderType _default, Identifier resourceLocation){
        return sepiaFlag ? bufferIn.getBuffer(ACRenderTypes.getBookWidget(resourceLocation, true)) : bufferIn.getBuffer(_default);
    }
}
