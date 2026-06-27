package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderType;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Captures legacy immediate-mode rendering (pre-1.21.5 MultiBufferSource style) into
 * CPU-side mesh buffers, then replays each captured mesh through the 26.1 submit
 * pipeline via {@link SubmitNodeCollector#submitCustomGeometry}.
 */
public class SubmitNodeBufferSource implements MultiBufferSource {

    private final Map<RenderType, BufferBuilder> builders = new LinkedHashMap<>();
    private final Map<RenderType, ByteBufferBuilder> memory = new LinkedHashMap<>();

    // The live submit pipeline that this capture eventually flushes into. Held so that
    // compat helpers (block/item rendering) can route directly to the collector instead of
    // forcing every draw through captured CPU meshes.
    private SubmitNodeCollector liveCollector;
    private PoseStack basePose;

    public void bindLive(SubmitNodeCollector collector, PoseStack basePose) {
        this.liveCollector = collector;
        this.basePose = basePose;
    }

    public SubmitNodeCollector liveCollector() {
        return liveCollector;
    }

    public PoseStack basePose() {
        return basePose;
    }

    public VertexConsumer getBuffer(RenderType renderType) {
        return builders.computeIfAbsent(renderType, type -> {
            ByteBufferBuilder bytes = new ByteBufferBuilder(Math.max(786, type.bufferSize() / 16));
            memory.put(type, bytes);
            return new BufferBuilder(bytes, type.mode(), type.format());
        });
    }

    public void flushInto(SubmitNodeCollector collector, PoseStack poseStack) {
        builders.forEach((type, builder) -> {
            MeshData mesh = builder.build();
            ByteBufferBuilder bytes = memory.get(type);
            if (mesh == null) {
                if (bytes != null) {
                    bytes.close();
                }
                return;
            }
            collector.submitCustomGeometry(poseStack, type, (pose, consumer) -> {
                replay(mesh, consumer);
                mesh.close();
                if (bytes != null) {
                    bytes.close();
                }
            });
        });
        builders.clear();
        memory.clear();
    }

    private static void replay(MeshData mesh, VertexConsumer consumer) {
        MeshData.DrawState drawState = mesh.drawState();
        VertexFormat format = drawState.format();
        int stride = format.getVertexSize();
        ByteBuffer vertexData = mesh.vertexBuffer().duplicate().order(ByteOrder.nativeOrder());
        boolean hasColor = format.contains(VertexFormatElement.COLOR);
        boolean hasUv0 = format.contains(VertexFormatElement.UV0);
        boolean hasUv1 = format.contains(VertexFormatElement.UV1);
        boolean hasUv2 = format.contains(VertexFormatElement.UV2);
        boolean hasNormal = format.contains(VertexFormatElement.NORMAL);
        int posOffset = format.getOffset(VertexFormatElement.POSITION);
        int colorOffset = hasColor ? format.getOffset(VertexFormatElement.COLOR) : 0;
        int uv0Offset = hasUv0 ? format.getOffset(VertexFormatElement.UV0) : 0;
        int uv1Offset = hasUv1 ? format.getOffset(VertexFormatElement.UV1) : 0;
        int uv2Offset = hasUv2 ? format.getOffset(VertexFormatElement.UV2) : 0;
        int normalOffset = hasNormal ? format.getOffset(VertexFormatElement.NORMAL) : 0;
        for (int i = 0; i < drawState.vertexCount(); i++) {
            int base = i * stride;
            consumer.addVertex(vertexData.getFloat(base + posOffset), vertexData.getFloat(base + posOffset + 4), vertexData.getFloat(base + posOffset + 8));
            if (hasColor) {
                consumer.setColor(vertexData.get(base + colorOffset) & 0xFF, vertexData.get(base + colorOffset + 1) & 0xFF, vertexData.get(base + colorOffset + 2) & 0xFF, vertexData.get(base + colorOffset + 3) & 0xFF);
            }
            if (hasUv0) {
                consumer.setUv(vertexData.getFloat(base + uv0Offset), vertexData.getFloat(base + uv0Offset + 4));
            }
            if (hasUv1) {
                consumer.setUv1(vertexData.getShort(base + uv1Offset), vertexData.getShort(base + uv1Offset + 2));
            }
            if (hasUv2) {
                consumer.setUv2(vertexData.getShort(base + uv2Offset), vertexData.getShort(base + uv2Offset + 2));
            }
            if (hasNormal) {
                consumer.setNormal(vertexData.get(base + normalOffset) / 127.0F, vertexData.get(base + normalOffset + 1) / 127.0F, vertexData.get(base + normalOffset + 2) / 127.0F);
            }
        }
    }
}
