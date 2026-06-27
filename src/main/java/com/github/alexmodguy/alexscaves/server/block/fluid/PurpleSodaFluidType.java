package com.github.alexmodguy.alexscaves.server.block.fluid;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.particle.ACParticleRegistry;
import com.github.alexmodguy.alexscaves.server.block.ACBlockRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PurpleSodaFluidType extends FluidType {

    public static final Identifier FLUID_STILL = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "block/purple_soda_still");
    public static final Identifier FLUID_FLOWING = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "block/purple_soda_flowing");
    public static final Identifier OVERLAY = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/misc/under_purple_soda.png");

    public PurpleSodaFluidType(Properties properties) {
        super(properties);
    }

    
    public void initializeClient(Consumer<IClientFluidTypeExtensions> consumer) {
        consumer.accept(new IClientFluidTypeExtensions() {
            
            public Identifier getStillTexture() {
                return FLUID_STILL;
            }

            
            public Identifier getFlowingTexture() {
                return FLUID_FLOWING;
            }

            
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return OVERLAY;
            }

        });
    }

    
    public boolean move(FluidState state, LivingEntity entity, Vec3 movementVector, double gravity) {
        double d9 = entity.getY();
        float f4 = 0.8F;
        float f5 = 0.02F;
        boolean flag = entity.getDeltaMovement().y <= 0.0D;
        if (entity.hasEffect(MobEffects.DOLPHINS_GRACE)) {
            f4 = 0.96F;
        }

        f5 *= (float) (1.0D + entity.getAttributeValue(net.neoforged.neoforge.common.NeoForgeMod.SWIM_SPEED));
        entity.moveRelative(f5, movementVector);
        entity.move(MoverType.SELF, entity.getDeltaMovement());
        Vec3 vec36 = entity.getDeltaMovement();
        if (entity.horizontalCollision && entity.onClimbable()) {
            vec36 = new Vec3(vec36.x, 0.2D, vec36.z);
        }

        entity.setDeltaMovement(vec36.multiply((double) f4, (double) 0.8F, (double) f4));
        Vec3 vec32 = entity.getFluidFallingAdjustedMovement(gravity, flag, entity.getDeltaMovement());
        entity.setDeltaMovement(vec32);
        if (entity.horizontalCollision && entity.isFree(vec32.x, vec32.y + (double) 0.6F - entity.getY() + d9, vec32.z)) {
            entity.setDeltaMovement(vec32.x, (double) 0.3F, vec32.z);
        }
        return true;
    }

    
    public boolean isVaporizedOnPlacement(Level level, BlockPos pos, FluidStack stack) {
        return false;
    }

    
    public void onVaporize(@Nullable Player player, Level level, BlockPos pos, FluidStack stack) {
        SoundEvent sound = this.getSound(player, level, pos, SoundActions.FLUID_VAPORIZE);
        level.playSound(player, pos, sound != null ? sound : SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.8F);
        for (int l = 0; l < 8; ++l) {
            level.addAlwaysVisibleParticle(ACParticleRegistry.PURPLE_SODA_FIZZ.get(), (double) pos.getX() + Math.random(), (double) pos.getY() + Math.random(), (double) pos.getZ() + Math.random(), (Math.random() - 0.5F) * 0.25F, Math.random() * 0.25F, (Math.random() - 0.5F) * 0.25F);
        }
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
    }
}
