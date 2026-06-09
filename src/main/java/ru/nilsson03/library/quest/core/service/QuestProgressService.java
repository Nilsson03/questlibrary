package ru.nilsson03.library.quest.core.service;

import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class QuestProgressService {

    public Set<QuestProgress> createEmptyProgressForQuest(final QuestUserData questUserData, final BaseQuest quest) {
        return quest.objectives()
                    .stream()
                    .map(objective -> new BaseQuestProgress(questUserData, quest, objective))
                    .collect(Collectors.toSet());
    }
}
