package com.bowcrossbow.trajectory;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;

/** Crossbow: prediction stays active while a loaded crossbow (arrow) is held in either hand. */
public final class CrossbowHandler {
    private CrossbowHandler() {}

    public static boolean query(LocalPlayer player, TrajectoryManager.Shot out) {
        ModConfiguration cfg = ModConfiguration.get();
        if (!cfg.crossbowPrediction) return false;
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (!stack.is(Items.CROSSBOW)) continue;
            ChargedProjectiles loaded = stack.get(DataComponents.CHARGED_PROJECTILES);
            if (loaded == null || loaded.isEmpty()) continue;
            if (loaded.contains(Items.FIREWORK_ROCKET)) return false; // rockets are not ballistic arrows
            out.speed = TrajectoryCalculator.CROSSBOW_ARROW_SPEED;
            out.multishot = cfg.multishotPrediction && hasMultishot(stack);
            out.yOffset = 0.15;
            return true;
        }
        return false;
    }

    private static boolean hasMultishot(ItemStack stack) {
        for (Holder<Enchantment> h : stack.getEnchantments().keySet()) {
            if (h.is(Enchantments.MULTISHOT)) return true;
        }
        return false;
    }
}
