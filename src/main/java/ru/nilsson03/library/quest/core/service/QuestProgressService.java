package ru.nilsson03.library.quest.core.service;

import ru.nilsson03.library.quest.objective.goal.impl.PrerequisiteQuestGoal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class QuestProgressService {

    public Set<QuestProgress> createEmptyProgressForQuest(final QuestUserData questUserData, final BaseQuest quest) {
        Set<String> completedQuestIds = questUserData.completeQuests().stream()
                .map(completedQuest -> completedQuest.questUniqueKey().getKey())
                .collect(Collectors.toSet());

        return quest.objectives()
                    .stream()
                    .map(objective -> {
                        BaseQuestProgress progress = new BaseQuestProgress(questUserData, quest, objective);
                        objective.goals().stream()
                                .filter(PrerequisiteQuestGoal.class::isInstance)
                                .filter(goal -> completedQuestIds.contains(((PrerequisiteQuestGoal) goal).getQuestId()))
                                .forEach(goal -> progress.setProgressDirectly(goal, goal.targetValue()));
                        return progress;
                    })
                    .collect(Collectors.toSet());
    }
}
