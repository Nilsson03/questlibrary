package ru.nilsson03.library.quest.objective.goal.impl;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class SubmitItemGoal extends ItemStackGoal {

    private final AtomicInteger submittedCount;
    private final int minDurability;
    private final int maxDurability;

    public SubmitItemGoal(ItemStack targetType, long targetValue, int minDurability, int maxDurability) {
        super(targetType, targetValue);
        this.submittedCount = new AtomicInteger(0);
        this.minDurability = minDurability;
        this.maxDurability = maxDurability;
    }

    public boolean submitItems(ItemStack[] items) {
        boolean anySubmitted = false;

        for (ItemStack item : items) {
            if (item == null || item.getType() == Material.AIR) {
                continue;
            }

            if (!matches(item)) {
                continue;
            }

            int targetDurability = item.getDurability();

            if (targetDurability <= minDurability || targetDurability >= maxDurability) {
                continue;
            }

            int currentSubmitted = submittedCount.get();
            int required = (int) targetValue();

            if (currentSubmitted >= required) {
                continue;
            }

            int needed = required - currentSubmitted;
            int toSubmit = Math.min(item.getAmount(), needed);

            if (toSubmit > 0) {
                submittedCount.addAndGet(toSubmit);

                item.setAmount(item.getAmount() - toSubmit);

                anySubmitted = true;
            }
        }

        return anySubmitted;
    }

    public int getSubmittedCount() {
        return submittedCount.get();
    }
}
