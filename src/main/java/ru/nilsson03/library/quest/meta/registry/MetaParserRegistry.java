package ru.nilsson03.library.quest.meta.registry;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.parser.SimpleMetaParser;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;

public class MetaParserRegistry extends ParserRegistry<Parser<QuestMeta>, QuestMeta> {

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerParser(String pluginName, String metaKey, Parser<QuestMeta> parser) {
        super.registerParser(pluginName, metaKey, parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestMeta parse(ConfigurationSection section) {
        return super.parse(section);
    }

    @Override
    public void onRegistryInit() {
        registerParser("questlibrary", "simpleMetaParser", new SimpleMetaParser());
    }
}
