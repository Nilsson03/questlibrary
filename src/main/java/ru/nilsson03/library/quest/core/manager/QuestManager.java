package ru.nilsson03.library.quest.core.manager;

import java.util.function.Consumer;

import lombok.Getter;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.core.service.QuestLifecycleService;
import ru.nilsson03.library.quest.core.service.QuestProgressService;
import ru.nilsson03.library.quest.core.service.QuestUpdateService;
import ru.nilsson03.library.quest.handler.QuestEventManager;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.objective.factory.registry.QuestProgressFactoryRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;
import ru.nilsson03.library.quest.tracker.MovementTracker;

@Getter
public class QuestManager {

    private final NPlugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final QuestLifecycleService questLifecycleService;
    private final QuestEventManager questEventManager;
    private final QuestProgressService questProgressService;
    private final MovementTracker movementTracker;
    private QuestUpdateService questUpdateService;

    /**
     * Конструктор класса
     * Во время инициализации класса инициализируется менеджер, который так же принимает в качестве параметров javaPlugin, а так же
     * хранилище игроков
     *
     * @param plugin            плагин, к которому должен относится данный менеджер
     * @param questUsersStorage реализация хранилища игроков
     * @see QuestEventManager
     */
    public QuestManager(NPlugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.questEventManager = new QuestEventManager(plugin, questUsersStorage, objectiveRegistry);

        QuestProgressFactoryRegistry factoryRegistry = new QuestProgressFactoryRegistry();
        QuestCompleterRegistry questCompleterRegistry = new QuestCompleterRegistry(questUsersStorage);
        questCompleterRegistry.onRegisterInit();

        this.questProgressService = new QuestProgressService(factoryRegistry);
        this.questLifecycleService = new QuestLifecycleService(questProgressService, questCompleterRegistry);
        
        this.movementTracker = new MovementTracker(plugin, questUsersStorage, objectiveRegistry);
    }

    public void registerEventHandlers() {
        this.questEventManager.register();
        this.movementTracker.start();
    }
    
    public void initializeQuestUpdateService(QuestStorage questStorage) {
        if (this.questUpdateService == null && questStorage != null) {
            this.questUpdateService = new QuestUpdateService(
                this.plugin,
                this.questUsersStorage,
                this.questUsersStorage.getUserDataPersistent(),
                questStorage
            );
        }
    }
    
    public void startQuestUpdateService(QuestStorage questStorage) {
        if (questStorage == null) {
            return;
        }
        
        if (this.questUpdateService == null) {
            initializeQuestUpdateService(questStorage);
        }
        
        if (this.questUpdateService != null) {
            this.questUpdateService.start();
        }
    }
    
    public void shutdown() {
        if (this.movementTracker != null) {
            this.movementTracker.stop();
        }
        
        if (this.questUpdateService != null) {
            this.questUpdateService.stop();
        }
    }

    /**
     * Используется для завершения квеста игроком
     *
     * @param user                  игрок
     * @param quest                 квест
     * @param questUserDataConsumer дополнительные действия, которые могут быть совершены с игроком
     */
    public CompleteStatus completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        return questLifecycleService.completeQuest(user, quest, questUserDataConsumer);
    }

    /**
     * Используется для начала выполнения квеста игроком
     *
     * @param user                  игрок, который должен начать выполнять
     * @param quest                 квест, который будет выполнять игрок
     * @param questUserDataConsumer дополнительные действия, которые могут быть связаны с игроком
     */
    public void startQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        questLifecycleService.startQuest(user, quest, questUserDataConsumer);
    }
}
