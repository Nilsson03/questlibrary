package ru.nilsson03.library.quest.core;

import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.reward.QuestReward;

public interface Quest {
    QuestReward rewards();

    QuestNamespace questUniqueKey();

    QuestMeta questMeta();
}