package ru.nilsson03.library.quest.reward.parser.registry;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.parser.BaseRewardParser;

public class RewardParserRegistry extends ParserRegistry<Parser<QuestReward>, QuestReward> {

    private final String pluginName;

    public RewardParserRegistry(Plugin plugin) {
        this.pluginName = plugin.getName();
    }

    @Override
    public void onRegistryInit() {
        registerParser(pluginName, "simple", new BaseRewardParser());
    }

    @Override
    public void onRegistryAfterInit() {

    }
}
