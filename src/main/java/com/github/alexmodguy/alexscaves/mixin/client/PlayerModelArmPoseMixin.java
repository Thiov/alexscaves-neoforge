package com.github.alexmodguy.alexscaves.mixin.client;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.render.entity.state.ACEffectRenderState;
import com.github.alexmodguy.alexscaves.server.item.GalenaGauntletItem;
import com.github.alexmodguy.alexscaves.server.item.OrtholanceItem;
import com.github.alexmodguy.alexscaves.server.item.RaygunItem;
import com.github.alexmodguy.alexscaves.server.item.ResistorShieldItem;
import com.github.alexmodguy.alexscaves.server.item.ShotGumItem;
import com.github.alexthe666.citadel.client.tick.ClientTickRateTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reinstates AC's weapon arm-raise poses, which upstream drove through the Citadel {@code EventPosePlayerHand}
 * NeoForge event (dropped in this port). Runs after vanilla {@code PlayerModel.setupAnim} and overrides the
 * held arm's rotation for AC weapons that "aim" (raygun, galena gauntlet, resistor shield, spears, shot gum)
 * based on each item's use progress. No-op for every non-AC item, so it can't affect normal player rendering.
 * The 26.1 render state already exposes the per-arm held stacks + use ticks, so no separate capture is needed.
 *
 * <p>Extends {@link HumanoidModel} so {@code head}/{@code rightArm}/{@code leftArm} resolve as inherited public
 * fields — {@code @Shadow} can't see superclass fields, which previously failed the mixin apply (black screen).
 */
@Mixin(PlayerModel.class)
public abstract class PlayerModelArmPoseMixin extends HumanoidModel<AvatarRenderState> {

