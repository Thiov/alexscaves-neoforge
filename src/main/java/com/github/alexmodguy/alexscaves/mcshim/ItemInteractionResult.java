package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.*;

public enum ItemInteractionResult {
    SUCCESS,
    PASS_TO_DEFAULT_BLOCK_INTERACTION,
    PASS,
    CONSUME;

    public static ItemInteractionResult sidedSuccess(boolean clientSide) {
        return SUCCESS;
    }
}
