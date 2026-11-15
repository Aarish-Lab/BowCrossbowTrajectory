package com.bowcrossbow.trajectory;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/** Block (collision shape) and entity (bounding box) sweeps; the nearest hit along each tick segment wins. */
public final class CollisionDetector implements CollisionProbe {
    private Level level;
    private Entity shooter;
    private ModConfiguration cfg;
    private List<Entity> candidates = Collections.emptyList();
    private long lastRefreshTick = Long.MIN_VALUE;

    /** Candidate entities are gathered once per game tick, not per raycast. */
    public void begin(Level level, Entity shooter, ModConfiguration cfg) {
        this.level = level;
        this.shooter = shooter;
        this.cfg = cfg;
        long tick = level.getGameTime();
        if (tick != lastRefreshTick) {
            lastRefreshTick = tick;
            AABB box = shooter.getBoundingBox().inflate(cfg.predictionDistance);
            candidates = level.getEntities(shooter, box, e -> e.isAlive() && !e.isSpectator() && e.isPickable());
        }
    }

    @Override
    public int raycast(double x0, double y0, double z0, double x1, double y1, double z1, double[] hitOut) {
        Vec3 from = new Vec3(x0, y0, z0);
        Vec3 to = new Vec3(x1, y1, z1);
        double best = Double.MAX_VALUE;
        int type = NONE;
        Vec3 bestPos = null;

        if (cfg.detectBlocks) {
            BlockHitResult br = level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter));
            if (br.getType() != HitResult.Type.MISS) {
                bestPos = br.getLocation();
                best = from.distanceToSqr(bestPos);
                type = BLOCK;
            }
        }
        double margin = cfg.entityMargin;
        for (int i = 0, n = candidates.size(); i < n; i++) {
            Entity e = candidates.get(i);
            int kind;
            if (e instanceof Player) { if (!cfg.detectPlayers) continue; kind = PLAYER; }
            else if (e instanceof EndCrystal) { if (!cfg.detectCrystals) continue; kind = CRYSTAL; }
            else { if (!cfg.detectEntities) continue; kind = ENTITY; }
            Optional<Vec3> hit = e.getBoundingBox().inflate(margin).clip(from, to);
            if (hit.isPresent()) {
                double d = from.distanceToSqr(hit.get());
                if (d < best) { best = d; bestPos = hit.get(); type = kind; }
            }
        }
        if (type != NONE) {
            hitOut[0] = bestPos.x; hitOut[1] = bestPos.y; hitOut[2] = bestPos.z;
        }
        return type;
    }

    @Override
    public boolean inWater(double x, double y, double z) {
        return level.getFluidState(BlockPos.containing(x, y, z)).is(FluidTags.WATER);
    }

    @Override
    public double minY() { return level.getMinY() - 16; }
}
