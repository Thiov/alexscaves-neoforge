package com.github.alexthe666.citadel.item;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;

import java.util.function.Supplier;

public class BlockItemWithSupplier extends BlockItem {

    private final Supplier<Block> blockSupplier;

    public BlockItemWithSupplier(Supplier<Block> blockSupplier, Properties props) {
        // 26.1: block items must opt into the "block." translation-key prefix; without this they
        // resolve to "item.<ns>.<path>" (untranslated, since AC's lang only defines "block.<ns>.<path>").
        super(null, props.useBlockDescriptionPrefix());
        this.blockSupplier = blockSupplier;
    }

    
    public Block getBlock() {
        return blockSupplier.get();
    }

    public boolean canFitInsideContainerItems() {
        return !(blockSupplier.get() instanceof ShulkerBoxBlock);
    }
}
