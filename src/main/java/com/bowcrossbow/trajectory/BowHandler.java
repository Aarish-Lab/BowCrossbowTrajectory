package com.bowcrossbow.trajectory;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Bow: prediction is active only while the bow is being drawn; speed follows vanilla draw strength. */
public final class BowHandler {
    private BowHandler() {}

    public static boolean query(LocalPlayer player, float partialTick, TrajectoryManager.Shot out) {
        if (!ModConfiguration.get().bowPrediction || !player.isUsingItem()) return false;
        ItemStack stack = player.getUseItem();
        if (!stack.is(Items.BOW)) return false;
        double speed = TrajectoryCalculator.bowSpeed(player.getTicksUsingItem() + partialTick);
        if (speed <= 0.0) return false;
        out.speed = speed;
        out.multishot = false;
        out.yOffset = 0.1;
        return true;
    }
}
