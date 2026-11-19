package ru.nilsson03.library.quest.condition.impl;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.util.QuestUtil;

public class QuestCompletedCondition implements QuestCondition {
    private final BaseQuest requiredQuest;
    private final ConditionType conditionType;

    public QuestCompletedCondition(BaseQuest requiredQuest) {
        this(requiredQuest, ConditionType.START);
    }
    
    public QuestCompletedCondition(BaseQuest requiredQuest, ConditionType conditionType) {
        this.requiredQuest = requiredQuest;
        this.conditionType = conditionType;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        return QuestUtil.questIsCompleted(user, requiredQuest);
    }
    
    @Override
    public ConditionType getType() {
        return conditionType;
    }
}
