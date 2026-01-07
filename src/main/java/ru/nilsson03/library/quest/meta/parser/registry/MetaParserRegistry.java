package ru.nilsson03.library.quest.meta.parser.registry;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.parser.SimpleMetaParser;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;

public class MetaParserRegistry extends ParserRegistry<Parser<QuestMeta>, QuestMeta> {

    private final String pluginName;

    public MetaParserRegistry(Plugin plugin) {
        this.pluginName = plugin.getName();
    }

    @Override
    public void onRegistryInit() {
        registerParser(pluginName, "simple", new SimpleMetaParser());
    }

    @Override
    public void onRegistryAfterInit() {

    }
}
