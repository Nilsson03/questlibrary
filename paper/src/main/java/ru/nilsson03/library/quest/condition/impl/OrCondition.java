package ru.nilsson03.library.quest.condition.impl;

import java.util.List;

import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class OrCondition implements QuestCondition {
    private final List<QuestCondition> conditions;

    public OrCondition(List<QuestCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        return isMet(ConditionContext.of(user));
    }

    @Override
    public boolean isMet(ConditionContext context) {
        for (QuestCondition condition : conditions) {
            if (condition.isMet(context)) {
                return true;
            }
        }
        return false;
    }
}
