package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public class MobCompat {

    private MobCompat() {
    }

    public static void clearRestriction(Mob mob) {
        mob.setHomeTo(mob.blockPosition(), -1);
    }

    public static boolean hasRestriction(Mob mob) {
        return mob.getHomeRadius() >= 0;
    }

    public static BlockPos getRestrictCenter(Mob mob) {
        return mob.getHomePosition();
    }

    public static <T extends Mob> T convertTo(Mob mob, EntityType<T> entityType, boolean keepEquipment) {
        return ((Mob) mob).convertTo(entityType, ConversionParams.single(mob, keepEquipment, true), converted -> {
        });
    }
}
