package ru.nilsson03.library.quest.storage.loader.impl;

import com.google.common.base.Preconditions;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.staged.impl.StagedQuestImpl;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.stage.QuestStage;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;
import ru.nilsson03.library.quest.storage.loader.helper.QuestLoadHelper;

import java.io.File;
import java.util.List;

public class StagedQuestLoader implements QuestLoader {

    private final QuestService questService;

    public StagedQuestLoader(QuestService questService) {
        this.questService = questService;
    }

    @Override
    public Quest loadQuestFromFile(File file) {
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        Preconditions.checkArgument(config.getString("id") != null,
                "Error loading quest file " + file.getName() + ": Quest id is null");

        QuestNamespace id = QuestNamespace.of(config.getString("id"));
        QuestMeta meta = parseMeta(config);
        QuestReward reward = parseReward(config);
        List<QuestStage> stages = parseStages(config, id);

        return new StagedQuestImpl(stages, reward, id, meta);
    }

    private QuestMeta parseMeta(FileConfiguration config) {
        Parser<QuestMeta> parser = questService.getMetaParser("stagedMetaParser");
        return parser.parse(config.getConfigurationSection("meta"));
    }

    private QuestReward parseReward(FileConfiguration config) {
        Parser<QuestReward> parser = questService.getRewardParser("stagedRewardParser");
        return parser.parse(config.getConfigurationSection("reward"));
    }

    private List<QuestStage> parseStages(FileConfiguration config, QuestNamespace id) {
        ConfigurationSection stagesSection = config.getConfigurationSection("stages");
        Preconditions.checkArgument(stagesSection != null,
                "Stages section is missing in quest " + id.getKey());

        return QuestLoadHelper.loadQuestStages(stagesSection, questService, id);
    }
}
