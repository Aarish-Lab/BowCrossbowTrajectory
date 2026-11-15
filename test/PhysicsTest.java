import com.bowcrossbow.trajectory.*;

public class PhysicsTest {
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); System.out.println("PASS " + m); }

    public static void main(String[] a) {
        // flat ground at y=0, a "player" box at x=20..31
        CollisionProbe world = new CollisionProbe() {
            public int raycast(double x0,double y0,double z0,double x1,double y1,double z1,double[] o){
                // entity slab x in [20,21], |z|<0.5, y in [0,2]
                if (x0 < 20 && x1 >= 20 && Math.abs(z0) < 0.5) {
                    double t = (20 - x0) / (x1 - x0), y = y0 + (y1 - y0) * t;
                    if (y >= 0 && y <= 2) { o[0]=20; o[1]=y; o[2]=z0; return PLAYER; }
                }
                if (y1 <= 0 && y0 > 0) { double t = y0 / (y0 - y1); o[0]=x0+(x1-x0)*t; o[1]=0; o[2]=z0+(z1-z0)*t; return BLOCK; }
                return NONE;
            }
            public boolean inWater(double x,double y,double z){ return false; }
            public double minY(){ return -64; }
        };
        check(TrajectoryCalculator.bowSpeed(20) == 3.0, "full charge speed is 3.0");
        check(TrajectoryCalculator.bowSpeed(1) == 0.0, "1 tick is below the 0.1 release threshold");
        check(Math.abs(TrajectoryCalculator.bowSpeed(10) - 1.25) < 1e-9, "half charge speed is 1.25");

        TrajectoryCalculator.Path p = new TrajectoryCalculator.Path();
        // level shot at full charge from eye height 1.52 towards +x
        TrajectoryCalculator.simulate(world, 0, 1.52, 0, 3.0, 0, 0, 200, 128, p);
        check(p.hitType == CollisionProbe.PLAYER, "level full-charge arrow hits the entity at x=20 (type " + p.hitType + ")");
        check(Math.abs(p.hitX - 20) < 1e-9, "hit x is 20");
        check(p.hitY < 1.52 && p.hitY > 0, "arrow dropped before reaching it: y=" + p.hitY);

        // shot upwards must hit ground further away than the entity plane check
        TrajectoryCalculator.simulate(world, 0, 1.52, 0, 3.0 * Math.cos(Math.toRadians(30)), 3.0 * Math.sin(Math.toRadians(30)), 0.0, 400, 400, p);
        check(p.hitType == CollisionProbe.NONE || p.hitType == CollisionProbe.BLOCK, "30 degree shot clears the entity or lands");
        System.out.println("   landing x=" + p.hitX + " ticks=" + (p.count - 1));

        // multishot: three directions, side arrows 10 degrees apart from centre about the up axis
        double[] c = {1, 0, 0}, l = new double[3], r = new double[3];
        MultishotHandler.rotate(1, 0, 0, 0, 1, 0, MultishotHandler.angleDegrees(1), l);
        MultishotHandler.rotate(1, 0, 0, 0, 1, 0, MultishotHandler.angleDegrees(2), r);
        double angL = Math.toDegrees(Math.acos(c[0]*l[0]+c[1]*l[1]+c[2]*l[2]));
        double angR = Math.toDegrees(Math.acos(c[0]*r[0]+c[1]*r[1]+c[2]*r[2]));
        check(Math.abs(angL - 10) < 1e-9 && Math.abs(angR - 10) < 1e-9, "side arrows are 10 degrees from centre");
        check(Math.abs(l[2] + r[2]) < 1e-12 && l[2] * r[2] < 0, "side arrows are mirrored left/right");
        check(Math.abs(Math.sqrt(l[0]*l[0]+l[1]*l[1]+l[2]*l[2]) - 1) < 1e-12, "rotation preserves length");
        check(MultishotHandler.projectileCount(true) == 3 && MultishotHandler.projectileCount(false) == 1, "projectile counts 3 / 1");

        // independent collisions: left/right arrows (rotated about up) must miss the |z|<0.5 entity at x=20
        for (int i = 0; i < 3; i++) {
            double[] d = new double[3];
            MultishotHandler.rotate(3.0, 0, 0, 0, 1, 0, MultishotHandler.angleDegrees(i), d);
            TrajectoryCalculator.simulate(world, 0, 1.52, 0, d[0], d[1], d[2], 200, 128, p);
            System.out.println("   arrow " + i + " -> hitType " + p.hitType);
            if (i == 0) check(p.hitType == CollisionProbe.PLAYER, "centre arrow hits entity");
            else check(p.hitType == CollisionProbe.BLOCK, "side arrow " + i + " misses entity and lands on ground");
        }
    }
}
