package ru.nilsson03.library.quest.storage.loader.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.parser.registry.MetaParserRegistry;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.quest.simple.impl.BaseQuestImpl;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.parser.registry.RewardParserRegistry;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;

/**
 * Базовая реализация загрузчика квестов из YAML файлов.
 * Поддерживает загрузку метаданных, условий, целей и наград.
 */
public class BaseQuestLoader implements QuestLoader {

    private final QuestService questService;
    private final ObjectiveParser objectiveParser;

    public BaseQuestLoader(QuestService questService) {
        this.questService = Objects.requireNonNull(questService, "QuestService cannot be null");
        this.objectiveParser = questService.getObjectiveRegistry().getObjectiveParser();
    }

    @Override
    public BaseQuest loadQuestFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            ConsoleLogger.warn(questService.getPlugin(), "Invalid quest file: %s", (file != null ? file.getName() : "null"));
            return null;
        }

        if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
            ConsoleLogger.warn(questService.getPlugin(), "Quest file must be YAML format: %s", file.getName());
            return null;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            return parseQuest(config, file);
        } catch (Exception e) {
            ConsoleLogger.error(questService.getPlugin(), "Failed to load quest from file %s: %s", file.getName(), e.getMessage());
            return null;
        }
    }

    private BaseQuest parseQuest(ConfigurationSection config, File file) {
        String questKey = config.getString("key");
        if (questKey == null || questKey.trim().isEmpty()) {
            ConsoleLogger.error(questService.getPlugin(), "Quest key cannot be null or empty in file: %s", file.getName());
            return null;
        }

        String pluginName = questService.getPlugin().getName();
        Namespace questNamespace = Namespace.of(pluginName, questKey);

        QuestMeta questMeta = parseMeta(config);
        Set<QuestCondition> conditions = parseConditions(config);
        List<Objective> objectives = parseObjectives(config);
        QuestReward reward = parseReward(config);

        return new BaseQuestImpl(questNamespace, questMeta, conditions, objectives, reward);
    }

    private QuestMeta parseMeta(ConfigurationSection config) {
        ConfigurationSection metaSection = config.getConfigurationSection("meta");
        if (metaSection == null) {
            ConsoleLogger.error(questService.getPlugin(), "Quest meta section is missing");
            return null;
        }

        try {
            MetaParserRegistry metaParserRegistry = questService.getMetaParserRegistry();
            return metaParserRegistry.parse(metaSection);
        } catch (Exception e) {
            ConsoleLogger.error(questService.getPlugin(), "Failed to parse quest meta: %s", e.getMessage());
            return null;
        }
    }

    private Set<QuestCondition> parseConditions(ConfigurationSection config) {
        ConfigurationSection conditionsSection = config.getConfigurationSection("conditions");
        if (conditionsSection == null) {
            return new HashSet<>();
        }

        Set<QuestCondition> conditions = new HashSet<>();
        
        try {
            QuestCondition condition = questService.getConditionParserRegistry().parse(conditionsSection);
            if (condition != null) {
                conditions.add(condition);
            }
        } catch (Exception e) {
            ConsoleLogger.warn(questService.getPlugin(), "Failed to parse conditions: %s", e.getMessage());
        }

        return conditions;
    }

    private List<Objective> parseObjectives(ConfigurationSection config) {
        ConfigurationSection objectivesSection = config.getConfigurationSection("objectives");
        if (objectivesSection == null) {
            ConsoleLogger.error(questService.getPlugin(), "Quest objectives section is missing");
            return new ArrayList<>();
        }

        List<Objective> objectives = new ArrayList<>();

        for (String objectiveKey : objectivesSection.getKeys(false)) {
            ConfigurationSection objectiveSection = objectivesSection.getConfigurationSection(objectiveKey);
            if (objectiveSection == null) {
                ConsoleLogger.warn(questService.getPlugin(), "Invalid objective section: %s", objectiveKey);
                continue;
            }

            try {
                Objective objective = objectiveParser.parse(objectiveSection);
                if (objective != null) {
                    objectives.add(objective);
                }
            } catch (Exception e) {
                ConsoleLogger.error(questService.getPlugin(), "Failed to parse objective '%s': %s", objectiveKey, e.getMessage());
            }
        }

        if (objectives.isEmpty()) {
            ConsoleLogger.error(questService.getPlugin(), "Quest must have at least one objective");
            return new ArrayList<>();
        }

        return objectives;
    }

    private QuestReward parseReward(ConfigurationSection config) {
        ConfigurationSection rewardSection = config.getConfigurationSection("rewards");
        if (rewardSection == null) {
            return null;
        }

        try {
            RewardParserRegistry rewardParserRegistry = questService.getRewardParserRegistry();
            return rewardParserRegistry.parse(rewardSection);
        } catch (Exception e) {
            ConsoleLogger.error(questService.getPlugin(), "Failed to parse quest reward: %s", e.getMessage());
            return null;
        }
    }
}
