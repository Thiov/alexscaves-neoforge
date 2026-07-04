package com.github.alexmodguy.alexscaves.client.render.hud;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.gui.GuiCompat;
import com.github.alexmodguy.alexscaves.server.entity.living.AtlatitanEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.CandicornEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.SubterranodonEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorsaurusEntity;
import com.github.alexmodguy.alexscaves.server.entity.living.TremorzillaEntity;
import com.github.alexmodguy.alexscaves.server.entity.util.RidingMeterMount;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.GuiLayer;

/**
 * The "charge cone" indicator that fills while riding a Candicorn / dinosaur mount (RidingMeterMount) and
 * unleashes on the ability key. NeoForge {@link GuiLayer} — registered above the HOTBAR in ClientProxy.
 * Ported from upstream-1.21.1 ClientEvents.onPostRenderGuiOverlay (RidingMeterMount branch).
 */
public class RidingMeterGuiLayer implements GuiLayer {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "riding_meter");
    private static final Identifier DINOSAUR_HUD_OVERLAYS =
            Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/dinosaur_hud_overlays.png");

    @Override
    public void render(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.options.hideGui || minecraft.getCameraEntity() != player) {
            return;
        }
        if (!(player.getVehicle() instanceof RidingMeterMount mount) || !mount.hasRidingMeter()) {
            return;
        }
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int baseY = 53;
        if (player.getArmorValue() > 0 && mount instanceof SubterranodonEntity) {
            baseY += 25;
        }
        int j = screenWidth / 2 - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorX.get();
        int k = screenHeight - baseY - AlexsCaves.CLIENT_CONFIG.subterranodonIndicatorY.get();
        float f = mount.getMeterAmount();
        float invProgress = 1.0F - f;
        int uOffset = 0;
        int vOffset = 0;
        int dinoHeight = 31;
        if (mount instanceof TremorsaurusEntity) {
            vOffset = 63;
            k += 5;
        } else if (mount instanceof AtlatitanEntity) {
            vOffset = 126;
            dinoHeight = 32;
            k += 3;
        } else if (mount instanceof TremorzillaEntity tremorzilla) {
            vOffset = 193;
            if (tremorzilla.isPowered() && !tremorzilla.isFiring() && tremorzilla.getSpikesDownAmount() > 0) {
                if (tremorzilla.tickCount / 2 % 2 == 1) {
                    vOffset = 251;
                }
                invProgress = 1.0F;
            }
            dinoHeight = 29;
            k += 5;
        } else if (mount instanceof CandicornEntity) {
            vOffset = 280;
            dinoHeight = 25;
            k += 4;
        }
        GuiCompat.blit(guiGraphics, DINOSAUR_HUD_OVERLAYS, j, k, uOffset, vOffset + dinoHeight, 43, dinoHeight, 128, 512);
        int fillHeight = (int) Math.floor(dinoHeight * invProgress);
        if (fillHeight > 0) {
            GuiCompat.blit(guiGraphics, DINOSAUR_HUD_OVERLAYS, j, k, uOffset, vOffset, 43, fillHeight, 128, 512);
        }
    }
}
