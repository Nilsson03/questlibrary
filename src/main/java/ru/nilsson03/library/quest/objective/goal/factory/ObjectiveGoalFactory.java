package ru.nilsson03.library.quest.objective.goal.factory;

import ru.nilsson03.library.quest.objective.goal.Goal;

import java.util.Map;

@FunctionalInterface
public interface ObjectiveGoalFactory {
    /**
     * Создает цель на основе переданных параметров.
     *
     * @param parameters параметры для создания цели.
     * @return созданная цель.
     */
    Goal create(Map<String, Object> parameters);
}
