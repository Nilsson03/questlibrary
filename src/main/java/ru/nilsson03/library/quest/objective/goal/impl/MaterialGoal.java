package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.Material;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
public class  MaterialGoal implements ObjectiveGoal {
    private final Material targetType;
    private final long targetValue;

    @Override
    public boolean matches(Object target) {
        return target instanceof Material && target == targetType;
    }

    @Override
    public String toString() {
        return "Material(".concat(targetType.name()).concat("-" + targetValue).concat(")");
    }

    @Override
    public Material targetType() {
        return targetType;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }
}
