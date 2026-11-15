package com.bowcrossbow.trajectory;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

/** Draws each arrow's path as a Catmull-Rom smoothed line strip, coloured by its predicted collision. */
public final class TrajectoryRenderer {
    private TrajectoryRenderer() {}

    public static void register() {
        LevelRenderEvents.AFTER_ENTITIES.register(TrajectoryRenderer::render);
    }

    private static void render(LevelRenderContext ctx) {
        Minecraft mc = Minecraft.getInstance();
        float partial = mc.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        TrajectoryManager.update(mc, partial);
        if (!TrajectoryManager.active) return;

        ModConfiguration cfg = ModConfiguration.get();
        Vec3 cam = mc.gameRenderer.getMainCamera().position();
        PoseStack.Pose pose = ctx.poseStack().last();
        MultiBufferSource src = ctx.bufferSource();
        RenderType type = RenderTypes.lines();
        VertexConsumer vc = src.getBuffer(type);

        for (int a = 0; a < TrajectoryManager.arrowCount; a++) {
            TrajectoryCalculator.Path p = TrajectoryManager.PATHS[a];
            if (p.count < 2) continue;
            int argb = 0xFF000000 | colourFor(cfg, p.hitType);
            drawCurve(vc, pose, p, cam, argb, cfg.thickness, cfg.quality);
            if (cfg.showCollisionIndicators && p.hitType != CollisionProbe.NONE) {
                double s = 0.25;
                float w = cfg.thickness + 0.5f;
                line(vc, pose, p.hitX - s, p.hitY, p.hitZ, p.hitX + s, p.hitY, p.hitZ, cam, argb, w);
                line(vc, pose, p.hitX, p.hitY - s, p.hitZ, p.hitX, p.hitY + s, p.hitZ, cam, argb, w);
                line(vc, pose, p.hitX, p.hitY, p.hitZ - s, p.hitX, p.hitY, p.hitZ + s, cam, argb, w);
            }
        }
        if (src instanceof MultiBufferSource.BufferSource bs) bs.endBatch(type);
    }

    private static int colourFor(ModConfiguration c, int hit) {
        if (!c.collisionColours) return c.colourDefault;
        return switch (hit) {
            case CollisionProbe.PLAYER -> c.colourPlayer;
            case CollisionProbe.CRYSTAL -> c.colourCrystal;
            case CollisionProbe.ENTITY -> c.colourEntity;
            case CollisionProbe.BLOCK -> c.colourBlock;
            default -> c.colourDefault;
        };
    }

    private static void drawCurve(VertexConsumer vc, PoseStack.Pose pose, TrajectoryCalculator.Path p,
                                  Vec3 cam, int argb, float width, int quality) {
        double[] v = p.xyz;
        int n = p.count;
        double px = v[0], py = v[1], pz = v[2];
        for (int i = 0; i < n - 1; i++) {
            int i0 = Math.max(i - 1, 0) * 3, i1 = i * 3, i2 = (i + 1) * 3, i3 = Math.min(i + 2, n - 1) * 3;
            for (int j = 1; j <= quality; j++) {
                double t = (double) j / quality, t2 = t * t, t3 = t2 * t;
                double x = cr(v[i0], v[i1], v[i2], v[i3], t, t2, t3);
                double y = cr(v[i0 + 1], v[i1 + 1], v[i2 + 1], v[i3 + 1], t, t2, t3);
                double z = cr(v[i0 + 2], v[i1 + 2], v[i2 + 2], v[i3 + 2], t, t2, t3);
                line(vc, pose, px, py, pz, x, y, z, cam, argb, width);
                px = x; py = y; pz = z;
            }
        }
    }

    private static double cr(double p0, double p1, double p2, double p3, double t, double t2, double t3) {
        return 0.5 * ((2 * p1) + (-p0 + p2) * t + (2 * p0 - 5 * p1 + 4 * p2 - p3) * t2 + (-p0 + 3 * p1 - 3 * p2 + p3) * t3);
    }

    private static void line(VertexConsumer vc, PoseStack.Pose pose, double ax, double ay, double az,
                             double bx, double by, double bz, Vec3 cam, int argb, float width) {
        float x0 = (float) (ax - cam.x), y0 = (float) (ay - cam.y), z0 = (float) (az - cam.z);
        float x1 = (float) (bx - cam.x), y1 = (float) (by - cam.y), z1 = (float) (bz - cam.z);
        float dx = x1 - x0, dy = y1 - y0, dz = z1 - z0;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len < 1.0e-6f) return;
        dx /= len; dy /= len; dz /= len;
        vc.addVertex(pose, x0, y0, z0).setColor(argb).setNormal(pose, dx, dy, dz).setLineWidth(width);
        vc.addVertex(pose, x1, y1, z1).setColor(argb).setNormal(pose, dx, dy, dz).setLineWidth(width);
    }
}
