package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.bukkit.util.ItemStackSerialize;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

import java.util.Map;

@AllArgsConstructor
public class ItemStackGoal implements ObjectiveGoal {

    private final ItemStack targetType;
    private final long targetValue;

    @Override
    public boolean matches(Object target) {
        if (target instanceof ItemStack other) {
            if (targetType.getType() != other.getType()) {
                return false;
            }

            if (!matchesEnchantments(targetType, other)) {
                return false;
            }

            if (targetType.hasItemMeta() && other.hasItemMeta()) {
                ItemMeta targetMeta = targetType.getItemMeta();
                ItemMeta otherMeta = other.getItemMeta();

                if (targetMeta instanceof PotionMeta && otherMeta instanceof PotionMeta) {
                    return matchesPotionMeta((PotionMeta) targetMeta, (PotionMeta) otherMeta);
                }

                if (targetMeta.hasDisplayName() && otherMeta.hasDisplayName()) {
                    if (!targetMeta.getDisplayName().equals(otherMeta.getDisplayName())) {
                        return false;
                    }
                } else if (targetMeta.hasDisplayName() != otherMeta.hasDisplayName()) {
                    return false;
                }
            } else if (targetType.hasItemMeta() != other.hasItemMeta()) {
                return false;
            }

            return true;
        }
        return false;
    }

    private boolean matchesEnchantments(ItemStack target, ItemStack other) {
        Map<Enchantment, Integer> targetEnchants = target.getEnchantments();
        Map<Enchantment, Integer> otherEnchants = other.getEnchantments();

        if (targetEnchants.isEmpty()) {
            return true;
        }

        for (Map.Entry<Enchantment, Integer> entry : targetEnchants.entrySet()) {
            Enchantment enchantment = entry.getKey();
            Integer targetLevel = entry.getValue();
            Integer otherLevel = otherEnchants.get(enchantment);

            if (otherLevel == null || !otherLevel.equals(targetLevel)) {
                return false;
            }
        }
        
        return true;
    }

    private boolean matchesPotionMeta(PotionMeta targetMeta, PotionMeta otherMeta) {
        PotionData targetData = targetMeta.getBasePotionData();
        PotionData otherData = otherMeta.getBasePotionData();

        if (targetData.getType() != otherData.getType()) {
            return false;
        }

        if (targetData.isExtended() != otherData.isExtended()) {
            return false;
        }

        if (targetData.isUpgraded() != otherData.isUpgraded()) {
            return false;
        }

        if (targetMeta.hasDisplayName() && otherMeta.hasDisplayName()) {
            if (!targetMeta.getDisplayName().equals(otherMeta.getDisplayName())) {
                return false;
            }
        } else if (targetMeta.hasDisplayName() != otherMeta.hasDisplayName()) {
            return false;
        }
        
        return true;
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
