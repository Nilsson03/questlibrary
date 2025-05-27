package ru.nilsson03.library.quest.objective.progress.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.helper.GoalHelper;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.StagedQuestProgress;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.stage.QuestStage;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StagedProgressParser implements Parser<QuestProgress> {


    private final ObjectiveGoalFactoryRegistry objectiveGoalRegistry;
    private final QuestStorage questStorage;
    private final QuestUsersStorage questUsersStorage;

    public StagedProgressParser(
            QuestStorage questStorage, ObjectiveGoalFactoryRegistry objectiveGoalRegistry,
            QuestUsersStorage questUsersStorage) {
        this.questStorage = questStorage;
        this.objectiveGoalRegistry = objectiveGoalRegistry;
        this.questUsersStorage = questUsersStorage;
    }


    @Override
    public QuestProgress parse(ConfigurationSection section) throws IllegalArgumentException, NullPointerException {

        String questId = section.getString("quest_id");

        StagedQuest quest = (StagedQuest) questStorage.getQuestByUniqueKeyOrThrow(questId);

        if (quest == null) {
            throw new NullPointerException("Не удалось получить Quest " + questId);
        }

        UUID userId = UUID.fromString(Objects.requireNonNull(section.getString("user_id"),
                                                             "User id in progress section is null. Quest id is " + questId + "."));

        QuestUserData user = questUsersStorage.getQuestUserData(userId);

        ConfigurationSection progress = section.getConfigurationSection("progress");

        if (progress == null) {
            throw new IllegalArgumentException("Не удалось получить поле progress в Quest " + quest.questUniqueKey()
                                                                                                   .getKey() + " с UUID игрока " + user.uuid());
        }

        Map<Goal, Long> questProgress = GoalHelper.loadGoalsToProgress(objectiveGoalRegistry, section);

        int currentStageWeight = section.getInt("current_stage");

        QuestStage questStage = quest.getStageByWeightOrThrow(currentStageWeight);

        return new StagedQuestProgress(user, quest, questProgress, questStage);
    }
}
