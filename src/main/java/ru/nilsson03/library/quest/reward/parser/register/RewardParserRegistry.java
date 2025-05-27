package ru.nilsson03.library.quest.reward.parser.register;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.parser.BaseRewardParser;

public class RewardParserRegistry extends ParserRegistry<Parser<QuestReward>, QuestReward> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerParser(String pluginName, String metaKey, Parser<QuestReward> parser) {
        super.registerParser(pluginName, metaKey, parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestReward parse(ConfigurationSection section) {
        return super.parse(section);
    }

    @Override
    public void onRegistryInit() {
        registerParser("questApi", "baseRewardParser", new BaseRewardParser());
    }
}
