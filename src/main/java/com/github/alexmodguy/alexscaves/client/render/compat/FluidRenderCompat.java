package com.github.alexmodguy.alexscaves.client.render.compat;

import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluid;

/**
 * 26.1.2: fluid render registration is NOT done here anymore. The Fabric
 * {@code FluidRenderingRegistry} does not exist on NeoForge, and the still/flowing sprite + camera-overlay
 * wiring moved to {@link com.github.alexmodguy.alexscaves.client.render.ACFluidRegistryClient} via
 * {@code RegisterFluidModelsEvent} (sprites) and {@code RegisterClientExtensionsEvent} (overlay), which are
 * registered on the client mod bus by {@code ClientProxy}.
 *
 * <p>This class is now unreferenced and retained only so dangling imports elsewhere stay resolvable. The
 * methods are inert no-ops.
 *
 * <p>TODO 26.1.2: delete this file once nothing imports it.
 */
public final class FluidRenderCompat {

    private FluidRenderCompat() {
    }

    /**
     * @deprecated No-op on NeoForge. Fluid models are registered by {@code ACFluidRegistryClient}.
     */
    @Deprecated
    public static void register(Fluid still, Fluid flowing, Identifier[] textures) {
        // no-op: handled by ACFluidRegistryClient.registerFluidModels(RegisterFluidModelsEvent)
    }

    public static Identifier[] handler(Identifier stillTexture, Identifier flowingTexture) {
        return new Identifier[]{stillTexture, flowingTexture};
    }
}
