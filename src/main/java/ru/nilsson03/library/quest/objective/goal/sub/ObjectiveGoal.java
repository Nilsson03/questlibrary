package ru.nilsson03.library.quest.objective.goal.sub;

import ru.nilsson03.library.quest.objective.goal.Goal;

public interface ObjectiveGoal extends Goal {

    Object targetType();

    long targetValue();

    String toString();
}
