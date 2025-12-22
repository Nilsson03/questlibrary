package ru.nilsson03.library.quest.condition.parser.registry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.parser.impl.AndConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.HasItemConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.OrConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.QuestCompletedConditionParser;
import ru.nilsson03.library.quest.exception.QuestStorageException;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;

/**
 * Класс ConditionParserRegistry отвечает за хранение и управление парсерами условий.
 * Он позволяет регистрировать новые парсеры и использовать их для разбора условий из конфигурации.
 * Доступ к классу можно получить через сервис квестов, который должен быть инициализирован для вашего плагина
 *
 * @see ru.nilsson03.library.quest.core.service.QuestService
 */
public class ConditionParserRegistry extends ParserRegistry<Parser<QuestCondition>, QuestCondition> {

    private final Plugin plugin;
    private final String pluginName;

    public ConditionParserRegistry(Plugin plugin, String pluginName) {
        this.plugin = plugin;
        this.pluginName = pluginName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerParser(String pluginName, String conditionType, Parser<QuestCondition> parser) {
        super.registerParser(pluginName, conditionType, parser);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestCondition parse(ConfigurationSection section) {
        return super.parse(section);
    }

    @Override
    public void onRegistryInit() {
        registerParser(pluginName, "has_item", new HasItemConditionParser());
        registerParser(pluginName, "and", new AndConditionParser(this));
        registerParser(pluginName, "or", new OrConditionParser(this));
        try {
            registerParser(pluginName, "quest_completed", new QuestCompletedConditionParser(plugin));
        } catch (QuestStorageException exception) {
            plugin.getLogger()
                  .warning("Произошла ошибка при инициализации ConditionParseRegistry: " + exception.getMessage());
        }
    }
}