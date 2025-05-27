package ru.nilsson03.library.quest.quest.staged.impl;

import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.stage.QuestStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record StagedQuestImpl(List<QuestStage> stages, QuestReward rewards, QuestNamespace questUniqueKey,
                              QuestMeta questMeta) implements StagedQuest {

    public StagedQuestImpl {
        Objects.requireNonNull(questUniqueKey, "QuestNamespace cannot be null");
        Objects.requireNonNull(questMeta, "QuestMeta cannot be null");
        Objects.requireNonNull(stages, "Quest stages cannot be null");
        Objects.requireNonNull(rewards, "Quest rewards cannot be null");
        stages = sortStages(stages);
    }

    public List<QuestStage> stages() {
        return new ArrayList<>(stages);
    }
}
