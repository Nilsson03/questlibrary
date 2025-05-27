package ru.nilsson03.library.quest.objective.factory;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;

@FunctionalInterface
public interface QuestProgressFactory {
    Set<QuestProgress> createProgress(QuestUserData userData, Quest quest);
}