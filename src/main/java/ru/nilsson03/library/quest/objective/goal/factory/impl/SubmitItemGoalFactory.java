package ru.nilsson03.library.quest.objective.goal.factory.impl;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.SubmitItemGoal;

public class SubmitItemGoalFactory implements ObjectiveGoalFactory {

    @Override
    public Goal create(Map<String, Object> parameters) {
        long targetValue = Long.parseLong(parameters.get("value").toString());

        ItemStack targetType = null;
        if (parameters.containsKey("item")) {
            Object itemObj = parameters.get("item");
            if (itemObj instanceof ItemStack) {
                targetType = (ItemStack) itemObj;
            } else if (itemObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> itemParams = (Map<String, Object>) itemObj;
                targetType = ItemUtil.fromMap(itemParams);
            } else if (itemObj instanceof String) {
                Material material = Material.getMaterial(itemObj.toString().toUpperCase());
                if (material != null) {
                    targetType = new ItemStack(material);
                }
            }
        }

        int minDurability = 1;
        if (parameters.containsKey("minDurability")) {
            minDurability = Integer.parseInt(parameters.get("minDurability").toString());
        }

        int maxDurability = 0;
        if (parameters.containsKey("maxDurability")) {
            maxDurability = Integer.parseInt(parameters.get("maxDurability").toString());
        }

        return new SubmitItemGoal(targetType, targetValue, minDurability, maxDurability);
    }
}
