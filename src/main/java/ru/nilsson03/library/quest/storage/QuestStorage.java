package ru.nilsson03.library.quest.storage;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.bukkit.plugin.Plugin;

import com.google.common.base.Preconditions;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;

public class QuestStorage {

    private final Plugin plugin;
    private final List<BaseQuest> quests;
    private final QuestLoader questLoader;

    /**
     * Конструктор для создания объекта QuestStorage.
     *
     * @param plugin      Плагин, для которого создается хранилище квестов.
     * @param questLoader загрузчик квестов.
     * @throws RuntimeException если не удалось инициализировать загрузчик квестов.
     */
    public QuestStorage(final Plugin plugin, final QuestLoader questLoader) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        Preconditions.checkNotNull(questLoader, "Quest loader class cannot be null");
        this.questLoader = questLoader;
        this.quests = new ArrayList<>();
    }

    public void loadQuests() {
        loadQuests("quests");
    }

    public void loadQuests(String relativeFolder) {
        Objects.requireNonNull(relativeFolder, "relativeFolder cannot be null");
        Path questsFolderPath = plugin.getDataFolder().toPath().resolve(relativeFolder);
        ConsoleLogger.info(plugin.getName(), "Quests folder Path %s", questsFolderPath);

        try {
            Files.createDirectories(questsFolderPath);
            File questsDataFolder = questsFolderPath.toFile();
            List<BaseQuest> loadedQuests = questLoader.loadQuests(questsDataFolder);

            List<BaseQuest> validQuests = loadedQuests.stream()
                    .filter(Objects::nonNull)
                    .toList();

            registerQuests(validQuests);

            int failedCount = loadedQuests.size() - validQuests.size();
            if (failedCount > 0) {
                ConsoleLogger.warn(plugin.getName(),
                        "Failed to load %d quests due to errors", failedCount);
            }

            ConsoleLogger.info(plugin.getName(), "Loaded %s quests from %s", validQuests.size(), relativeFolder);
        } catch (Exception exception) {
            ConsoleLogger.error(plugin.getName(),
                    "Failed to load quests from %s: %s", questsFolderPath, exception.getMessage());
        }
    }

    /**
     * Registers quest definitions for key lookup (e.g. daily pool into main storage).
     */
    public void registerQuests(java.util.Collection<? extends BaseQuest> questsToRegister) {
        Objects.requireNonNull(questsToRegister, "questsToRegister cannot be null");
        for (BaseQuest quest : questsToRegister) {
            if (quest == null) {
                continue;
            }
            boolean alreadyPresent = quests.stream()
                    .anyMatch(existing -> existing.questUniqueKey().equals(quest.questUniqueKey()));
            if (!alreadyPresent) {
                this.quests.add(quest);
            }
        }
    }

    /**
     * Удаляет квест по уникальному ключу. Если найдено более одного квеста с таким
     * ключом,
     * удаляет все такие квесты и выводит предупреждение в лог. Если квест не
     * найден,
     * выбрасывает исключение.
     *
     * @param key Уникальный ключ квеста.
     * @throws IllegalArgumentException если квест не найден.
     */
    public void removeQuestOrThrow(final Namespace key) {
        Preconditions.checkArgument(key != null, "Quest key cannot be null");

        List<BaseQuest> matchingQuests = quests.stream()
                .filter(Objects::nonNull)
                .filter(quest -> key.equals(quest.questUniqueKey()))
                .toList();

        if (matchingQuests.isEmpty()) {
            throw new IllegalArgumentException("No quests found with key: " + key + " in plugin: " + plugin.getName());
        }

        if (matchingQuests.size() > 1) {
            plugin.getLogger()
                    .warning(
                            "Found more than one quest with key: " + key + " in plugin: " + plugin.getName()
                                    + ". Removed all quests with identify keys");
        }

        quests.removeAll(matchingQuests);
    }

    /**
     * Возвращает квест по уникальному ключу. Если найдено более одного квеста с
     * таким ключом,
     * выводит предупреждение в лог и возвращает первый найденный квест.
     *
     * @param key Уникальный ключ квеста.
     * @return Найденный квест или null, если квест не найден.
     */
    protected BaseQuest getQuestByUniqueKey(final Namespace key) {
        List<BaseQuest> matchingQuests = quests.stream()
                .filter(quest -> key.equals(quest.questUniqueKey()))
                .toList();

        if (matchingQuests.size() > 1) {
            plugin.getLogger()
                    .warning(
                            "Found more than one quest with key: " + key + " in plugin: " + plugin.getName()
                                    + ". Selected first found.");
        }

        return matchingQuests.stream()
                .findFirst()
                .orElse(null);
    }

    /**
     * Возвращает квест по уникальному ключу. Если квест не найден, выбрасывает
     * исключение.
     *
     * @param key Уникальный ключ квеста.
     * @return Найденный квест.
     * @throws IllegalArgumentException если квест не найден.
     */
    public BaseQuest getQuestByUniqueKeyOrThrow(String key) {
        Namespace questNamespace = Namespace.of(plugin.getName(), key);
        BaseQuest quest = getQuestByUniqueKey(questNamespace);
        Preconditions.checkArgument(quest != null,
                "No quest found with key: " + key + " in plugin: " + plugin.getName());
        return quest;
    }

    protected Plugin getPlugin() {
        return plugin;
    }

    public List<BaseQuest> getQuests() {
        return quests;
    }
}
