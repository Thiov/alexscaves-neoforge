package com.github.alexmodguy.alexscaves.client.render;

import com.github.alexmodguy.alexscaves.server.block.fluid.ACFluidRegistry;
import com.github.alexmodguy.alexscaves.server.block.fluid.AcidFluidType;
import com.github.alexmodguy.alexscaves.server.block.fluid.PurpleSodaFluidType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/**
 * Client-side wiring for Alex's Caves fluids on NeoForge 26.1.2.
 *
 * <p>In 26.1.2 the per-FluidType {@code initializeClient} hook is gone:
 * the camera overlay is registered via {@link RegisterClientExtensionsEvent}
 * and the still/flowing sprites are registered via {@link RegisterFluidModelsEvent}.
 * Register both methods on the CLIENT mod event bus.
 */
public final class ACFluidRegistryClient {

    private ACFluidRegistryClient() {
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return AcidFluidType.OVERLAY;
            }
        }, ACFluidRegistry.ACID_FLUID_TYPE.value());

        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public Identifier getRenderOverlayTexture(Minecraft mc) {
                return PurpleSodaFluidType.OVERLAY;
            }
        }, ACFluidRegistry.PURPLE_SODA_FLUID_TYPE.value());
    }

    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        // null overlay Material + null FluidTintSource. The 4th arg null is cast to FluidTintSource to
        // disambiguate from the deprecated (Material, Material, Material, BlockTintSource) overload.
        Material acidStill = new Material(AcidFluidType.FLUID_STILL);
        Material acidFlowing = new Material(AcidFluidType.FLUID_FLOWING);
        FluidModel.Unbaked acidModel = new FluidModel.Unbaked(acidStill, acidFlowing, null, (FluidTintSource) null);
        event.register(acidModel, ACFluidRegistry.ACID_FLUID_SOURCE.value());
        event.register(acidModel, ACFluidRegistry.ACID_FLUID_FLOWING.value());

        Material sodaStill = new Material(PurpleSodaFluidType.FLUID_STILL);
        Material sodaFlowing = new Material(PurpleSodaFluidType.FLUID_FLOWING);
        FluidModel.Unbaked sodaModel = new FluidModel.Unbaked(sodaStill, sodaFlowing, null, (FluidTintSource) null);
        event.register(sodaModel, ACFluidRegistry.PURPLE_SODA_FLUID_SOURCE.value());
        event.register(sodaModel, ACFluidRegistry.PURPLE_SODA_FLUID_FLOWING.value());
    }
}
