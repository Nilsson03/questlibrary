package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.entity.EntityType;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
public class EntityTypeGoal implements ObjectiveGoal {

    private final EntityType targetType;
    private final long targetValue;

    @Override
    public boolean matches(Object target) {
        return target instanceof EntityType && target == targetType;
    }

    @Override
    public EntityType targetType() {
        return targetType;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        return "Entity(".concat(targetType.name()).concat("-" + targetValue).concat(")");
    }
}
