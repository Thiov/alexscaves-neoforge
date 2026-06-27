package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.entity.*;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public interface Saddleable {
    boolean isSaddleable();

    void equipSaddle(ItemStack saddle, @Nullable SoundSource soundSource);

    boolean isSaddled();
}
