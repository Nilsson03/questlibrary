package ru.nilsson03.library.quest.objective.factory.impl;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.StagedQuestProgress;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class StagedQuestProgressFactory implements QuestProgressFactory {

    @Override
    public Set<QuestProgress> createProgress(QuestUserData userData, Quest quest) {
        StagedQuest stagedQuest = (StagedQuest) quest;
        return stagedQuest.stages()
                          .stream()
                          .map(stage -> new StagedQuestProgress(userData, stagedQuest))
                          .collect(Collectors.toSet());
    }
}
