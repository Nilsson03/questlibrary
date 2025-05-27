package ru.nilsson03.library.quest.storage.loader.helper;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.exception.ParserNotRegisterException;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.stage.QuestStage;
import ru.nilsson03.library.quest.stage.impl.BaseQuestStage;
import ru.nilsson03.library.quest.stage.impl.RewardQuestStage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestLoadHelper {

    public static List<QuestCondition> loadQuestConditions(
            ConfigurationSection configurationSection, QuestNamespace id, QuestService questService) {
        List<QuestCondition> conditions = new ArrayList<>();
        if (configurationSection != null) {
            for (String conditionKey : configurationSection.getKeys(false)) {
                try {
                    Parser<QuestCondition> conditionParser = questService.getConditionParser(conditionKey);
                    QuestCondition condition = conditionParser.parse(
                            configurationSection.getConfigurationSection(conditionKey));
                    conditions.add(condition);
                } catch (ParserNotRegisterException exception) {
                    questService.getPlugin()
                                .getLogger()
                                .severe("The conditions for completing the " + id.getKey() + " quest could not be loaded because the " + conditionKey + " condition parser was not found.");
                }
            }
        }

        return conditions;
    }

    public static List<QuestStage> loadQuestStages(ConfigurationSection stagesSection,
                                                   QuestService questService,
                                                   QuestNamespace questId) {
        Objects.requireNonNull(stagesSection, "Stages section cannot be null");
        Objects.requireNonNull(questService, "QuestService cannot be null");
        Objects.requireNonNull(questId, "QuestNamespace cannot be null");

        List<QuestStage> stages = new ArrayList<>();
        ObjectiveParser objectiveParser = questService.getObjectiveRegistry().getObjectiveParser();
        Parser<QuestReward> rewardParser = questService.getRewardParser("baseRewardParser");

        for (String stageKey : stagesSection.getKeys(false)) {
            ConfigurationSection stageSection = stagesSection.getConfigurationSection(stageKey);
            if (stageSection == null) continue;

            int weight = stageSection.getInt("weight");
            Objective objective = objectiveParser.parse(stageSection.getConfigurationSection("objective"));

            if (stageSection.contains("reward")) {
                QuestReward reward = rewardParser.parse(stageSection.getConfigurationSection("reward"));
                stages.add(new RewardQuestStage(objective, questId, weight, reward));
            } else {
                stages.add(new BaseQuestStage(objective, questId, weight));
            }
        }

        return stages;
    }

    public static List<Objective> loadQuestObjectives(
            ConfigurationSection configurationSection, ObjectiveParser objectiveParser) {
        List<Objective> objectives = new ArrayList<>();
        if (configurationSection != null) {
            for (String objectiveKey : configurationSection.getKeys(false)) {
                ConfigurationSection objectiveSection = configurationSection.getConfigurationSection(objectiveKey);
                if (objectiveSection != null) {
                    Objective objective = objectiveParser.parse(objectiveSection);
                    objectives.add(objective);
                }
            }
        }

        return objectives;
    }
}
