package com.bowcrossbow.trajectory;

/** World access used by the physics code. Keeping this an interface keeps TrajectoryCalculator free of Minecraft classes (unit-testable). */
public interface CollisionProbe {
    int NONE = 0, BLOCK = 1, ENTITY = 2, PLAYER = 3, CRYSTAL = 4;

    /** Sweeps the segment; returns a hit type constant and writes the hit position into hitOut[0..2]. */
    int raycast(double x0, double y0, double z0, double x1, double y1, double z1, double[] hitOut);

    boolean inWater(double x, double y, double z);

    /** Lowest Y at which the projectile is considered lost (void). */
    double minY();
}
