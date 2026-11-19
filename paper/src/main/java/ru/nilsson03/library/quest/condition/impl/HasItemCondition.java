package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class HasItemCondition implements QuestCondition {
    private final Material itemType;
    private final int amount;
    private final ConditionType conditionType;

    public HasItemCondition(Material itemType, int amount) {
        this(itemType, amount, ConditionType.START);
    }

    public HasItemCondition(Material itemType, int amount, ConditionType conditionType) {
        this.itemType = itemType;
        this.amount = amount;
        this.conditionType = conditionType;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        return isMet(ConditionContext.of(user));
    }

    @Override
    public boolean isMet(ConditionContext context) {
        if (context.isGroupOwner() && context.actor().isEmpty()) {
            return getType() == ConditionType.START;
        }

        Player player = context.playerForChecks().orElse(null);
        if (player == null) {
            return false;
        }

        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == itemType) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }

    @Override
    public ConditionType getType() {
        return conditionType;
    }
}
