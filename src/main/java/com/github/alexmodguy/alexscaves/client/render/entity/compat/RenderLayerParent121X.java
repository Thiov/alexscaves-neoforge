package com.github.alexmodguy.alexscaves.client.render.entity.compat;

import com.github.alexthe666.citadel.client.model.basic.BasicEntityModel;
import net.minecraft.world.entity.LivingEntity;

public interface RenderLayerParent121X<T extends LivingEntity, M extends BasicEntityModel<T>> {
    M getModel();
}
