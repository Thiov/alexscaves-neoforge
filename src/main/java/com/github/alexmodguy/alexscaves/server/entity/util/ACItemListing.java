package com.github.alexmodguy.alexscaves.server.entity.util;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.trading.MerchantOffer;

/**
 * Stand-in for the removed {@code com.github.alexmodguy.alexscaves.server.entity.util.ACItemListing} functional interface. 26.1 redesigned
 * villager trades into data-driven VillagerTrade/TradeSet, but AC builds MerchantOffers dynamically,
 * so the original factory shape is preserved here. Trade registration into vanilla villagers is no
 * longer wired (Fabric removed TradeOfferHelper) — these listings are invoked by AC's own entities.
 */
@FunctionalInterface
public interface ACItemListing {
    MerchantOffer getOffer(Entity entity, RandomSource randomSource);
}
