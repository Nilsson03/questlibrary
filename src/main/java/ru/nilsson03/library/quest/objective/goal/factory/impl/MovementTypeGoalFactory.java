package ru.nightvision.quests.goal.factory;

import java.util.Map;

import ru.nightvision.quests.goal.MovementTypeGoal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;

public class MovementTypeGoalFactory implements ObjectiveGoalFactory {

    @Override
    public MovementTypeGoal create(Map<String, Object> parameters) {
        String typeStr = (String) parameters.get("movementType");
        MovementTypeGoal.MovementType movementType = MovementTypeGoal.MovementType.valueOf(typeStr.toUpperCase());
        
        long targetValue = Long.parseLong(parameters.get("value").toString());
        
        return new MovementTypeGoal(movementType, targetValue);
    }
}
