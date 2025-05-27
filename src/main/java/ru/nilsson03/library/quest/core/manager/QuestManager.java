package ru.nilsson03.library.quest.core.manager;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.handler.QuestEventManager;
import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;
import ru.nilsson03.library.quest.objective.factory.registry.QuestProgressFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.Set;
import java.util.function.Consumer;

/**
 * Менеджер для управления квестами, а так же остальными компонентами, которые к ним относятся
 *
 * @see QuestService инициализация данного менеджера
 */
public class QuestManager {

    private final QuestProgressFactoryRegistry factoryRegistry;
    private final QuestCompleterRegistry questCompleterRegistry;

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

        this.questCompleterRegistry = new QuestCompleterRegistry(questUsersStorage);
        this.factoryRegistry = new QuestProgressFactoryRegistry();

        this.questCompleterRegistry.onRegisterInit();
    }

    /**
     * Метод для инициализации начального прогресса квеста для игрока
     *
     * @param questUserData игрок, для которого необходимо инициализировать прогресс
     * @param quest         квест, от которого берутся задачи
     * @return набор начального прогресса для установки в дату игрока
     */
    private Set<QuestProgress> createEmptyProgressForQuest(final QuestUserData questUserData, final Quest quest) {
        QuestProgressFactory factory = factoryRegistry.getFactory(quest);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for quest type: " + quest.getClass()
                                                                                          .getSimpleName());
        }
        return factory.createProgress(questUserData, quest);
    }

    /**
     * Используется для завершения квеста игроком
     *
     * @param user                  игрок
     * @param quest                 квест
     * @param questUserDataConsumer дополнительные действия, которые могут быть совершены с игроком
     */
    public void completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {

        if (user.questIsComplete(quest)) {
            return;
        }

        QuestCompleter completer = questCompleterRegistry.getCompleter(quest);
        if (completer == null) {
            throw new IllegalArgumentException("No completer found for quest type: " + quest.getClass()
                                                                                            .getSimpleName());
        }

        completer.completeQuest(user, quest, questUserDataConsumer);
    }

    /**
     * Используется для начала выполнения квеста игроком
     *
     * @param user                  игрок, который должен начать выполнять
     * @param quest                 квест, который будет выполнять игрок
     * @param questUserDataConsumer дополнительные действия, которые могут быть связаны с игроком
     */
    public void startQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {

        if (user.questIsComplete(quest)) {
            return;
        }

        Set<QuestProgress> objectiveProgressSet = createEmptyProgressForQuest(user, quest);

        user.addNewProgressFromSet(objectiveProgressSet);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }
    }

    private QuestEventManager getQuestEventManager() {
        return questEventManager;
    }

    public QuestCompleterRegistry getQuestCompleterRegistry() {
        return questCompleterRegistry;
    }

    public QuestProgressFactoryRegistry getFactoryRegistry() {
        return factoryRegistry;
    }
}
