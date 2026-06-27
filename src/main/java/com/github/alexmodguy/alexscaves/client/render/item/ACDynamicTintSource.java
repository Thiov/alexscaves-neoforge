package com.github.alexmodguy.alexscaves.client.render.item;

import com.github.alexmodguy.alexscaves.server.item.ACItemRegistry;
import com.github.alexmodguy.alexscaves.server.item.BiomeTreatItem;
import com.github.alexmodguy.alexscaves.server.item.CaveInfoItem;
import com.github.alexmodguy.alexscaves.server.item.GazingPearlItem;
import com.github.alexmodguy.alexscaves.server.item.JellyBeanItem;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * 26.1 made item tints data-driven via {@link ItemTintSource}s referenced in an item model's {@code tints}
 * array; the old imperative {@code RegisterColorHandlersEvent.Item} hook is a no-op on this Fabric API.
 * This single source ({@code alexscaves:dynamic}) reproduces AC's per-item dynamic colours (biome colour
 * for the cave tablet/codex/biome treat, the stored pearl/bean colour) by dispatching on the item.
 * Registered into {@code ItemTintSources.ID_MAPPER} by {@code mixin.client.ItemTintSourcesMixin}.
 */
public record ACDynamicTintSource() implements ItemTintSource {

    public static final MapCodec<ACDynamicTintSource> MAP_CODEC = MapCodec.unit(new ACDynamicTintSource());

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        Level useLevel = level != null ? level : Minecraft.getInstance().level;
        Item item = stack.getItem();
        int color;
        if (item == ACItemRegistry.CAVE_TABLET.get() || item == ACItemRegistry.CAVE_CODEX.get()) {
            if (useLevel == null) {
                return -1;
            }
            color = CaveInfoItem.getBiomeColorOf(useLevel, stack, false);
        } else if (item == ACItemRegistry.GAZING_PEARL.get()) {
            color = GazingPearlItem.getPearlColor(stack);
        } else if (item == ACItemRegistry.JELLY_BEAN.get()) {
            color = JellyBeanItem.getBeanColor(stack);
        } else if (item == ACItemRegistry.BIOME_TREAT.get()) {
            if (useLevel == null) {
                return -1;
            }
            color = BiomeTreatItem.getBiomeTreatColorOf(useLevel, stack);
        } else {
            return -1;
        }
        return color | 0xFF000000;
    }

    @Override
    public MapCodec<ACDynamicTintSource> type() {
        return MAP_CODEC;
    }
}
