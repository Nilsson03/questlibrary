package ru.nilsson03.library.quest.objective.progress.parser;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.impl.PrerequisiteQuestGoal;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.saver.BaseProgressSaver;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaseProgressParserTest {

    @Test
    void restoresPrerequisiteGoalFromSavedProgress() {
        PrerequisiteQuestGoal goal = new PrerequisiteQuestGoal("neron_quest_02", "quest", "villager");
        Objective objective = new Objective("neron_02", mock(ObjectiveType.class), List.of(), List.of(goal), "");
        BaseQuest quest = mock(BaseQuest.class);
        when(quest.questUniqueKey()).thenReturn(Namespace.of("Quests", "next_quest"));
        when(quest.objectives()).thenReturn(List.of(objective));
        QuestUserData user = mock(QuestUserData.class);
        when(user.uuid()).thenReturn(UUID.randomUUID());

        QuestProgress original = new ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress(
                user, quest, objective);
        original.setProgressDirectly(goal, 1L);
        YamlConfiguration yaml = new YamlConfiguration();
        ConfigurationSection section = yaml.createSection("active_progresses.next_quest");
        new BaseProgressSaver().save(original, section);

        QuestStorage storage = mock(QuestStorage.class);
        when(storage.getQuestByUniqueKeyOrThrow("next_quest")).thenReturn(quest);
        QuestProgress restored = new BaseProgressParser(storage, mock(ObjectiveGoalFactoryRegistry.class))
                .parse(section, user);

        assertEquals(1L, restored.getValue(goal));
    }
}
