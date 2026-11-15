package com.bowcrossbow.trajectory;

/**
 * Vanilla Multishot: three projectiles are fired. Their directions are the player's view vector rotated about the
 * player's up vector by 0, -10 and +10 degrees (CrossbowItem.shoot). Pure maths, no Minecraft dependency.
 */
public final class MultishotHandler {
    public static final double SPREAD_DEGREES = 10.0;

    private MultishotHandler() {}

    public static int projectileCount(boolean multishot) { return multishot ? 3 : 1; }

    /** Index 0 = centre, 1 = -10 degrees, 2 = +10 degrees. */
    public static double angleDegrees(int index) {
        return index == 1 ? -SPREAD_DEGREES : index == 2 ? SPREAD_DEGREES : 0.0;
    }

    /** Rodrigues rotation of (dx,dy,dz) about unit axis (ax,ay,az). */
    public static void rotate(double dx, double dy, double dz, double ax, double ay, double az, double degrees, double[] out) {
        double len = Math.sqrt(ax * ax + ay * ay + az * az);
        ax /= len; ay /= len; az /= len;
        double th = Math.toRadians(degrees), c = Math.cos(th), s = Math.sin(th);
        double dot = ax * dx + ay * dy + az * dz;
        double cx = ay * dz - az * dy, cy = az * dx - ax * dz, cz = ax * dy - ay * dx;
        out[0] = dx * c + cx * s + ax * dot * (1 - c);
        out[1] = dy * c + cy * s + ay * dot * (1 - c);
        out[2] = dz * c + cz * s + az * dot * (1 - c);
    }
}
