package ru.nilsson03.library.quest.core.service;

import lombok.Getter;
import org.bukkit.Bukkit;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.condition.parser.registry.ConditionParserRegistry;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.meta.parser.registry.MetaParserRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.Objects;

/**
 * Сервис, представляющих доступ к QuestManager и сопутствующим системам
 *
 * @see QuestManager
 * @see ConditionParserRegistry
 */
@Getter
public class QuestService {

    private final ConditionParserRegistry conditionParserRegistry;
    private final MetaParserRegistry metaParserRegistry;
    private final ObjectiveRegistry objectiveRegistry;
    private final NPlugin plugin;
    private QuestManager questManager;

    /**
     * @param plugin                        плагин, для которого инициализируется сервис квестов
     * @param questUsersStorage             реализация хранения данных пользователей
     * @param customConditionParserRegistry кастомный регистер парсеров условий для выполнения квестов
     * @param customMetaParserRegistry      кастомный регистер парсеров метаданных квестов
     */
    public QuestService(
            NPlugin plugin, QuestUsersStorage questUsersStorage,
            ConditionParserRegistry customConditionParserRegistry,
            MetaParserRegistry customMetaParserRegistry) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");
        
        this.conditionParserRegistry = Objects.requireNonNull(customConditionParserRegistry,
                                                             "customConditionParserRegistry cannot be null");
        this.metaParserRegistry = Objects.requireNonNull(customMetaParserRegistry,
                                                         "customMetaParserRegistry cannot be null");
        this.objectiveRegistry = new ObjectiveRegistry();
        
        this.objectiveRegistry.onRegistryInit();
        this.conditionParserRegistry.onRegistryInit();
        this.metaParserRegistry.onRegistryInit();

        if (questUsersStorage != null) {
            this.questManager = new QuestManager(plugin, questUsersStorage, objectiveRegistry);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            this.conditionParserRegistry.onRegistryAfterInit();
            this.metaParserRegistry.onRegistryAfterInit();
        }, 300);
    }

    public QuestService(NPlugin plugin, QuestUsersStorage questUsersStorage) {
        this.plugin = Objects.requireNonNull(plugin, "plugin cannot be null");

        String pluginName = plugin.getName();
        
        this.objectiveRegistry = new ObjectiveRegistry();
        this.conditionParserRegistry = new ConditionParserRegistry(plugin);
        this.metaParserRegistry = new MetaParserRegistry(plugin);
        
        this.objectiveRegistry.onRegistryInit();
        this.conditionParserRegistry.onRegistryInit();
        this.metaParserRegistry.onRegistryInit();

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
}
