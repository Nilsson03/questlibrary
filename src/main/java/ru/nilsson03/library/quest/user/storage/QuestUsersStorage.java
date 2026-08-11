package ru.nilsson03.library.quest.user.storage;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.plugin.Plugin;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.exception.QuestAlreadyCompletedException;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestSubjectKind;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.data.impl.BaseQuestUserData;

public class QuestUsersStorage {

    private static final Map<Plugin, QuestUsersStorage> initializationMap = new ConcurrentHashMap<>();

    private final UserDataPersistent userDataPersistent;

    private final Map<UUID, QuestUserData> usersData;
    private final NPlugin plugin;

    public QuestUsersStorage(NPlugin plugin,
            UserDataPersistent userDataPersistent) {

        if (initializationMap.containsKey(plugin)) {
            throw new IllegalStateException(
                    "Error on loading QuestService. Quest service for plugin " + plugin.getName()
                            + " is already initialized.");
        }
        this.plugin = plugin;

        this.usersData = new ConcurrentHashMap<>();
        this.userDataPersistent = userDataPersistent;

        initializationMap.put(plugin, this);
    }

    /**
     * Удаляет данные игрока из кэша и базы данных
     * 
     * @param questUserData игрок
     */
    public void deleteUserData(QuestUserData questUserData) {
        Objects.requireNonNull(questUserData, "QuestUserData is null");
        UUID userId = questUserData.uuid();
        usersData.remove(userId);
        userDataPersistent.deleteUserData(userId);
        ConsoleLogger.info(plugin, "Данные игрока о квестах %s были удалены", userId);
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
    public void addCompleteQuest(UUID uuid, BaseQuest quest) {
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
    public void addCompleteQuests(UUID uuid, List<BaseQuest> quests) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(quests, "quests cannot be null");

        if (quests.isEmpty()) {
            return;
        }

        QuestUserData questUserData = getQuestUserData(uuid);
        if (questUserData == null) {
            throw new IllegalArgumentException("QuestUserData not found for UUID: " + uuid);
        }

        for (BaseQuest quest : quests) {
            questUserData.addCompletedQuest(quest);
        }
    }

    /**
     * Получение прогресса выполнения квеста у игрока
     *
     * @param uuid  идентификатор пользователя
     * @param quest квест, информацию о прогрессе выполнения которого необходимо
     *              получить
     * @return объект, представляющий прогресс прохождения игроком квеста
     */
    public QuestProgress getObjectiveProgress(UUID uuid, BaseQuest quest) {
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

    public QuestUserData getOrCreateGroupOwner(UUID uuid) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        return usersData.compute(uuid, (id, existing) -> {
            if (existing != null && existing.subjectKind() == QuestSubjectKind.GROUP) {
                return existing;
            }
            if (existing != null) {
                return new BaseQuestUserData(
                        id,
                        QuestSubjectKind.GROUP,
                        existing.completeQuests(),
                        existing.getActiveQuests());
            }
            QuestUserData loaded = loadData(id);
            if (loaded.subjectKind() == QuestSubjectKind.GROUP) {
                return loaded;
            }
            return new BaseQuestUserData(
                    id,
                    QuestSubjectKind.GROUP,
                    loaded.completeQuests(),
                    loaded.getActiveQuests());
        });
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

    public void saveAllData() {
        for (QuestUserData userData : usersData.values()) {
            saveData(userData);
        }
    }

    public UserDataPersistent getUserDataPersistent() {
        return userDataPersistent;
    }

    public Collection<QuestUserData> getAllLoadedUsers() {
        return usersData.values();
    }

    public void clearCachedQuestData(Collection<? extends BaseQuest> quests) {
        Objects.requireNonNull(quests, "quests cannot be null");
        if (quests.isEmpty()) {
            return;
        }
        for (QuestUserData userData : usersData.values()) {
            for (BaseQuest quest : quests) {
                userData.clearQuestState(quest);
            }
        }
    }

    public void clearCachedQuestData(UUID uuid, Collection<? extends BaseQuest> quests) {
        Objects.requireNonNull(uuid, "uuid cannot be null");
        Objects.requireNonNull(quests, "quests cannot be null");
        QuestUserData userData = usersData.get(uuid);
        if (userData == null || quests.isEmpty()) {
            return;
        }
        for (BaseQuest quest : quests) {
            userData.clearQuestState(quest);
        }
    }

    public static void clearInitialization(Plugin plugin) {
        if (plugin != null) {
            initializationMap.remove(plugin);
        }
    }
}
