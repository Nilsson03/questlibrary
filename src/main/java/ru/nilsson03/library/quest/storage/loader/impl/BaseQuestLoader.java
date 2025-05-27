package ru.nilsson03.library.quest.storage.loader.impl;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.simple.impl.BaseQuestImpl;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;
import ru.nilsson03.library.quest.storage.loader.helper.QuestLoadHelper;

import java.io.File;
import java.util.List;

public class BaseQuestLoader implements QuestLoader {

    private final QuestService questService;
    private final ObjectiveParser objectiveParser;

    public BaseQuestLoader(QuestService questService) {
        this.questService = questService;
        this.objectiveParser = questService.getObjectiveRegistry()
                                           .getObjectiveParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quest loadQuestFromFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        Preconditions.checkArgument(config.getString("id") != null,
                "Error loading quest file " + file.getName() + ": Quest id is null");

        QuestNamespace id = QuestNamespace.of(config.getString("id"));
        QuestMeta meta = parseMeta(config);
        QuestReward reward = parseReward(config);
        List<QuestCondition> conditions = parseConditions(config, id);
        List<Objective> objectives = parseObjectives(config);

        return new BaseQuestImpl(id, meta, conditions, objectives, reward);
    }

    private QuestMeta parseMeta(FileConfiguration config) {
        Parser<QuestMeta> parser = questService.getMetaParser("simpleMetaParser");
        return parser.parse(config.getConfigurationSection("meta"));
    }

    private QuestReward parseReward(FileConfiguration config) {
        Parser<QuestReward> parser = questService.getRewardParser("simpleRewardParser");
        return parser.parse(config.getConfigurationSection("reward"));
    }

    private List<QuestCondition> parseConditions(FileConfiguration config, QuestNamespace id) {
        return QuestLoadHelper.loadQuestConditions(
                config.getConfigurationSection("conditions"), id, questService);
    }

    private List<Objective> parseObjectives(FileConfiguration config) {
        return QuestLoadHelper.loadQuestObjectives(
                config.getConfigurationSection("objectives"), objectiveParser);
    }
}
