package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.entity.EntityType;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

public record EntityTypeGoal(EntityType targetType, long targetValue) implements ObjectiveGoal {

    @Override
    public boolean matches(Object target) {
        return target instanceof EntityType && target == targetType;
    }

    @Override
    public String toString() {
        return "Entity(".concat(targetType.name()).concat("-" + targetValue).concat(")");
    }
}
