package ru.nilsson03.library.quest.core;

import java.util.Set;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.reward.QuestReward;

public interface Quest {
    QuestReward rewards();

    Namespace questUniqueKey();

    QuestMeta questMeta();

    Set<QuestCondition> conditions();

    default boolean conditionsIsEmpty() {
        return conditions().isEmpty();
    }
}