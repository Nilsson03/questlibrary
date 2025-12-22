package ru.nilsson03.library.quest.core.service;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.parser.registry.ConditionParserRegistry;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.Objects;

/**
 * Сервис, представляющих доступ к QuestManager и сопутствующим системам
 *
 * @see QuestManager
 * @see ConditionParserRegistry
 */
public class QuestService {

    private final ParserRegistry<? super Parser<QuestCondition>, QuestCondition> conditionParseRegistry;
    private final ObjectiveRegistry objectiveRegistry;
    private final Plugin plugin;
    private QuestManager questManager;

    /**
     * @param plugin                        плагин, для которого инициализируется сервис квестов
     * @param questUsersStorage             реализация хранения данных пользователей
     * @param customConditionParserRegistry кастомный регистер парсеров условий для выполнения квестов
     * @param customQuestMetaParserRegistry кастомный регистер парсеров мета-данных квестов
     */
    public QuestService(
            Plugin plugin, QuestUsersStorage questUsersStorage,
            ParserRegistry<? super Parser<QuestCondition>, QuestCondition> customConditionParserRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        
        this.conditionParseRegistry = Objects.requireNonNull(customConditionParserRegistry,
                                                             "customConditionParserRegistry cannot be null");
        this.objectiveRegistry = new ObjectiveRegistry();
        
        this.objectiveRegistry.onRegistryInit();
        this.conditionParseRegistry.onRegistryInit();

        if (questUsersStorage != null) {
            this.questManager = new QuestManager(plugin, questUsersStorage, objectiveRegistry);
        }
    }

    public QuestService(Plugin plugin, QuestUsersStorage questUsersStorage) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");

        String pluginName = plugin.getName();
        
        this.objectiveRegistry = new ObjectiveRegistry();
        this.conditionParseRegistry = new ConditionParserRegistry(plugin, pluginName);
        
        this.objectiveRegistry.onRegistryInit();
        this.conditionParseRegistry.onRegistryInit();

        if (questUsersStorage != null) {
            this.questManager = new QuestManager(plugin, questUsersStorage, objectiveRegistry);
        }
    }
    
    public void initializeQuestManager(QuestUsersStorage questUsersStorage) {
        if (this.questManager == null) {
            this.questManager = new QuestManager(plugin, questUsersStorage, objectiveRegistry);
        }
    }
    
    public void registerEventHandlers() {
        if (questManager != null) {
            questManager.registerEventHandlers();
        }
    }

    public ParserRegistry<? super Parser<QuestCondition>, QuestCondition> getConditionParserRegistry() {
        return conditionParseRegistry;
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public QuestManager getQuestManager() {
        if (questManager == null) {
            throw new IllegalStateException("QuestManager not initialized. Call initializeQuestManager() first.");
        }
        return questManager;
    }

    public ObjectiveRegistry getObjectiveRegistry() {
        return objectiveRegistry;
    }
}
