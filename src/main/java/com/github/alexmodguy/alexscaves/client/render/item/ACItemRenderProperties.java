package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.mcshim.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

public class ACItemRenderProperties implements IClientItemExtensions {

    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new ACItemstackRenderer();
    }
}
