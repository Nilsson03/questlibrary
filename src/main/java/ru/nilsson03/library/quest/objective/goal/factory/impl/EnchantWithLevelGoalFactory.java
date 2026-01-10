package ru.nilsson03.library.quest.objective.goal.factory.impl;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.util.ItemUtil;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.EnchantWithLevelGoal;

import java.util.Map;

public class EnchantWithLevelGoalFactory implements ObjectiveGoalFactory {
    
    @Override
    public Goal create(Map<String, Object> parameters) {
        long targetValue = Long.parseLong(parameters.get("value").toString());
        
        ItemStack targetType = null;
        if (parameters.containsKey("item")) {
            Object itemObj = parameters.get("item");
            if (itemObj instanceof ItemStack) {
                targetType = (ItemStack) itemObj;
            } else if (itemObj instanceof Map) {
                // Если item - это Map с параметрами, используем SpigotItemBuilder
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
        
        Enchantment enchantment = null;
        if (parameters.containsKey("enchantment")) {
            String enchantName = parameters.get("enchantment").toString().toLowerCase();
            enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName));
            
            if (enchantment == null) {
                for (Enchantment ench : Enchantment.values()) {
                    if (ench.getKey().getKey().equalsIgnoreCase(enchantName) || 
                        ench.getName().equalsIgnoreCase(enchantName)) {
                        enchantment = ench;
                        break;
                    }
                }
            }
        }
        
        int minLevel = 1;
        if (parameters.containsKey("minLevel")) {
            minLevel = Integer.parseInt(parameters.get("minLevel").toString());
        } else if (parameters.containsKey("level")) {
            minLevel = Integer.parseInt(parameters.get("level").toString());
        }
        
        int maxLevel = 0;
        if (parameters.containsKey("maxLevel")) {
            maxLevel = Integer.parseInt(parameters.get("maxLevel").toString());
        }
        
        return new EnchantWithLevelGoal(targetType, enchantment, minLevel, maxLevel, targetValue);
    }
}
