package ru.nilsson03.library.quest.objective.goal.impl;

import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

import java.util.Map;

public class NumericGoal implements ObjectiveGoal {

    private final long targetValue;
    public NumericGoal(long targetValue) {
        this.targetValue = targetValue;
    }

    public static NumericGoal fromParameters(Map<String, Object> parameters) {
        return new NumericGoal(Long.parseLong(parameters.get("value").toString()));
    }

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
