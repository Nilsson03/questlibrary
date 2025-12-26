package ru.nilsson03.library.quest.objective.goal.impl;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
public class NumericGoal implements ObjectiveGoal {

    private final long targetValue;

    @Override
    public Long targetType() {
        return 1L;
    }

    @Override
    public boolean matches(Object target) {
        return target instanceof Long;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        return "Value(" + targetValue + ")";
    }
}
