package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.world.*;

public final class InteractionResultHolder<T> {
    private final InteractionResult result;
    private final T object;

    public InteractionResultHolder(InteractionResult result, T object) {
        this.result = result;
        this.object = object;
    }

    public static <T> InteractionResultHolder<T> pass(T object) {
        return new InteractionResultHolder<>(InteractionResult.PASS, object);
    }

    public static <T> InteractionResultHolder<T> consume(T object) {
        return new InteractionResultHolder<>(InteractionResult.CONSUME, object);
    }

    public static <T> InteractionResultHolder<T> fail(T object) {
        return new InteractionResultHolder<>(InteractionResult.FAIL, object);
    }

    public static <T> InteractionResultHolder<T> success(T object) {
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, object);
    }

    public static <T> InteractionResultHolder<T> sidedSuccess(T object, boolean clientSide) {
        return new InteractionResultHolder<>(clientSide ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER, object);
    }

    public InteractionResult result() {
        return result;
    }

    public T object() {
        return object;
    }
}
