package com.github.alexmodguy.alexscaves.mcshim;
import net.minecraft.client.renderer.*;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.server.packs.resources.ResourceProvider;

public class ShaderInstance {
    private final String name;
    private final VertexFormat vertexFormat;

    public ShaderInstance(ResourceProvider resourceProvider, String name, VertexFormat vertexFormat) {
        this.name = name;
        this.vertexFormat = vertexFormat;
    }

    public String getName() {
        return name;
    }

    public VertexFormat getVertexFormat() {
        return vertexFormat;
    }
}
