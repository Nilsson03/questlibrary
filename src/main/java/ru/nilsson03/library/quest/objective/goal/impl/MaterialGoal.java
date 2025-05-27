package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.Material;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

public record MaterialGoal(Material targetType, long targetValue) implements ObjectiveGoal {
    @Override
    public boolean matches(Object target) {
        return target instanceof Material && target == targetType;
    }

    @Override
    public String toString() {
        return "Material(".concat(targetType.name()).concat("-" + targetValue).concat(")");
    }
}