    public PlayerModelArmPoseMixin(ModelPart root) {
        super(root);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void alexscaves$weaponArmPoses(AvatarRenderState state, CallbackInfo ci) {
        float partialTick = Minecraft.getInstance().getDeltaTracker().getGameTimeDeltaPartialTick(false);
        // Sugar Rush gait first, so an actively-aimed AC weapon (below) still overrides the arm raise.
        alexscaves$sugarRushGait(state);
        alexscaves$poseArm(this.rightArm, state.rightHandItemStack, state, partialTick, true);
        alexscaves$poseArm(this.leftArm, state.leftHandItemStack, state, partialTick, false);
        alexscaves$poseShotGum(state);
    }

    /**
     * Frantic Sugar Rush walk/run gait (upstream {@code ClientEvents} ~632-663), driven off the render state's
     * walk animation + head angles. {@code speedModifier} is scaled by the client tick rate so the animation
     * keeps pace while Sugar Rush slow-motion is active (the tick-rate modifier is now client-side again).
     * Skipped for the first-person local player, matching upstream's {@code !isFirstPersonPlayer} guard.
     */
    @Unique
    private void alexscaves$sugarRushGait(AvatarRenderState state) {
        if (!((ACEffectRenderState) state).alexscaves$isSugarRush()) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        Entity camera = minecraft.getCameraEntity();
        if (camera != null && camera.getId() == state.id && minecraft.options.getCameraType().isFirstPerson()) {
            return;
        }
        float speedModifier = 0.35F;
        if (AlexsCaves.COMMON_CONFIG.sugarRushSlowsTime.get()
                && AlexsCaves.PROXY.isTickRateModificationActive(minecraft.level)) {
            float tickRate = ClientTickRateTracker.getForClient(minecraft).getClientTickRate() / 50.0F;
            speedModifier *= tickRate;
        }
        float deltaSpeed = 1.0F;
        float walkPos = state.walkAnimationPos;
        float walkSpeed = state.walkAnimationSpeed;
        float headXRot = state.xRot;
        float headYRot = state.yRot;
        this.rightArm.xRot = Mth.cos(walkPos * speedModifier + (float) Math.PI * 0.5F) * 2.0F * walkSpeed * 0.5F / deltaSpeed;
        this.leftArm.xRot = Mth.cos(walkPos * speedModifier) * 2.0F * walkSpeed * 0.5F / deltaSpeed;
        this.rightArm.zRot = (Mth.sin(walkPos * -speedModifier + (float) Math.PI * 0.5F) + 2.5F) * 1.5F * walkSpeed * 0.5F / deltaSpeed;
        this.leftArm.zRot = (Mth.sin(walkPos * -speedModifier) - 2.5F) * 1.5F * walkSpeed * 0.5F / deltaSpeed;
        this.head.xRot = headXRot * ((float) Math.PI / 180F) + Mth.cos(walkPos * speedModifier + (float) Math.PI) * 1.0F * walkSpeed * 0.5F / deltaSpeed;
        this.head.yRot = headYRot * ((float) Math.PI / 180F) + Mth.sin(walkPos * speedModifier + (float) Math.PI) * 1.0F * walkSpeed * 0.5F / deltaSpeed;
        this.leftLeg.xRot = Mth.cos(walkPos * speedModifier + (float) Math.PI) * 4.0F * walkSpeed * 0.5F / deltaSpeed;
        this.rightLeg.xRot = Mth.cos(walkPos * speedModifier) * 4.0F * walkSpeed * 0.5F / deltaSpeed;
    }

    @Unique
    private void alexscaves$poseArm(ModelPart arm, ItemStack stack, AvatarRenderState state, float partialTick, boolean right) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        Item item = stack.getItem();
        // These AC weapons report ItemUseAnimation.NONE, so vanilla gives them no pose. Drive the raise from
        // the vanilla per-arm use ticks (reliably synced — same signal that makes the dreadbow draw work)
        // rather than the items' own UseTime data component, which doesn't reliably reach the render state.
        float useTicks = state.ticksUsingItem(right ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
        if (useTicks <= 0F) {
            return;
        }
        if (item instanceof ResistorShieldItem) {
            float p = Math.min(10F, useTicks) / 10F;
            float turn = Math.min(p * 4F, 1F);
            float up = (float) Math.sin(p * Math.PI);
            float tilt = state.isCrouching ? 120F : 80F;
            arm.xRot = -(float) Math.toRadians(tilt) - (float) Math.toRadians(80F) * up;
            arm.yRot = (right ? -1F : 1F) * (float) Math.toRadians(20F) * turn;
        } else if (item instanceof GalenaGauntletItem) {
            float p = Math.min(5F, useTicks) / 5F;
            arm.xRot = (this.head.xRot - (float) Math.toRadians(80F)) * p;
            arm.yRot = this.head.yRot * p;
        } else if (item instanceof RaygunItem) {
            float p = Math.min(5F, useTicks) / 5F;
            arm.xRot = (this.head.xRot - (float) Math.toRadians(80F)) * p;
            arm.yRot = this.head.yRot * p;
            arm.zRot = 0F;
        } else if (item instanceof OrtholanceItem) {
            // Charging lance (3rd person): only a small forearm raise — a 45deg raise still put it way too high.
            float p = Math.min(1F, useTicks / 10F);
            arm.xRot = this.head.xRot - (float) Math.toRadians(20F) * p;
            arm.yRot = this.head.yRot * p;
            arm.zRot = 0F;
        }
        // NOTE: all poses are driven from the vanilla per-arm use ticks (state.ticksUsingItem), which is reliably
        // synced (same signal that makes the dreadbow draw work) — not the items' own UseTime data component.
    }

    @Unique
    private void alexscaves$poseShotGum(AvatarRenderState state) {
        if (state.rightHandItemStack.getItem() instanceof ShotGumItem && ShotGumItem.shouldBeHeldUpright(state.rightHandItemStack)) {
            this.rightArm.xRot = this.head.xRot - (float) Math.toRadians(70F);
            this.rightArm.yRot = this.head.yRot;
            this.rightArm.zRot = 0F;
            this.leftArm.xRot = this.head.xRot - (float) Math.toRadians(70F);
            this.leftArm.yRot = this.head.yRot + (float) Math.toRadians(40F);
            this.leftArm.zRot = (float) Math.toRadians(20F);
        }
        if (state.leftHandItemStack.getItem() instanceof ShotGumItem && ShotGumItem.shouldBeHeldUpright(state.leftHandItemStack)) {
            this.leftArm.xRot = this.head.xRot - (float) Math.toRadians(70F);
            this.leftArm.yRot = this.head.yRot;
            this.leftArm.zRot = 0F;
            this.rightArm.xRot = this.head.xRot - (float) Math.toRadians(70F);
            this.rightArm.yRot = this.head.yRot - (float) Math.toRadians(40F);
            this.rightArm.zRot = -(float) Math.toRadians(20F);
        }
    }
}
