package com.github.alexmodguy.alexscaves.client.sound;

import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import net.minecraft.core.Holder;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;

import java.util.List;

public class ACMusics {
    public static final Music LUXTRUCTOSAURUS_BOSS_MUSIC = new Music(Holder.direct(ACSoundRegistry.LUXTRUCTOSAURUS_BOSS_MUSIC.get()),
            0, 0, true);

    // Cached per-biome cave tracks: Music is a record, so reusing ONE instance per biome makes
    // MusicManager.isPlayingMusic/stopPlaying equality checks reliable. minDelay=0 -> prompt start on cave
    // entry; replaceCurrentMusic=true -> interrupts whatever overworld track was playing.
    public static final Music MAGNETIC_CAVES_MUSIC = caveMusic(ACSoundRegistry.MAGNETIC_CAVES_MUSIC.get());
    public static final Music PRIMORDIAL_CAVES_MUSIC = caveMusic(ACSoundRegistry.PRIMORDIAL_CAVES_MUSIC.get());
    public static final Music TOXIC_CAVES_MUSIC = caveMusic(ACSoundRegistry.TOXIC_CAVES_MUSIC.get());
    public static final Music ABYSSAL_CHASM_MUSIC = caveMusic(ACSoundRegistry.ABYSSAL_CHASM_MUSIC.get());
    public static final Music FORLORN_HOLLOWS_MUSIC = caveMusic(ACSoundRegistry.FORLORN_HOLLOWS_MUSIC.get());
    public static final Music CANDY_CAVITY_MUSIC = caveMusic(ACSoundRegistry.CANDY_CAVITY_MUSIC.get());

    // Every AC-forced track, for the "player left the cave" cleanup (teleport out, death respawn outside,
    // dimension change): vanilla music has replaceCurrentMusic=false so it never stops a lingering AC track
    // on its own — the getSituationalMusic override stops these explicitly when no longer applicable.
    public static final List<Music> ALL_FORCED_MUSICS = List.of(
            MAGNETIC_CAVES_MUSIC, PRIMORDIAL_CAVES_MUSIC, TOXIC_CAVES_MUSIC,
            ABYSSAL_CHASM_MUSIC, FORLORN_HOLLOWS_MUSIC, CANDY_CAVITY_MUSIC,
            LUXTRUCTOSAURUS_BOSS_MUSIC);

    private static Music caveMusic(SoundEvent sound) {
        return new Music(Holder.direct(sound), 0, 24000, true);
    }
}
