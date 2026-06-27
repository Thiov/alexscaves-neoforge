package com.github.alexmodguy.alexscaves.client.sound;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;

public class ACMusics {
    public static final Music LUXTRUCTOSAURUS_BOSS_MUSIC = new Music(Holder.direct(ACSoundRegistry.LUXTRUCTOSAURUS_BOSS_MUSIC.get()),
            0, 0, true);
}
