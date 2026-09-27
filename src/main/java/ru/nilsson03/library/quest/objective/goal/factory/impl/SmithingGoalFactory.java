package ru.nilsson03.library.quest.objective.goal.factory.impl;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.util.ItemStackParser;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.SmithingGoal;

import java.util.Map;

public class SmithingGoalFactory implements ObjectiveGoalFactory {
    @Override
    public Goal create(Map<String, Object> parameters) {
        ItemStack input = parseItem(parameters.get("input"));
        ItemStack result = parseItem(parameters.get("result"));
        long value = Long.parseLong(parameters.getOrDefault("value", 1).toString());
        return new SmithingGoal(input, result, value);
    }

    @SuppressWarnings("unchecked")
    private ItemStack parseItem(Object value) {
        if (value instanceof ItemStack item) return item;
        if (value instanceof ConfigurationSection section) return ItemStackParser.fromMap(section.getValues(false));
        if (value instanceof Map<?, ?> map) return ItemStackParser.fromMap((Map<String, Object>) map);
        if (value instanceof String material) {
            Material type = Material.matchMaterial(material);
            return type == null ? null : new ItemStack(type);
        }
        return null;
    }
}
