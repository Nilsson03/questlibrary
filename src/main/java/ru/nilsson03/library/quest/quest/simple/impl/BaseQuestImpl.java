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

public class BaseQuestImpl implements BaseQuest {
    
    private final Namespace questUniqueKey;
    private final QuestMeta questMeta;
    private final Set<QuestCondition> questCondition;
    private final List<Objective> objectives;
    private final QuestReward rewards;

    public BaseQuestImpl(Namespace questUniqueKey, QuestMeta questMeta, Set<QuestCondition> questCondition,
                         List<Objective> objectives, QuestReward rewards) {
        this.questUniqueKey = Objects.requireNonNull(questUniqueKey, "QuestNamespace cannot be null");
        this.questMeta = Objects.requireNonNull(questMeta, "QuestMeta cannot be null");
        this.questCondition = new HashSet<>(Objects.requireNonNull(questCondition, "QuestCondition cannot be null"));
        this.objectives = new ArrayList<>(Objects.requireNonNull(objectives, "Objectives cannot be null"));
        this.rewards = rewards;
    }

    @Override
    public Namespace questUniqueKey() {
        return questUniqueKey;
    }

    @Override
    public QuestMeta questMeta() {
        return questMeta;
    }

    @Override
    public Set<QuestCondition> conditions() {
        return new HashSet<>(questCondition);
    }

    @Override
    public List<Objective> objectives() {
        return new ArrayList<>(objectives);
    }

    @Override
    public QuestReward rewards() {
        return rewards;
    }
}
