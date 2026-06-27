package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.renderer.block.model.*;

import net.minecraft.resources.Identifier;

public class ItemOverride {
    private String model = "minecraft:air";

    public Identifier getModel() {
        return Identifier.parse(this.model);
    }
}
