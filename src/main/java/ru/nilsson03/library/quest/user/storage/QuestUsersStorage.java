package ru.nilsson03.library.quest.user.storage;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.exception.QuestAlreadyCompletedException;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class QuestUsersStorage {

    private static final Map<Plugin, QuestUsersStorage> initializationMap = new ConcurrentHashMap<>();

    private final UserDataPersistent userDataPersistent;

    private final Map<UUID, QuestUserData> usersData;

    public QuestUsersStorage(NPlugin plugin,
                             UserDataPersistent userDataPersistent) {

        if (initializationMap.containsKey(plugin)) {
            throw new IllegalStateException(
                    "Error on loading QuestService. Quest service for plugin " + plugin.getName() + " is already initialized.");
        }

        this.usersData = new ConcurrentHashMap<>();
        this.userDataPersistent = userDataPersistent;

        initializationMap.put(plugin, this);
    }

    /**
     * Добавление данных игрока в коллекцию
     *
     * @param questUserData объект, представляющий данный игрока
     */
    public void loadQuestUserData(QuestUserData questUserData) {
        Objects.requireNonNull(questUserData, "QuestUserData is null");
        usersData.putIfAbsent(questUserData.uuid(), questUserData);
    }

    /**
     * Добавление игроку нового выполненного квестов
     *
     * @param uuid  идентификатор пользователя
     * @param quest квест, который игрок выполнил
     */
    public void addCompleteQuest(UUID uuid, Quest quest) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(quest, "quest cannot be null");

        QuestUserData questUserData = getQuestUserData(uuid);
        if (questUserData == null) {
            throw new IllegalArgumentException("QuestUserData not found for UUID: " + uuid);
        }

        questUserData.addCompletedQuest(quest);
    }

    /**
     * Добавление игроку список новых выполненных квестов
     *
     * @param uuid   идентификатор пользователя
     * @param quests список квестов, которые необходимо пометить, как выполенные
     */
    public void addCompleteQuests(UUID uuid, List<Quest> quests) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(quests, "quests cannot be null");

        if (quests.isEmpty()) {
            return;
        }

        QuestUserData questUserData = getQuestUserData(uuid);
        if (questUserData == null) {
            throw new IllegalArgumentException("QuestUserData not found for UUID: " + uuid);
        }

        for (Quest quest : quests) {
            questUserData.addCompletedQuest(quest);
        }
    }

    /**
     * Получение прогресса выполнения квеста у игрока
     *
     * @param uuid  идентификатор пользователя
     * @param quest квест, информацию о прогрессе выполнения которого необходимо получить
     * @return объект, представляющий прогресс прохождения игроком квеста
     */
    public QuestProgress getObjectiveProgress(UUID uuid, Quest quest) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(quest, "quest cannot be null");

        QuestUserData questUserData = getQuestUserData(uuid);
        if (questUserData == null) {
            throw new IllegalArgumentException("QuestUserData not found for UUID: " + uuid);
        }

        try {
            return questUserData.getProgressByQuestOrThrow(quest);
        } catch (QuestAlreadyCompletedException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Получение объекта, хранящего информацию об игроке
     *
     * @param uuid идентификатор пользователя
     * @return объект, представляющий игрока или же null
     */
    public QuestUserData getQuestUserData(UUID uuid) {
        return usersData.computeIfAbsent(uuid, this::loadData);
    }

    public QuestUserData loadData(UUID uuid) {
        return userDataPersistent.loadUserData(uuid);
    }

    public void saveData(QuestUserData userData) {
        userDataPersistent.saveUserData(userData);
    }

    public Plugin plugin() {
        return null;
    }
}
