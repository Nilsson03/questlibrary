package ru.nilsson03.library.quest.objective.goal.factory.impl;

import java.util.Map;

import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.PrerequisiteQuestGoal;

public class PrerequisiteQuestGoalFactory implements ObjectiveGoalFactory {

    @Override
    public Goal create(Map<String, Object> parameters) {
        String questId = parameters.get("quest-id").toString();
        String questName = parameters.get("quest-name").toString();
        String villagerName = parameters.get("villager-name").toString();
        
        return new PrerequisiteQuestGoal(questId, questName, villagerName);
    }
}
