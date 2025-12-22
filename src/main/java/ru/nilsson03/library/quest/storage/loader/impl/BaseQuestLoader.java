package ru.nilsson03.library.quest.storage.loader.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.parser.SimpleMetaParser;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;
import ru.nilsson03.library.quest.quest.simple.impl.BaseQuestImpl;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.parser.BaseRewardParser;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;

import java.io.File;
import java.util.*;

/**
 * Базовая реализация загрузчика квестов из YAML файлов.
 * Поддерживает загрузку метаданных, условий, целей и наград.
 */
public class BaseQuestLoader implements QuestLoader {

    private final QuestService questService;
    private final SimpleMetaParser metaParser;
    private final BaseRewardParser rewardParser;
    private final ObjectiveParser objectiveParser;

    public BaseQuestLoader(QuestService questService) {
        this.questService = Objects.requireNonNull(questService, "QuestService cannot be null");
        this.metaParser = new SimpleMetaParser();
        this.rewardParser = new BaseRewardParser();
        this.objectiveParser = questService.getObjectiveRegistry().getObjectiveParser();
    }

    @Override
    public Quest loadQuestFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            questService.getPlugin().getLogger().warning("Invalid quest file: " + (file != null ? file.getName() : "null"));
            return null;
        }

        if (!file.getName().endsWith(".yml") && !file.getName().endsWith(".yaml")) {
            questService.getPlugin().getLogger().warning("Quest file must be YAML format: " + file.getName());
            return null;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            return parseQuest(config, file);
        } catch (Exception e) {
            questService.getPlugin().getLogger().severe("Failed to load quest from file: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Парсит квест из конфигурации.
     */
    private Quest parseQuest(ConfigurationSection config, File file) {
        String questKey = config.getString("key");
        if (questKey == null || questKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Quest key cannot be null or empty in file: " + file.getName());
        }

        String pluginName = questService.getPlugin().getName();
        Namespace questNamespace = Namespace.of(pluginName, questKey);

        QuestMeta questMeta = parseMeta(config);
        Set<QuestCondition> conditions = parseConditions(config);
        List<Objective> objectives = parseObjectives(config);
        QuestReward reward = parseReward(config);

        return new BaseQuestImpl(questNamespace, questMeta, conditions, objectives, reward);
    }

    /**
     * Парсит метаданные квеста.
     */
    private QuestMeta parseMeta(ConfigurationSection config) {
        ConfigurationSection metaSection = config.getConfigurationSection("meta");
        if (metaSection == null) {
            throw new IllegalArgumentException("Quest meta section is missing");
        }

        try {
            return metaParser.parse(metaSection);
        } catch (Exception e) {
            questService.getPlugin().getLogger().severe("Failed to parse quest meta: " + e.getMessage());
            throw new RuntimeException("Failed to parse quest meta", e);
        }
    }

    /**
     * Парсит условия квеста.
     */
    private Set<QuestCondition> parseConditions(ConfigurationSection config) {
        ConfigurationSection conditionsSection = config.getConfigurationSection("conditions");
        if (conditionsSection == null) {
            return new HashSet<>();
        }

        Set<QuestCondition> conditions = new HashSet<>();

        for (String conditionKey : conditionsSection.getKeys(false)) {
            ConfigurationSection conditionSection = conditionsSection.getConfigurationSection(conditionKey);
            if (conditionSection == null) {
                questService.getPlugin().getLogger().warning("Invalid condition section: " + conditionKey);
                continue;
            }

            try {
                QuestCondition condition = questService.getConditionParserRegistry().parse(conditionSection);
                if (condition != null) {
                    conditions.add(condition);
                }
            } catch (Exception e) {
                questService.getPlugin().getLogger().warning("Failed to parse condition '" + conditionKey + "': " + e.getMessage());
            }
        }

        return conditions;
    }

    /**
     * Парсит цели квеста.
     */
    private List<Objective> parseObjectives(ConfigurationSection config) {
        ConfigurationSection objectivesSection = config.getConfigurationSection("objectives");
        if (objectivesSection == null) {
            throw new IllegalArgumentException("Quest objectives section is missing");
        }

        List<Objective> objectives = new ArrayList<>();

        for (String objectiveKey : objectivesSection.getKeys(false)) {
            ConfigurationSection objectiveSection = objectivesSection.getConfigurationSection(objectiveKey);
            if (objectiveSection == null) {
                questService.getPlugin().getLogger().warning("Invalid objective section: " + objectiveKey);
                continue;
            }

            try {
                Objective objective = objectiveParser.parse(objectiveSection);
                if (objective != null) {
                    objectives.add(objective);
                }
            } catch (Exception e) {
                questService.getPlugin().getLogger().severe("Failed to parse objective '" + objectiveKey + "': " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (objectives.isEmpty()) {
            throw new IllegalArgumentException("Quest must have at least one objective");
        }

        return objectives;
    }

    /**
     * Парсит награду квеста.
     */
    private QuestReward parseReward(ConfigurationSection config) {
        ConfigurationSection rewardSection = config.getConfigurationSection("rewards");
        if (rewardSection == null) {
            throw new IllegalArgumentException("Quest reward section is missing");
        }

        try {
            return rewardParser.parse(rewardSection);
        } catch (Exception e) {
            questService.getPlugin().getLogger().severe("Failed to parse quest reward: " + e.getMessage());
            throw new RuntimeException("Failed to parse quest reward", e);
        }
    }
}
