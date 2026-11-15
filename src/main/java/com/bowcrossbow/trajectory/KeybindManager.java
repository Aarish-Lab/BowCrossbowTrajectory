package com.bowcrossbow.trajectory;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

/** Toggle key, rebindable under Options > Controls > Key Binds. Default B (unused by vanilla). */
public final class KeybindManager {
    private static KeyMapping toggle;

    private KeybindManager() {}

    public static void register() {
        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath("bowcrossbowtrajectory", "main"));
        toggle = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.bowcrossbowtrajectory.toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_B, category));
        ClientTickEvents.END_CLIENT_TICK.register(KeybindManager::tick);
    }

    private static void tick(Minecraft mc) {
        while (toggle.consumeClick()) {
            ModConfiguration cfg = ModConfiguration.get();
            cfg.enabled = !cfg.enabled;
            cfg.save();
            if (mc.player != null) {
                mc.gui.setOverlayMessage(Component.literal("Trajectory prediction: " + (cfg.enabled ? "ON" : "OFF")), true);
            }
        }
    }
}
