package com.github.alexmodguy.alexscaves.mixin;

import com.github.alexmodguy.alexscaves.server.level.biome.BiomeSourceAccessor;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

@Mixin(BiomeSource.class)
public class BiomeSourceMixin implements BiomeSourceAccessor {

    @Shadow
    public Supplier<Set<Holder<Biome>>> possibleBiomes;
    private boolean expanded;
    private Map<ResourceKey<Biome>, Holder<Biome>> map = new HashMap<>();

    
    public void setResourceKeyMap(Map<ResourceKey<Biome>, Holder<Biome>> map) {
        this.map = map;
    }

    
    public Map<ResourceKey<Biome>, Holder<Biome>> getResourceKeyMap() {
        return map;
    }

    
    public void expandBiomesWith(Set<Holder<Biome>> newGenBiomes) {
        if(!expanded){
            ImmutableSet.Builder<Holder<Biome>> builder = ImmutableSet.builder();
            builder.addAll(this.possibleBiomes.get());
            builder.addAll(newGenBiomes);
            possibleBiomes = Suppliers.memoize(builder::build);
            expanded = true;
        }
    }

}
