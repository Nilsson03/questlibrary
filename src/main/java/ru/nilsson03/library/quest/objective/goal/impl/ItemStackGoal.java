package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.inventory.ItemStack;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.bukkit.util.ItemStackSerialize;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
public class ItemStackGoal implements ObjectiveGoal {

    private final ItemStack targetType;
    private final long targetValue;

    @Override
    public boolean matches(Object target) {
        if (target instanceof ItemStack other) {
            return targetType.isSimilar(other);
        }
        return false;
    }

    @Override
    public ItemStack targetType() {
        return targetType;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        String data = ItemStackSerialize.serialize(targetType);
        return "ItemStack(".concat(data).concat("-" + targetValue).concat(")");
    }
}
