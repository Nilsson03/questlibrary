package ru.nilsson03.library.quest.objective.factory.impl;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class BaseQuestProgressFactory implements QuestProgressFactory {

    @Override
    public Set<QuestProgress> createProgress(QuestUserData userData, Quest quest) {
        BaseQuest baseQuest = (BaseQuest) quest;
        return baseQuest.objectives()
                        .stream()
                        .map(objective -> new BaseQuestProgress(userData, baseQuest, objective))
                        .collect(Collectors.toSet());
    }
}