package com.bowcrossbow.trajectory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;

/** All settings. Edited live from the Mod Menu screen, saved automatically as JSON in the config folder. */
public final class ModConfiguration {
    // General
    public boolean enabled = true;
    public boolean bowPrediction = true;
    public boolean crossbowPrediction = true;
    public boolean multishotPrediction = true;
    public boolean collisionColours = true;
    // Colours (0xRRGGBB)
    public int colourDefault = 0xFFFFFF;
    public int colourPlayer = 0xFF4040;
    public int colourEntity = 0xFFAA00;
    public int colourCrystal = 0xFF55FF;
    public int colourBlock = 0x55FF55;
    // Rendering
    public float thickness = 2.5f;
    public boolean showTrajectory = true;
    public boolean showCollisionIndicators = true;
    public int quality = 3;                 // curve subdivisions per tick segment
    // Prediction
    public int predictionDistance = 128;
    public int maxTicks = 200;
    public int updateEveryNFrames = 1;      // 1 = recompute every frame (most precise)
    public boolean detectBlocks = true;
    public boolean detectPlayers = true;
    public boolean detectEntities = true;
    public boolean detectCrystals = true;
    public float entityMargin = 0.3f;       // vanilla arrow hitbox inflation

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("bowcrossbowtrajectory.json");
    private static ModConfiguration instance = new ModConfiguration();

    public static ModConfiguration get() { return instance; }

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                ModConfiguration c = GSON.fromJson(Files.readString(FILE), ModConfiguration.class);
                if (c != null) instance = c;
            }
        } catch (Exception e) {
            System.err.println("[BowCrossbowTrajectory] Could not read config, using defaults: " + e);
        }
        instance.clamp();
        instance.save();
    }

    public void save() {
        try {
            clamp();
            Files.writeString(FILE, GSON.toJson(this));
        } catch (Exception e) {
            System.err.println("[BowCrossbowTrajectory] Could not save config: " + e);
        }
    }

    /** Restores defaults in place so every holder of get() sees the change immediately. */
    public void reset() {
        ModConfiguration d = new ModConfiguration();
        try {
            for (Field f : ModConfiguration.class.getDeclaredFields()) {
                if (Modifier.isStatic(f.getModifiers())) continue;
                f.set(this, f.get(d));
            }
        } catch (IllegalAccessException e) { throw new IllegalStateException(e); }
        save();
    }

    private void clamp() {
        thickness = Math.max(0.5f, Math.min(8f, thickness));
        quality = Math.max(1, Math.min(6, quality));
        predictionDistance = Math.max(16, Math.min(256, predictionDistance));
        maxTicks = Math.max(20, Math.min(TrajectoryCalculator.MAX_TICKS_LIMIT, maxTicks));
        updateEveryNFrames = Math.max(1, Math.min(6, updateEveryNFrames));
        entityMargin = Math.max(0f, Math.min(1f, entityMargin));
    }
}
