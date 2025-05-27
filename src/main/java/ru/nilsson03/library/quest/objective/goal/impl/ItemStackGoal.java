package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.util.ItemStackSerialize;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

public record ItemStackGoal(ItemStack targetType, long targetValue) implements ObjectiveGoal {

    @Override
    public boolean matches(Object target) {
        if (target instanceof ItemStack other) {
            return targetType.isSimilar(other);
        }
        return false;
    }

    @Override
    public String toString() {
        String data = ItemStackSerialize.serialize(targetType);
        return "ItemStack(".concat(data).concat("-" + targetValue).concat(")");
    }
}
