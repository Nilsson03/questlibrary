package ru.nilsson03.library.quest.core.service;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.QuestLibrary;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.parser.registry.ConditionParserRegistry;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.registry.MetaParserRegistry;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.registry.QuestProgressRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.parser.register.RewardParserRegistry;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис, представляющих доступ к QuestManager и сопутствующим системам
 * Данный сервис может быть инициализирован ОДИН раз для каждого плагина, в целях корректной работы
 *
 * @see QuestManager
 * @see ConditionParserRegistry
 * @see MetaParserRegistry
 */
public class QuestService {

    private final ParserRegistry<? super Parser<QuestReward>, QuestReward> questRewardParserRegistry;
    private final ParserRegistry<? super Parser<QuestMeta>, QuestMeta> questMetaParserRegistry;
    private final ParserRegistry<? super Parser<QuestCondition>, QuestCondition> conditionParseRegistry;
    private final QuestProgressRegistry questProgressRegistry;
    private final ObjectiveRegistry objectiveRegistry;
    private static final Map<Plugin, QuestService> initializationMap = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private QuestManager questManager;
    private final QuestStorage questStorage;

    /**
     * @param plugin                        плагин, для которого инициализируется сервис квестов
     * @param questUsersStorage             реализация хранения данных пользователей
     * @param customConditionParserRegistry кастомный регистер парсеров условий для выполнения квестов
     * @param customQuestMetaParserRegistry кастомный регистер парсеров мета-данных квестов
     */
    public QuestService(
            Plugin plugin, QuestUsersStorage questUsersStorage,
            ParserRegistry<? super Parser<QuestCondition>, QuestCondition> customConditionParserRegistry,
            ParserRegistry<? super Parser<QuestMeta>, QuestMeta> customQuestMetaParserRegistry,
            ParserRegistry<? super Parser<QuestReward>, QuestReward> customQuestRewardParserRegistry,
            QuestProgressRegistry questProgressRegistry) throws IllegalStateException {
        this.plugin = plugin;

        this.conditionParseRegistry = Objects.requireNonNull(customConditionParserRegistry,
                                                             "customConditionParserRegistry cannot be null");
        this.questMetaParserRegistry = Objects.requireNonNull(customQuestMetaParserRegistry,
                                                              "customQuestMetaParserRegistry cannot be null");
        this.questRewardParserRegistry = Objects.requireNonNull(customQuestRewardParserRegistry,
                                                                "customQuestRewardParserRegistry cannot be null");
        this.questProgressRegistry = Objects.requireNonNull(questProgressRegistry,
                                                                  "questProgressParserRegistry cannot be null");
        this.objectiveRegistry = new ObjectiveRegistry();

        this.questStorage = QuestLibrary.getApi()
                                    .getQuestStorage(plugin);

        initialize(plugin, questUsersStorage);
    }

    public QuestService(Plugin plugin, QuestUsersStorage questUsersStorage) throws IllegalStateException {
        this.plugin = plugin;

        this.conditionParseRegistry = new ConditionParserRegistry(this);
        this.questMetaParserRegistry = new MetaParserRegistry();
        this.questRewardParserRegistry = new RewardParserRegistry();
        this.objectiveRegistry = new ObjectiveRegistry();

        ObjectiveGoalFactoryRegistry objectiveGoalFactoryRegistry = objectiveRegistry.getObjectiveGoalRegistry();

        this.questStorage = QuestLibrary.getApi()
                                    .getQuestStorage(plugin);

        this.questProgressRegistry = new QuestProgressRegistry(questStorage, objectiveGoalFactoryRegistry,
                                                                           questUsersStorage);

        initialize(plugin, questUsersStorage);
    }

    public void registerMetaParser(Parser<QuestMeta> parser, String uniqueParserKey) {
        questMetaParserRegistry.registerParser(plugin.getName(), uniqueParserKey, parser);
    }

    public void registerConditionParser(Parser<QuestCondition> parser, String uniqueParserKey) {
        conditionParseRegistry.registerParser(plugin.getName(), uniqueParserKey, parser);
    }

    public void registerRewardParser(Parser<QuestReward> parser, String uniqueParserKey) {
        questRewardParserRegistry.registerParser(plugin.getName(), uniqueParserKey, parser);
    }

    public void registerProgressSaver(Parser<QuestProgress> parser, String uniqueParserKey) {
        questProgressRegistry.registerParser(plugin.getName(), uniqueParserKey, parser);
    }

    public Parser<QuestMeta> getMetaParser(String uniqueParserKey) {
        return questMetaParserRegistry.getParser(uniqueParserKey);
    }

    public Parser<QuestCondition> getConditionParser(String uniqueParserKey) {
        return conditionParseRegistry.getParser(uniqueParserKey);
    }

    public Parser<QuestReward> getRewardParser(String uniqueParserKey) {
        return questRewardParserRegistry.getParser(uniqueParserKey);
    }

    public Parser<QuestProgress> getProgressParser(String uniqueParserKey) {
        return questProgressRegistry.getParser(uniqueParserKey);
    }

    public void registerProgressSaver(ProgressSaver progressSaver, String uniqueParserKey) {
        questProgressRegistry.registerSaver(uniqueParserKey, progressSaver);
    }

    public ProgressSaver getProgressSaver(String uniqueParserKey) {
        return questProgressRegistry.getSaver(uniqueParserKey);
    }

    private synchronized void initialize(Plugin plugin, QuestUsersStorage questUsersStorage) throws
                                                                                             IllegalStateException {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(questUsersStorage, "questUsersStorage cannot be null");

        if (initializationMap.containsKey(plugin)) {
            throw new IllegalStateException(
                    "Error on loading QuestService. Quest service for plugin " + plugin.getName() + " is already initialized.");
        }

        this.questManager = new QuestManager(plugin, questUsersStorage, objectiveRegistry);

        conditionParseRegistry.onRegistryInit();
        questMetaParserRegistry.onRegistryInit();
        questRewardParserRegistry.onRegistryInit();
        objectiveRegistry.onRegistryInit();
        questProgressRegistry.onRegistryInit();

        initializationMap.put(plugin, this);
    }

    public Plugin getPlugin() {
        return plugin;
    }

    public QuestManager getQuestManager() {
        return questManager;
    }

    public ObjectiveRegistry getObjectiveRegistry() {
        return objectiveRegistry;
    }
}
