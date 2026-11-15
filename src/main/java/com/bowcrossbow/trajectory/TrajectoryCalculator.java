package com.bowcrossbow.trajectory;

/**
 * Re-implementation of vanilla AbstractArrow motion: per tick the arrow moves by its velocity (stopping at the first
 * collision), then velocity is multiplied by 0.99 (0.6 in water) and gravity 0.05 is subtracted from Y.
 * Allocation-free: results are written into a reusable Path.
 */
public final class TrajectoryCalculator {
    public static final double GRAVITY = 0.05, AIR_DRAG = 0.99, WATER_DRAG = 0.6;
    public static final double CROSSBOW_ARROW_SPEED = 3.15;
    public static final int MAX_TICKS_LIMIT = 400;
    public static final int MAX_POINTS = MAX_TICKS_LIMIT + 2;

    private TrajectoryCalculator() {}

    public static final class Path {
        public final double[] xyz = new double[MAX_POINTS * 3];
        public final double[] hitBuf = new double[3];
        public int count;
        public int hitType = CollisionProbe.NONE;
        public double hitX, hitY, hitZ;

        public void clear() { count = 0; hitType = CollisionProbe.NONE; }

        void add(double x, double y, double z) {
            if (count >= MAX_POINTS) return;
            int i = count * 3;
            xyz[i] = x; xyz[i + 1] = y; xyz[i + 2] = z;
            count++;
        }
    }

    /** Vanilla BowItem.getPowerForTime * 3.0. Returns 0 when the bow could not be released yet (power < 0.1). */
    public static double bowSpeed(double chargeTicks) {
        double f = chargeTicks / 20.0;
        f = (f * f + f * 2.0) / 3.0;
        if (f > 1.0) f = 1.0;
        return f < 0.1 ? 0.0 : f * 3.0;
    }

    public static void simulate(CollisionProbe probe, double sx, double sy, double sz,
                                double vx, double vy, double vz,
                                int maxTicks, double maxDistance, Path out) {
        out.clear();
        if (vx * vx + vy * vy + vz * vz < 1.0e-8) return;
        if (maxTicks > MAX_TICKS_LIMIT) maxTicks = MAX_TICKS_LIMIT;
        double maxDistSq = maxDistance * maxDistance;
        double x = sx, y = sy, z = sz;
        out.add(x, y, z);
        for (int t = 0; t < maxTicks; t++) {
            double nx = x + vx, ny = y + vy, nz = z + vz;
            int hit = probe.raycast(x, y, z, nx, ny, nz, out.hitBuf);
            if (hit != CollisionProbe.NONE) {
                out.hitType = hit;
                out.hitX = out.hitBuf[0]; out.hitY = out.hitBuf[1]; out.hitZ = out.hitBuf[2];
                out.add(out.hitX, out.hitY, out.hitZ);
                return;
            }
            double drag = probe.inWater(x, y, z) ? WATER_DRAG : AIR_DRAG;
            x = nx; y = ny; z = nz;
            out.add(x, y, z);
            vx *= drag; vy *= drag; vz *= drag;
            vy -= GRAVITY;
            double dx = x - sx, dy = y - sy, dz = z - sz;
            if (dx * dx + dy * dy + dz * dz > maxDistSq || y < probe.minY()) return;
        }
    }
}
