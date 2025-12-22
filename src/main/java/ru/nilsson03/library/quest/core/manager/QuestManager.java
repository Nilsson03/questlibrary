package ru.nilsson03.library.quest.core.manager;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.service.QuestLifecycleService;
import ru.nilsson03.library.quest.core.service.QuestProgressService;
import ru.nilsson03.library.quest.handler.QuestEventManager;
import ru.nilsson03.library.quest.objective.factory.registry.QuestProgressFactoryRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.function.Consumer;

/**
 * Менеджер для управления квестами, а так же остальными компонентами, которые к ним относятся
 *
 * @see ru.nilsson03.library.quest.core.service.QuestService инициализация данного менеджера
 */
public class QuestManager {

    private final QuestLifecycleService questLifecycleService;
    private final QuestEventManager questEventManager;

    /**
     * Конструктор класса
     * Во время инициализации класса инициализируется менеджер, который так же принимает в качестве параметров javaPlugin, а так же
     * хранилище игроков
     *
     * @param plugin            плагин, к которому должен относится данный менеджер
     * @param questUsersStorage реализация хранилища игроков
     * @see QuestEventManager
     */
    public QuestManager(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this.questEventManager = new QuestEventManager(plugin, questUsersStorage, objectiveRegistry);

        QuestProgressFactoryRegistry factoryRegistry = new QuestProgressFactoryRegistry();
        QuestCompleterRegistry questCompleterRegistry = new QuestCompleterRegistry(questUsersStorage);
        questCompleterRegistry.onRegisterInit();

        QuestProgressService questProgressService = new QuestProgressService(factoryRegistry);
        this.questLifecycleService = new QuestLifecycleService(questProgressService, questCompleterRegistry);
    }

    public void registerEventHandlers() {
        this.questEventManager.register();
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

    public QuestLifecycleService getQuestLifecycleService() {
        return questLifecycleService;
    }

    public QuestEventManager getQuestEventManager() {
        return questEventManager;
    }
}
