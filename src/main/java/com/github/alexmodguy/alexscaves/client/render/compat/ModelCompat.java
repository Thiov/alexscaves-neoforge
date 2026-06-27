package com.github.alexmodguy.alexscaves.client.render.compat;

import net.minecraft.client.model.HumanoidModel;

import java.lang.reflect.Field;

public class ModelCompat {

    private ModelCompat() {
    }

    public static boolean isHumanoid(Object model) {
        return model instanceof HumanoidModel<?>;
    }

    public static boolean setHumanoidCrouching(Object model, boolean crouching) {
        if (model instanceof HumanoidModel<?> humanoidModel) {
            try {
                Field crouchingField = HumanoidModel.class.getField("crouching");
                boolean previous = crouchingField.getBoolean(humanoidModel);
                crouchingField.setBoolean(humanoidModel, crouching);
                return previous;
            } catch (ReflectiveOperationException ignored) {
                return false;
            }
        }
        return false;
    }
}
