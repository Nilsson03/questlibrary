package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.entity.Player;

import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class PermissionCondition implements QuestCondition {

    private final String permission;
    private final ConditionType conditionType;

    public PermissionCondition(String permission) {
        this(permission, ConditionType.START);
    }

    public PermissionCondition(String permission, ConditionType conditionType) {
        this.permission = permission;
        this.conditionType = conditionType;
    }

    @Override
    public boolean isMet(QuestUserData questUserData) {
        return isMet(ConditionContext.of(questUserData));
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
        return player.hasPermission(permission);
    }

    @Override
    public ConditionType getType() {
        return conditionType;
    }
}
