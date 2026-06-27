package com.github.alexmodguy.alexscaves.server.misc;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ACKeybindRegistry {

    // 26.1: KeyMapping takes a KeyMapping.Category instead of a String category id.
    public static final KeyMapping KEY_SPECIAL_ABILITY = new KeyMapping("key.special_ability", InputConstants.KEY_G, KeyMapping.Category.MISC);
}
