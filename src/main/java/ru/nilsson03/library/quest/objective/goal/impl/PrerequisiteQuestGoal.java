package ru.nilsson03.library.quest.objective.goal.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
@Getter
public class PrerequisiteQuestGoal implements ObjectiveGoal {

    private final String questId;
    private final String questName;
    private final String villagerName;

    @Override
    public boolean matches(Object target) {
        return target instanceof String && target.equals(questId);
    }

    @Override
    public String targetType() {
        return questId;
    }

    @Override
    public long targetValue() {
        return 1L;
    }

    @Override
    public String toString() {
        return "PrerequisiteQuest(" + questId + "-" + questName + "@" + villagerName + ")";
    }
}
