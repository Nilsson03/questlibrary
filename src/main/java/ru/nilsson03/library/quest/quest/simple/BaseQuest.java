package ru.nilsson03.library.quest.quest.simple;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.reward.QuestReward;

public interface BaseQuest {

    List<Objective> objectives();

    QuestReward rewards();

    Namespace questUniqueKey();

    QuestMeta questMeta();

    Set<QuestCondition> conditions();

    default boolean conditionsIsEmpty() {
        return conditions().isEmpty();
    }

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
