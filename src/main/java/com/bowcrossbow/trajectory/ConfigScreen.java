package com.bowcrossbow.trajectory;

import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/** Mod Menu config screen. Four pages; every change is applied to the live config immediately and saved on close. */
public final class ConfigScreen extends Screen {
    private static final String[] PAGES = {"General", "Colours", "Rendering", "Prediction"};
    private static int lastPage = 0;

    private final Screen parent;
    private final ModConfiguration cfg = ModConfiguration.get();

    public ConfigScreen(Screen parent) {
        super(Component.literal("Bow & Crossbow Trajectory"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int titleWidth = this.font.width("Bow & Crossbow Trajectory");
        addRenderableWidget(new StringWidget(cx - titleWidth / 2, 8, titleWidth, 12, this.title, this.font));
        for (int i = 0; i < PAGES.length; i++) {
            final int page = i;
            Button b = Button.builder(Component.literal(PAGES[i]), btn -> { lastPage = page; rebuildWidgets(); })
                    .bounds(cx - 156 + i * 79, 24, 76, 20).build();
            b.active = (i != lastPage);
            addRenderableWidget(b);
        }
        int left = cx - 155, right = cx + 5, y = 56, step = 24;
        switch (lastPage) {
            case 0 -> {
                toggle(left, y, 310, "Mod enabled", () -> cfg.enabled, v -> cfg.enabled = v); y += step;
                toggle(left, y, 310, "Bow prediction", () -> cfg.bowPrediction, v -> cfg.bowPrediction = v); y += step;
                toggle(left, y, 310, "Crossbow prediction", () -> cfg.crossbowPrediction, v -> cfg.crossbowPrediction = v); y += step;
                toggle(left, y, 310, "Multishot prediction (3 arrows)", () -> cfg.multishotPrediction, v -> cfg.multishotPrediction = v); y += step;
                toggle(left, y, 310, "Collision colour changes", () -> cfg.collisionColours, v -> cfg.collisionColours = v);
            }
            case 1 -> {
                colour(left, y, "Default (no collision)", () -> cfg.colourDefault, v -> cfg.colourDefault = v); y += step;
                colour(left, y, "Player collision", () -> cfg.colourPlayer, v -> cfg.colourPlayer = v); y += step;
                colour(left, y, "Other entity collision", () -> cfg.colourEntity, v -> cfg.colourEntity = v); y += step;
                colour(left, y, "End Crystal collision", () -> cfg.colourCrystal, v -> cfg.colourCrystal = v); y += step;
                colour(left, y, "Block collision", () -> cfg.colourBlock, v -> cfg.colourBlock = v);
            }
            case 2 -> {
                slider(left, y, 310, "Thickness", 0.5, 8, 0.5, cfg.thickness, v -> cfg.thickness = (float) v); y += step;
                slider(left, y, 310, "Rendering quality (curve smoothness)", 1, 6, 1, cfg.quality, v -> cfg.quality = (int) v); y += step;
                toggle(left, y, 310, "Show trajectories", () -> cfg.showTrajectory, v -> cfg.showTrajectory = v); y += step;
                toggle(left, y, 310, "Show collision indicators", () -> cfg.showCollisionIndicators, v -> cfg.showCollisionIndicators = v);
            }
            default -> {
                slider(left, y, 310, "Prediction distance (blocks)", 16, 256, 8, cfg.predictionDistance, v -> cfg.predictionDistance = (int) v); y += step;
                slider(left, y, 310, "Maximum trajectory length (ticks)", 20, TrajectoryCalculator.MAX_TICKS_LIMIT, 10, cfg.maxTicks, v -> cfg.maxTicks = (int) v); y += step;
                slider(left, y, 310, "Simulation precision (recalculate every N frames)", 1, 6, 1, cfg.updateEveryNFrames, v -> cfg.updateEveryNFrames = (int) v); y += step;
                slider(left, y, 310, "Entity hitbox margin", 0, 1, 0.05, cfg.entityMargin, v -> cfg.entityMargin = (float) v); y += step;
                toggle(left, y, 150, "Blocks", () -> cfg.detectBlocks, v -> cfg.detectBlocks = v);
                toggle(right, y, 150, "Players", () -> cfg.detectPlayers, v -> cfg.detectPlayers = v); y += step;
                toggle(left, y, 150, "Other entities", () -> cfg.detectEntities, v -> cfg.detectEntities = v);
                toggle(right, y, 150, "End Crystals", () -> cfg.detectCrystals, v -> cfg.detectCrystals = v);
            }
        }
        addRenderableWidget(Button.builder(Component.literal("Restore Defaults"), btn -> { cfg.reset(); rebuildWidgets(); })
                .bounds(left, this.height - 28, 150, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), btn -> onClose())
                .bounds(right, this.height - 28, 150, 20).build());
    }

    @Override
    public void onClose() {
        cfg.save();
        if (this.minecraft != null) this.minecraft.setScreen(parent);
    }

    private void toggle(int x, int y, int w, String label, BooleanSupplier get, java.util.function.Consumer<Boolean> set) {
        addRenderableWidget(Button.builder(onOff(label, get.getAsBoolean()), btn -> {
            boolean v = !get.getAsBoolean();
            set.accept(v);
            btn.setMessage(onOff(label, v));
        }).bounds(x, y, w, 20).build());
    }

    private static Component onOff(String label, boolean v) { return Component.literal(label + ": " + (v ? "ON" : "OFF")); }

    private void slider(int x, int y, int w, String label, double min, double max, double step, double cur, DoubleConsumer set) {
        addRenderableWidget(new Slider(x, y, w, 20, label, min, max, step, cur, set));
    }

    private void colour(int x, int y, String label, IntSupplier get, IntConsumer set) {
        addRenderableWidget(new StringWidget(x, y + 6, 190, 10, Component.literal(label), this.font));
        EditBox box = new EditBox(this.font, x + 200, y, 110, 20, Component.literal(label));
        box.setMaxLength(7);
        box.setValue(String.format("#%06X", get.getAsInt() & 0xFFFFFF));
        box.setResponder(s -> {
            String t = s.startsWith("#") ? s.substring(1) : s;
            boolean ok = t.length() == 6;
            if (ok) {
                try { set.accept(Integer.parseInt(t, 16)); } catch (NumberFormatException e) { ok = false; }
            }
            box.setTextColor(ok ? 0xFFE0E0E0 : 0xFFFF5555);
        });
        addRenderableWidget(box);
    }

    private static final class Slider extends AbstractSliderButton {
        private final String label;
        private final double min, max, step;
        private final DoubleConsumer set;

        Slider(int x, int y, int w, int h, String label, double min, double max, double step, double cur, DoubleConsumer set) {
            super(x, y, w, h, Component.empty(), (cur - min) / (max - min));
            this.label = label; this.min = min; this.max = max; this.step = step; this.set = set;
            updateMessage();
        }

        private double snapped() {
            double v = min + this.value * (max - min);
            v = Math.round(v / step) * step;
            return Math.max(min, Math.min(max, v));
        }

        @Override
        protected void updateMessage() {
            double v = snapped();
            setMessage(Component.literal(label + ": " + (step < 1 ? String.format("%.2f", v) : String.valueOf((int) v))));
        }

        @Override
        protected void applyValue() { set.accept(snapped()); }
    }
}
