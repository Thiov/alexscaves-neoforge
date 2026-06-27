package com.github.alexmodguy.alexscaves.client.render.entity;

import com.github.alexmodguy.alexscaves.AlexsCaves;
import com.github.alexmodguy.alexscaves.server.entity.item.SeekingArrowEntity;
import com.github.alexmodguy.alexscaves.client.render.entity.compat.ArrowRenderer121X;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

public class SeekingArrowRenderer extends ArrowRenderer121X<SeekingArrowEntity> {

    public static final Identifier TEXTURE = Identifier.fromNamespaceAndPath(AlexsCaves.MODID, "textures/entity/seeking_arrow.png");

    public SeekingArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    protected int getBlockLightLevel(SeekingArrowEntity entity, BlockPos pos) {
        return 15;
    }


    public Identifier getTextureLocation(SeekingArrowEntity entity) {
        return TEXTURE;
    }
}
