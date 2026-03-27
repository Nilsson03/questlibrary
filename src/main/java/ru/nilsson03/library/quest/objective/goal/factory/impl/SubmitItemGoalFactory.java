package ru.nilsson03.library.quest.objective.goal.factory.impl;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.bukkit.util.ItemStackParser;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.SubmitItemGoal;

public class SubmitItemGoalFactory implements ObjectiveGoalFactory {

    @Override
    public Goal create(Map<String, Object> parameters) {
        long targetValue = Long.parseLong(parameters.get("value").toString());

        ItemStack targetType = null;
        if (parameters.containsKey("itemstack")) {
            Object itemStackObj = parameters.get("itemstack");

            if (itemStackObj instanceof ItemStack) {
                targetType = (ItemStack) itemStackObj;
            } else if (itemStackObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemStackParams = (Map<String, Object>) itemStackObj;
                targetType = ItemStackParser.fromMap(itemStackParams);
            } else if (itemStackObj instanceof ConfigurationSection) {
                ConfigurationSection section = (ConfigurationSection) itemStackObj;
                Map<String, Object> itemStackParams = section.getValues(false);
                targetType = ItemStackParser.fromMap(itemStackParams);
            } else if (itemStackObj instanceof String) {
                Material material = Material.getMaterial(itemStackObj.toString().toUpperCase());
                if (material != null) {
                    targetType = new ItemStack(material);
                }
            }
        }

        boolean durabilityCheck = (Boolean) parameters.getOrDefault("durabilityCheck", false);
        int minDurability = 1;
        if (parameters.containsKey("minDurability")) {
            minDurability = Integer.parseInt(parameters.get("minDurability").toString());
        }

        int maxDurability = 0;
        if (parameters.containsKey("maxDurability")) {
            maxDurability = Integer.parseInt(parameters.get("maxDurability").toString());
        }

        return new SubmitItemGoal(targetType, targetValue, minDurability, maxDurability, durabilityCheck);
    }
}
