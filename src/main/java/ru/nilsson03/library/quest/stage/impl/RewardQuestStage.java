package ru.nilsson03.library.quest.stage.impl;

import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.reward.QuestReward;

public class RewardQuestStage extends BaseQuestStage {

    private final QuestReward questReward;

    public RewardQuestStage(Objective objective, QuestNamespace quest, int weight, QuestReward questReward) {
        super(objective, quest, weight);
        this.questReward = questReward;
    }

    public QuestReward getQuestReward() {
        return questReward;
    }
}
