package ru.nilsson03.library.quest.objective.progress.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.helper.GoalHelper;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class BaseProgressParser implements Parser<QuestProgress> {

    private final ObjectiveGoalFactoryRegistry objectiveGoalRegistry;
    private final QuestStorage questStorage;

    public BaseProgressParser(
            QuestStorage questStorage, ObjectiveGoalFactoryRegistry objectiveGoalRegistry) {
        this.questStorage = questStorage;
        this.objectiveGoalRegistry = objectiveGoalRegistry;
    }

    @Override
    public QuestProgress parse(ConfigurationSection section) throws IllegalArgumentException, NullPointerException {
        throw new UnsupportedOperationException("Use parse(ConfigurationSection, QuestUserData) instead");
    }

    public QuestProgress parse(ConfigurationSection section, QuestUserData user) throws IllegalArgumentException, NullPointerException {

        String questId = section.getString("quest_id");

        BaseQuest quest = (BaseQuest) questStorage.getQuestByUniqueKeyOrThrow(questId);

        if (quest == null) {
            throw new NullPointerException("Не удалось получить Quest " + questId);
        }

        UUID userId = UUID.fromString(Objects.requireNonNull(section.getString("user_id"),
                                                             "User id in progress section is null. Quest id is " + questId + "."));

        if (!user.uuid().equals(userId)) {
            throw new IllegalArgumentException("User UUID mismatch: expected " + userId + ", got " + user.uuid());
        }

        String objectiveId = section.getString("objective_id");

        Objective objective = quest.objectives()
                                   .stream()
                                   .filter(objective1 -> objective1.key()
                                                                   .equals(objectiveId))
                                   .findFirst()
                                   .orElseThrow(() -> new NullPointerException(
                                           "Не удалось получить Objective в Quest " + quest.questUniqueKey()
                                                                                           .getKey() + " с id " + objectiveId));

        ConfigurationSection progress = section.getConfigurationSection("progress");

        if (progress == null) {
            throw new IllegalArgumentException("Не удалось получить поле progress в Quest " + quest.questUniqueKey()
                    .getKey() + " с UUID игрока " + user.uuid());
        }

        Map<Goal, Long> questProgress = GoalHelper.loadGoalsToProgress(objectiveGoalRegistry, section);

        return new BaseQuestProgress(user, quest, objective, questProgress);
    }
}
