package ru.nilsson03.library.quest.storage;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.bukkit.file.FileHelper;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class QuestStorage {

    private final Plugin plugin;
    private final List<Quest> quests;

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

        this.quests = new ArrayList<>();

        String questsFolderPath = Paths.get(plugin.getDataFolder()
                                                  .getPath(), "quests")
                                       .toString();

        try {
            File questsDataFolder = FileHelper.getOrCreateDirectory(questsFolderPath);
            this.quests.addAll(questLoader.loadQuests(questsDataFolder));
        } catch (Exception exception) {
            plugin.getLogger()
                  .severe("Failed to load quests from " + questsFolderPath);
        }
    }

    /**
     * Удаляет квест по уникальному ключу. Если найдено более одного квеста с таким ключом,
     * удаляет все такие квесты и выводит предупреждение в лог. Если квест не найден,
     * выбрасывает исключение.
     *
     * @param key Уникальный ключ квеста.
     * @throws IllegalArgumentException если квест не найден.
     */
    public void removeQuestOrThrow(final QuestNamespace key) {
        Preconditions.checkArgument(key != null, "Quest key cannot be null");

        List<Quest> matchingQuests = quests.stream()
                                           .filter(Objects::nonNull)
                                           .filter(quest -> key.equals(quest.questUniqueKey()))
                                           .toList();

        if (matchingQuests.isEmpty()) {
            throw new IllegalArgumentException("No quests found with key: " + key + " in plugin: " + plugin.getName());
        }

        if (matchingQuests.size() > 1) {
            plugin.getLogger()
                  .warning(
                          "Found more than one quest with key: " + key + " in plugin: " + plugin.getName() + ". Removed all quests with identify keys");
        }

        quests.removeAll(matchingQuests);
    }

    /**
     * Возвращает квест по уникальному ключу. Если найдено более одного квеста с таким ключом,
     * выводит предупреждение в лог и возвращает первый найденный квест.
     *
     * @param key Уникальный ключ квеста.
     * @return Найденный квест или null, если квест не найден.
     */
    protected Quest getQuestByUniqueKey(final QuestNamespace key) {
        List<Quest> matchingQuests = quests.stream()
                                           .filter(quest -> key.equals(quest.questUniqueKey()))
                                           .toList();

        if (matchingQuests.size() > 1) {
            plugin.getLogger()
                  .warning(
                          "Found more than one quest with key: " + key + " in plugin: " + plugin.getName() + ". Selected first found.");
        }

        return matchingQuests.stream()
                             .findFirst()
                             .orElse(null);
    }

    /**
     * Возвращает квест по уникальному ключу. Если квест не найден, выбрасывает исключение.
     *
     * @param key Уникальный ключ квеста.
     * @return Найденный квест.
     * @throws IllegalArgumentException если квест не найден.
     */
    public Quest getQuestByUniqueKeyOrThrow(final String key) {
        QuestNamespace questNamespace = QuestNamespace.of(key);
        Quest quest = getQuestByUniqueKey(questNamespace);
        Preconditions.checkArgument(quest != null,
                                    "No quest found with key: " + key + " in plugin: " + plugin.getName());
        return quest;
    }

    protected Plugin getPlugin() {
        return plugin;
    }

    public List<Quest> getQuests() {
        return quests;
    }
}
