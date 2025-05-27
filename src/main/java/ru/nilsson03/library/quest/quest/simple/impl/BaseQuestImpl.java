package ru.nilsson03.library.quest.quest.simple.impl;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record BaseQuestImpl(QuestNamespace questUniqueKey, QuestMeta questMeta, List<QuestCondition> questCondition,
                            List<Objective> objectives, QuestReward rewards) implements BaseQuest {

    public BaseQuestImpl {
        Objects.requireNonNull(questUniqueKey, "QuestNamespace cannot be null");
        Objects.requireNonNull(questMeta, "QuestMeta cannot be null");
        Objects.requireNonNull(questCondition, "QuestCondition cannot be null");
        Objects.requireNonNull(objectives, "Objectives cannot be null");
        Objects.requireNonNull(rewards, "QuestReward cannot be null");
    }

    @Override
    public List<Objective> objectives() {
        return new ArrayList<>(objectives);
    }

    @Override
    public List<QuestCondition> conditions() {
        return new ArrayList<>(questCondition);
    }
}
