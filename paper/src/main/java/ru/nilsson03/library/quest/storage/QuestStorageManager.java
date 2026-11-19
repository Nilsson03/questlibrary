package ru.nilsson03.library.quest.storage;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.exception.QuestStorageDuplicateException;
import ru.nilsson03.library.quest.exception.QuestStorageNotLoadedException;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Класс-менеджер, представляющий методы для управления хранилищами квестов.
 * Не рекомендуется к использованию в асинхронной среде!
 *
 * @see QuestStorage
 */
public class QuestStorageManager {

    private final List<QuestStorage> questStorages;

    private static boolean initialized = false;

    /**
     * Конструктор по умолчанию. Инициализирует список хранилищ квестов.
     *
     * @throws IllegalStateException если класс уже инициализирован.
     */
    public QuestStorageManager() {
        if (initialized) {
            throw new IllegalStateException("QuestStorageManager already initialized");
        }

        this.questStorages = new CopyOnWriteArrayList<>();

        initialized = true;
    }

    private boolean addNewQuestStorageToList(final QuestStorage questStorage) {
        Objects.requireNonNull(questStorage, "QuestStorage cannot be null");

        return questStorages.add(questStorage);
    }

    /**
     * Загружает хранилище квестов для указанного плагина.
     *
     * @param plugin Плагин, для которого загружается хранилище квестов.
     * @return Загруженное хранилище квестов.
     * @throws QuestStorageNotLoadedException если хранилище квестов не удалось загрузить.
     * @throws QuestStorageDuplicateException если хранилище квестов уже загружено и не пусто.
     */
    public QuestStorage loadQuestStorage(final Plugin plugin, final QuestLoader questsLoaderClass) throws
                                                                                                   QuestStorageNotLoadedException,
                                                                                                   QuestStorageDuplicateException {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        if (isQuestStorageLoadedAndNotEmpty(plugin)) {
            throw new QuestStorageDuplicateException(
                    "Quest storage for plugin '" + plugin.getName() + "' is already loaded and not empty.");
        }

        QuestStorage questStorage = new QuestStorage(plugin, questsLoaderClass);

        boolean result = addNewQuestStorageToList(questStorage);

        if (result) {
            plugin.getLogger()
                  .info("Successfully loaded quest storage for plugin: " + plugin.getName());
        } else {
            plugin.getLogger()
                  .warning("Failed to load quest storage for plugin: " + plugin.getName());
            throw new QuestStorageNotLoadedException("Failed to load quest storage for plugin: " + plugin.getName());
        }

        return questStorage;
    }

    /**
     * Возвращает хранилище квестов для указанного плагина.
     *
     * @param plugin Плагин, для которого нужно получить хранилище квестов.
     * @return Хранилище квестов.
     * @throws QuestStorageNotLoadedException если хранилище квестов не загружено для указанного плагина.
     * @throws QuestStorageDuplicateException если найдено более одного хранилища квестов для указанного плагина.
     */
    public QuestStorage getQuestStorageByPlugin(final Plugin plugin) throws QuestStorageNotLoadedException,
                                                                            QuestStorageDuplicateException {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        List<QuestStorage> questStoragesForPlugin = findQuestStoragesByPlugin(plugin);

        if (questStoragesForPlugin.isEmpty()) {
            throw new QuestStorageNotLoadedException("Quest storage not loaded for plugin: " + plugin.getName());
        }

        if (questStoragesForPlugin.size() > 1) {
            throw new QuestStorageDuplicateException(
                    "More than one quest storage found for plugin: " + plugin.getName());
        }

        return questStoragesForPlugin.get(0);
    }

    /**
     * Проверяет, загружено ли хранилище квестов для указанного плагина и содержит ли оно квесты.
     *
     * @param plugin Плагин, для которого выполняется проверка.
     * @return true, если хранилище загружено и содержит квесты, иначе false.
     */
    public boolean isQuestStorageLoadedAndNotEmpty(final Plugin plugin) {
        Objects.requireNonNull(plugin, "Plugin cannot be null");

        Optional<QuestStorage> questStorageOptional = findQuestStorageByPlugin(plugin);

        if (questStorageOptional.isEmpty()) {
            return false;
        }

        QuestStorage questStorage = questStorageOptional.get();

        return !questStorage.getQuests()
                            .isEmpty();
    }

    /**
     * Ищет хранилища квестов для указанного плагина.
     *
     * @param plugin Плагин, для которого выполняется поиск.
     * @return Список найденных хранилищ квестов.
     */
    private List<QuestStorage> findQuestStoragesByPlugin(final Plugin plugin) {
        return questStorages.stream()
                            .filter(questStorage -> questStorage.getPlugin() == plugin)
                            .toList();
    }

    /**
     * Ищет хранилище квестов для указанного плагина.
     *
     * @param plugin Плагин, для которого выполняется поиск.
     * @return Optional с найденным хранилищем квестов.
     */
    private Optional<QuestStorage> findQuestStorageByPlugin(final Plugin plugin) {
        return questStorages.stream()
                            .filter(questStorage -> questStorage.getPlugin() == plugin)
                            .findFirst();
    }
}
