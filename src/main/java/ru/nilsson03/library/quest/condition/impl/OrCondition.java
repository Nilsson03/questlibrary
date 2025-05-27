package ru.nilsson03.library.quest.condition.impl;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;

public class OrCondition implements QuestCondition {
    private final List<QuestCondition> conditions;

    public OrCondition(List<QuestCondition> conditions) {
        this.conditions = conditions;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        for (QuestCondition condition : conditions) {
            if (condition.isMet(user)) {
                return true;
            }
        }
        return false;
    }
}
