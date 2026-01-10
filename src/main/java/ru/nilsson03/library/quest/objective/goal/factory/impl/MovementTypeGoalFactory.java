package ru.nilsson03.library.quest.objective.goal.factory.impl;

import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.MovementTypeGoal;

import java.util.Map;

public class MovementTypeGoalFactory implements ObjectiveGoalFactory {

    @Override
    public MovementTypeGoal create(Map<String, Object> parameters) {
        String typeStr = (String) parameters.get("movementType");
        MovementTypeGoal.MovementType movementType = MovementTypeGoal.MovementType.valueOf(typeStr.toUpperCase());
        
        long targetValue = Long.parseLong(parameters.get("value").toString());
        
        return new MovementTypeGoal(movementType, targetValue);
    }
}
