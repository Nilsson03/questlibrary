package ru.nilsson03.library.quest.core.service;

import org.junit.jupiter.api.Test;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.impl.PrerequisiteQuestGoal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuestProgressServiceTest {

    @Test
    void creditsPrerequisiteCompletedBeforeQuestWasStarted() {
        PrerequisiteQuestGoal completedGoal = new PrerequisiteQuestGoal("neron_quest_02", "quest", "villager");
        PrerequisiteQuestGoal pendingGoal = new PrerequisiteQuestGoal("another_quest", "quest", "villager");
        Objective objective = new Objective("neron_02", mock(ObjectiveType.class), List.of(),
                List.of(completedGoal, pendingGoal), "");

        BaseQuest completedQuest = mock(BaseQuest.class);
        when(completedQuest.questUniqueKey()).thenReturn(Namespace.of("Quests", "neron_quest_02"));
        BaseQuest quest = mock(BaseQuest.class);
        when(quest.objectives()).thenReturn(List.of(objective));
        QuestUserData user = mock(QuestUserData.class);
        when(user.completeQuests()).thenReturn(List.of(completedQuest));

        QuestProgress progress = new QuestProgressService().createEmptyProgressForQuest(user, quest)
                .iterator().next();

        assertEquals(1L, progress.getValue(completedGoal));
        assertEquals(0L, progress.getValue(pendingGoal));
    }
}
