package ru.nilsson03.library.quest.quest.simple;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;

public interface BaseQuest extends Quest {

    List<Objective> objectives();

    /**
     * Проверка содержит ли список задач тип задачи 
     * @param objectiveType проверяемый тип задачи
     * @return true если содержит, false если нет
     */
    default boolean constainsObjectiveType(ObjectiveType objectiveType) {
        return objectives().stream()
                .anyMatch(objective -> objective.type().key().equals(objectiveType.key()));
    }

    /**
     * Фильтрует список задач по типу задачи
     * @param objectiveType тип задачи
     * @return множество задач соответствующих типу
     */
    default Set<Objective> filterObjectivesByType(ObjectiveType objectiveType) {
        return objectives().stream()
                .filter(objective -> objective.type().key().equals(objectiveType.key()))
                .collect(Collectors.toSet());
    }
}
