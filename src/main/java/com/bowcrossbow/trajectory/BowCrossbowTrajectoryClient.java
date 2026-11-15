package com.bowcrossbow.trajectory;

import net.fabricmc.api.ClientModInitializer;

public final class BowCrossbowTrajectoryClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModConfiguration.load();
        KeybindManager.register();
        TrajectoryRenderer.register();
    }
}
