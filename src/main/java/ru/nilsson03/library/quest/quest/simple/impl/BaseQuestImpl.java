package ru.nilsson03.library.quest.quest.simple.impl;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.reward.QuestReward;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record BaseQuestImpl(Namespace questUniqueKey, QuestMeta questMeta, Set<QuestCondition> questCondition,
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
    public Set<QuestCondition> conditions() {
        return new HashSet<>(questCondition);
    }
}
