package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.bukkit.util.ItemStackSerialize;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

import java.util.Map;

public class EnchantWithLevelGoal implements ObjectiveGoal {

    private final ItemStack targetType;
    private final Enchantment enchantment;
    private final int minLevel;
    private final int maxLevel;
    private final long targetValue;

    public EnchantWithLevelGoal(
        ItemStack targetType,
        Enchantment enchantment,
        int minLevel,
        int maxLevel,
        long targetValue
    ) {
        this.targetType = targetType;
        this.enchantment = enchantment;
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.targetValue = targetValue;
    }

    @Override
    public boolean matches(Object target) {
        if (!(target instanceof ItemStack other)) {
            return false;
        }

        if (targetType != null && !targetType.isSimilar(other)) {
            return false;
        }

        if (enchantment == null) {
            return true;
        }

        Map<Enchantment, Integer> enchants = other.getEnchantments();
        if (!enchants.containsKey(enchantment)) {
            return false;
        }

        int level = enchants.get(enchantment);
        if (level < minLevel) {
            return false;
        }

        if (maxLevel > 0 && level > maxLevel) {
            return false;
        }

        return true;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("EnchantWithLevel(");
        if (targetType != null) {
            sb.append("item=").append(ItemStackSerialize.serialize(targetType)).append(", ");
        }
        if (enchantment != null) {
            sb.append("enchant=").append(enchantment.getKey().getKey()).append(", ");
        }
        sb.append("level=").append(minLevel);
        if (maxLevel > 0) {
            sb.append("-").append(maxLevel);
        }
        sb.append(", count=").append(targetValue).append(")");
        return sb.toString();
    }

    @Override
    public ItemStack targetType() {
        return targetType;
    }
}
