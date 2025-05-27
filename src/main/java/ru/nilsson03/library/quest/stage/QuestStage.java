package ru.nilsson03.library.quest.stage;

import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;

public interface QuestStage extends Comparable<QuestStage> {

    /**
     * Задача данного этапа
     *
     * @return задача этапа, которую необходимо выполнить для перехода на следующую
     */
    Objective objective();

    /**
     * Идентификатор квеста к которому относится этап
     *
     * @return Идентификатор квеста, к которому относится этап
     */
    QuestNamespace quest();

    /**
     * Вес этапа квеста. Используется для определения последовательности
     *
     * @return целочисленный вес этапа
     */
    int weight();

}
