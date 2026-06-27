package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

public class EntityRendererContextCompat {

    private EntityRendererContextCompat() {
    }

    public static ItemInHandRenderer getItemInHandRenderer(EntityRendererProvider.Context context) {
        Minecraft minecraft = Minecraft.getInstance();
        // 26.1: ItemInHandRenderer(Minecraft, EntityRenderDispatcher, ItemModelResolver);
        // Minecraft#getItemRenderer() was removed.
        return new ItemInHandRenderer(
            minecraft,
            context.getEntityRenderDispatcher(),
            context.getItemModelResolver()
        );
    }
}
