package ru.nilsson03.library.quest.condition.parser.registry;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.parser.impl.AndConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.HasItemConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.OrConditionParser;
import ru.nilsson03.library.quest.condition.parser.impl.QuestCompletedConditionParser;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.exception.QuestStorageException;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;

/**
 * Класс ConditionParserRegistry отвечает за хранение и управление парсерами условий.
 * Он позволяет регистрировать новые парсеры и использовать их для разбора условий из конфигурации.
 * Доступ к классу можно получить через сервис квестов, который должен быть инициализирован для вашего плагина
 *
 * @see QuestService
 */
public class ConditionParserRegistry extends ParserRegistry<Parser<QuestCondition>, QuestCondition> {

    private final QuestService questService;

    public ConditionParserRegistry(QuestService questService) {
        this.questService = questService;
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
        registerParser("questApi", "has_item", new HasItemConditionParser());
        registerParser("questApi", "and", new AndConditionParser(this));
        registerParser("questApi", "or", new OrConditionParser(this));
        try {
            registerParser("questApi", "quest_completed", new QuestCompletedConditionParser(questService));
        } catch (QuestStorageException exception) {
            questService.getPlugin()
                        .getLogger()
                        .warning(
                                "Произошла ошибка при инициализации ConditionParseRegistry: " + exception.getMessage());
        }
    }
}