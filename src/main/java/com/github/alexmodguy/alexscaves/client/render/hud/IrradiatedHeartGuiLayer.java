package com.github.alexmodguy.alexscaves.client.render.hud;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.ClientProxy;
import com.github.alexmodguy.alexscaves.client.gui.GuiCompat;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;

/**
 * Radioactive heart HUD overlay drawn over the vanilla hearts while Irradiated (upstream
 * {@code ClientEvents} ~743-801). NeoForge {@link GuiLayer} replacement for the Fabric
 * {@code IrradiatedHeartHudElement}; registered via {@code RegisterGuiLayersEvent.registerAbove(VanillaGuiLayers.PLAYER_HEALTH, ...)}.
 */
public class IrradiatedHeartGuiLayer implements GuiLayer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "irradiated_hearts");
    private static final Identifier POTION_EFFECT_HUD_OVERLAYS =
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/potion_effect_hud_overlays.png");

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.gameMode == null
                || !minecraft.gameMode.canHurtPlayer() || minecraft.getCameraEntity() != player
                || !player.hasEffect(ACEffectRegistry.IRRADIATED)) {
            return;
        }
        int leftHeight = 39;
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        int healthInt = Mth.ceil(player.getHealth());
        int guiTicks = minecraft.gui.getGuiTicks();
        AttributeInstance attrMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        float healthMax = attrMaxHealth == null ? player.getMaxHealth() : (float) attrMaxHealth.getValue();
        float absorb = Mth.ceil(player.getAbsorptionAmount());
        int healthRows = Mth.ceil((healthMax + absorb) / 2.0F / 10.0F);
        int rowHeight = Math.max(10 - (healthRows - 2), 3);
        ClientProxy.random.setSeed(guiTicks * 312871L);
        int left = width / 2 - 91;
        int top = height - leftHeight;
        int regen = -1;
        if (player.hasEffect(MobEffects.REGENERATION)) {
            regen = guiTicks % Mth.ceil(healthMax + 5.0F);
        }
        final int heartV = player.level().getLevelData().isHardcore() ? 9 : 0;
        int heartU = 0;
        float absorbRemaining = absorb;
        for (int i = Mth.ceil((healthMax + absorb) / 2.0F) - 1; i >= 0; --i) {
            int row = Mth.ceil((float) (i + 1) / 10.0F) - 1;
            int x = left + i % 10 * 8;
            int y = top - row * rowHeight;
            if (healthInt <= 4) {
                y += ClientProxy.random.nextInt(2);
            }
            if (i == regen) {
                y -= 2;
            }
            GuiCompat.blit(guiGraphics, POTION_EFFECT_HUD_OVERLAYS, x, y, heartU, heartV + 18, 9, 9, 32, 32);
            if (absorbRemaining > 0.0F) {
                if (absorbRemaining == absorb && absorb % 2.0F == 1.0F) {
                    GuiCompat.blit(guiGraphics, POTION_EFFECT_HUD_OVERLAYS, x, y, heartU + 9, heartV, 9, 9, 32, 32);
                    absorbRemaining -= 1.0F;
                } else {
                    GuiCompat.blit(guiGraphics, POTION_EFFECT_HUD_OVERLAYS, x, y, heartU, heartV, 9, 9, 32, 32);
                    absorbRemaining -= 2.0F;
                }
            } else if (i * 2 + 1 < healthInt) {
                GuiCompat.blit(guiGraphics, POTION_EFFECT_HUD_OVERLAYS, x, y, heartU, heartV, 9, 9, 32, 32);
            } else if (i * 2 + 1 == healthInt) {
                GuiCompat.blit(guiGraphics, POTION_EFFECT_HUD_OVERLAYS, x, y, heartU + 9, heartV, 9, 9, 32, 32);
            }
        }
    }
}
