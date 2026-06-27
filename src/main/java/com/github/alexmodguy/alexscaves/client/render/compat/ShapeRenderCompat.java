package com.github.alexmodguy.alexscaves.client.render.compat;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.world.phys.AABB;

/**
 * 26.1 dropped {@code ShapeRenderer#renderLineBox(PoseStack, VertexConsumer, AABB, float, float, float, float)}
 * (only {@code renderShape} remains). This reimplements the classic 12-edge line box so the mod's
 * magnet-range visualization keeps working with the {@code net.minecraft.client.renderer.rendertype.RenderTypes.lines()} consumer (which needs
 * a per-vertex normal).
 */
public final class ShapeRenderCompat {

    private ShapeRenderCompat() {
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, AABB box,
            float r, float g, float b, float a) {
        renderLineBox(poseStack, consumer, box.minX, box.minY, box.minZ, box.maxX, box.maxY, box.maxZ, r, g, b, a);
    }

    public static void renderLineBox(PoseStack poseStack, VertexConsumer consumer, double minX, double minY, double minZ,
            double maxX, double maxY, double maxZ, float r, float g, float b, float a) {
        PoseStack.Pose pose = poseStack.last();
        float fMinX = (float) minX;
        float fMinY = (float) minY;
        float fMinZ = (float) minZ;
        float fMaxX = (float) maxX;
        float fMaxY = (float) maxY;
        float fMaxZ = (float) maxZ;

        // Bottom edges
        line(consumer, pose, fMinX, fMinY, fMinZ, fMaxX, fMinY, fMinZ, r, g, b, a, 1, 0, 0);
        line(consumer, pose, fMinX, fMinY, fMinZ, fMinX, fMaxY, fMinZ, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fMinX, fMinY, fMinZ, fMinX, fMinY, fMaxZ, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fMaxX, fMinY, fMinZ, fMaxX, fMaxY, fMinZ, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fMaxX, fMinY, fMinZ, fMaxX, fMinY, fMaxZ, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fMinX, fMaxY, fMinZ, fMaxX, fMaxY, fMinZ, r, g, b, a, 1, 0, 0);
        line(consumer, pose, fMinX, fMaxY, fMinZ, fMinX, fMaxY, fMaxZ, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fMaxX, fMaxY, fMinZ, fMaxX, fMaxY, fMaxZ, r, g, b, a, 0, 0, 1);
        line(consumer, pose, fMinX, fMinY, fMaxZ, fMaxX, fMinY, fMaxZ, r, g, b, a, 1, 0, 0);
        line(consumer, pose, fMinX, fMinY, fMaxZ, fMinX, fMaxY, fMaxZ, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fMaxX, fMinY, fMaxZ, fMaxX, fMaxY, fMaxZ, r, g, b, a, 0, 1, 0);
        line(consumer, pose, fMinX, fMaxY, fMaxZ, fMaxX, fMaxY, fMaxZ, r, g, b, a, 1, 0, 0);
    }

    private static void line(VertexConsumer consumer, PoseStack.Pose pose, float x1, float y1, float z1,
            float x2, float y2, float z2, float r, float g, float b, float a, float nx, float ny, float nz) {
        consumer.addVertex(pose, x1, y1, z1).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
        consumer.addVertex(pose, x2, y2, z2).setColor(r, g, b, a).setNormal(pose, nx, ny, nz);
    }
}
