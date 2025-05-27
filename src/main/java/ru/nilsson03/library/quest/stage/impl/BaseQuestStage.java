package ru.nilsson03.library.quest.stage.impl;

import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.stage.QuestStage;

public class BaseQuestStage implements QuestStage {

    private final Objective objective;
    private final QuestNamespace quest;
    private final int weight;

    public BaseQuestStage(Objective objective, QuestNamespace quest, int weight) {
        this.objective = objective;
        this.quest = quest;
        this.weight = weight;
    }

    @Override
    public Objective objective() {
        return objective;
    }

    @Override
    public QuestNamespace quest() {
        return quest;
    }

    @Override
    public int weight() {
        return weight;
    }

    @Override
    public int compareTo(QuestStage o) {
        return Integer.compare(weight, o.weight());
    }
}
