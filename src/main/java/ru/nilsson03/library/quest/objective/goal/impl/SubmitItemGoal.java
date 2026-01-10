package ru.nightvision.quests.goal;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.quest.objective.goal.impl.ItemStackGoal;

public class SubmitItemGoal extends ItemStackGoal {

    private final AtomicInteger submittedCount;

    public SubmitItemGoal(ItemStack targetType, long targetValue) {
        super(targetType, targetValue);
        this.submittedCount = new AtomicInteger(0);
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
