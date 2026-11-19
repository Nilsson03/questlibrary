package ru.nilsson03.library.quest.objective.goal.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

@AllArgsConstructor
@Getter
public class MovementTypeGoal implements ObjectiveGoal {

    private final MovementType movementType;
    private final long targetValue;

    @Override
    public boolean matches(Object target) {
        if (movementType == MovementType.ANY) {
            return true;
        }
        if (target instanceof MovementType) {
            return movementType == target;
        }
        return false;
    }

    @Override
    public MovementType targetType() {
        return movementType;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    @Override
    public String toString() {
        return "Movement(" + movementType.name() + "-" + targetValue + ")";
    }

    public enum MovementType {
        WALK, // Ходьба/бег
        FLY, // Полёт с элитрами
        BOAT, // Лодка
        HORSE, // Лошадь, осёл, мул и т.д.
        PIG, // Свинья
        STRIDER, // Страйдер
        VEHICLE, // Другие транспортные средства
        ANY // Любой тип движения
    }
}
