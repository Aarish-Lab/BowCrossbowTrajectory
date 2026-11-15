package com.bowcrossbow.trajectory;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.Vec3;

/** Orchestrates handlers, Multishot directions and per-arrow simulation. Holds preallocated results for the renderer. */
public final class TrajectoryManager {
    public static final class Shot { public double speed; public double yOffset; public boolean multishot; }

    public static final TrajectoryCalculator.Path[] PATHS = {
            new TrajectoryCalculator.Path(), new TrajectoryCalculator.Path(), new TrajectoryCalculator.Path() };
    public static int arrowCount;
    public static boolean active;

    private static final Shot SHOT = new Shot();
    private static final CollisionDetector DETECTOR = new CollisionDetector();
    private static final double[] DIR = new double[3];
    private static boolean computed;
    private static int frame;

    private TrajectoryManager() {}

    public static void update(Minecraft mc, float partialTick) {
        ModConfiguration cfg = ModConfiguration.get();
        LocalPlayer player = mc.player;
        ClientLevel level = mc.level;
        if (!cfg.enabled || !cfg.showTrajectory || player == null || level == null) { active = false; computed = false; return; }
        if (!BowHandler.query(player, partialTick, SHOT) && !CrossbowHandler.query(player, SHOT)) { active = false; computed = false; return; }
        active = true;
        if (computed && (++frame % cfg.updateEveryNFrames) != 0) return;

        Vec3 eye = player.getEyePosition(partialTick);
        Vec3 look = player.getViewVector(partialTick);
        double yaw = Math.toRadians(player.getViewYRot(partialTick));
        double pitch = Math.toRadians(player.getViewXRot(partialTick));
        // Up vector = view vector with pitch reduced by 90 degrees (Entity.getUpVector)
        double upX = -Math.sin(yaw) * Math.sin(pitch), upY = Math.cos(pitch), upZ = Math.cos(yaw) * Math.sin(pitch);
        Vec3 move = player.getDeltaMovement();
        double mvx = move.x, mvy = player.onGround() ? 0.0 : move.y, mvz = move.z;

        DETECTOR.begin(level, player, cfg);
        arrowCount = MultishotHandler.projectileCount(SHOT.multishot);
        for (int i = 0; i < arrowCount; i++) {
            MultishotHandler.rotate(look.x, look.y, look.z, upX, upY, upZ, MultishotHandler.angleDegrees(i), DIR);
            TrajectoryCalculator.simulate(DETECTOR,
                    eye.x, eye.y - SHOT.yOffset, eye.z,
                    DIR[0] * SHOT.speed + mvx, DIR[1] * SHOT.speed + mvy, DIR[2] * SHOT.speed + mvz,
                    cfg.maxTicks, cfg.predictionDistance, PATHS[i]);
        }
        computed = true;
    }
}
