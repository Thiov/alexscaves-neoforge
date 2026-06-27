package com.github.alexmodguy.alexscaves.client.render.entity.layer;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.client.model.GumbeeperModel;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.EnergySwirlLayer121X;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.RenderLayerParent121X;
import com.github.alexmodguy.alexscaves.server.entity.living.GumbeeperEntity;
import net.minecraft.resources.Identifier;

public class GumbeeperEnergySwirlLayer extends EnergySwirlLayer121X<GumbeeperEntity, GumbeeperModel> {
    private static final Identifier POWER_LOCATION = Identifier.fromNamespaceAndPath(AlexsCaves.MODID,
            "textures/entity/gumbeeper_charged.png");
    private final GumbeeperModel model = new GumbeeperModel(1.0F);

    public GumbeeperEnergySwirlLayer(RenderLayerParent121X<GumbeeperEntity, GumbeeperModel> renderer) {
        super(renderer);
    }

    protected float xOffset(float f) {
        return f * 0.01F;
    }

    protected Identifier getTextureLocation() {
        return POWER_LOCATION;
    }

    protected Object model() {
        return this.model;
    }
}
