package ru.nilsson03.library.quest.user.data.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.file.DirectoryHelper;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.parser.BaseProgressParser;
import ru.nilsson03.library.quest.objective.progress.saver.BaseProgressSaver;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestSubjectKind;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;

public class FileUserPersistent implements UserDataPersistent {

    private final DirectoryHelper directoryHelper;
    private final NPlugin plugin;
    private final QuestStorage questStorage;
    private final Parser<QuestProgress> questProgressParser;
    private final ProgressSaver progressSaver;
    private final DirectoryHelper.Directory usersDirectory;
    private final Map<UUID, Object> fileLocks = new ConcurrentHashMap<>();

    public FileUserPersistent(NPlugin plugin,
            QuestStorage questStorage,
            ObjectiveGoalFactoryRegistry objectiveGoalFactoryRegistry) {
        this.plugin = plugin;
        this.directoryHelper = DirectoryHelper.of(plugin);
        this.questStorage = questStorage;
        this.questProgressParser = new BaseProgressParser(questStorage, objectiveGoalFactoryRegistry);
        this.progressSaver = new BaseProgressSaver();
        this.usersDirectory = directoryHelper.getOrLoad("users");
        if (this.usersDirectory == null) {
            throw new NullPointerException("Users directory not found, class FileUserDataStorage");
        }
    }

    private static String toUserFileName(UUID uuid) {
        return uuid.toString() + ".yml";
    }

    @Override
    public void saveUserData(QuestUserData userData) {
        UUID uuid = userData.uuid();
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {
            String userFileName = toUserFileName(uuid);
            FileConfiguration config;
            if (usersDirectory.contains(userFileName)) {
                config = usersDirectory.get(userFileName);
            } else {
                config = directoryHelper.create(usersDirectory, userFileName);
                if (config == null) {
                    throw new NullPointerException("Не удалось создать пустую конфигурацию для игрока " + userFileName);
                }
            }

            config.set("uuid", userData.uuid().toString());
            config.set("subject_kind", userData.subjectKind().name());

            config.set("completed_quests", userData.completeQuests().stream()
                    .map(q -> q.questUniqueKey().getKey())
                    .toList());

            userData.completeQuests().forEach(quest -> {
                String questKey = quest.questUniqueKey().getKey();
                if (!config.contains("quest_completion_times." + questKey)) {
                    config.set("quest_completion_times." + questKey, System.currentTimeMillis());
                }
            });

            config.set("active_progresses", null);

            userData.getActiveQuests().forEach(progress -> {
                BaseQuest quest = progress.quest();
                Namespace namespace = quest.questUniqueKey();
                ConfigurationSection configurationSection = config
                        .createSection("active_progresses." + namespace.getKey());

                progressSaver.save(progress, configurationSection);
            });

            config.set("receipts_rewards", null);

            usersDirectory.save(userFileName);
        }
    }

    @Override
    public QuestUserData loadUserData(UUID uuid) {
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {

            String userFileName = toUserFileName(uuid);
            if (!usersDirectory.contains(userFileName)) {
                return new BaseQuestUserData(uuid,
                        new ArrayList<>(),
                        new ArrayList<>());
            }

            FileConfiguration config = usersDirectory.get(userFileName);

            List<QuestProgress> questProgressList = new ArrayList<>();

            List<BaseQuest> completedQuests = new ArrayList<>(config.getStringList("completed_quests")
                    .stream()
                    .map(questStorage::getQuestByUniqueKeyOrThrow)
                    .toList());

            QuestSubjectKind subjectKind = QuestSubjectKind.PLAYER;
            String rawKind = config.getString("subject_kind");
            if (rawKind != null && !rawKind.isEmpty()) {
                try {
                    subjectKind = QuestSubjectKind.valueOf(rawKind);
                } catch (IllegalArgumentException ignored) {
                    subjectKind = QuestSubjectKind.PLAYER;
                }
            }

            QuestUserData userData = new BaseQuestUserData(uuid,
                    subjectKind,
                    completedQuests,
                    new ArrayList<>());

            if (config.contains("active_progresses")) {
                ConfigurationSection configurationSection = config.getConfigurationSection("active_progresses");

                for (String keyQuest : configurationSection.getKeys(false)) {

                    ConfigurationSection progressSection = configurationSection.getConfigurationSection(keyQuest);
                    if (progressSection == null) {
                        continue;
                    }

                    QuestProgress questProgress = ((BaseProgressParser) questProgressParser).parse(progressSection,
                            userData);
                    questProgressList.add(questProgress);
                }
            }

            userData.addActiveQuests(questProgressList);
            return userData;
        }
    }

    @Override
    public void deleteUserData(UUID uuid) {
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {
            String userFileName = toUserFileName(uuid);
            if (usersDirectory.contains(userFileName)) {
                directoryHelper.delete(usersDirectory, userFileName);
                ConsoleLogger.info(plugin, "Данные игрока %s были успешно удалены.", userFileName);
                fileLocks.remove(uuid);
            }
        }
    }

    @Override
    public long getQuestCompletionTime(UUID uuid, String questKey) {
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {
            String userFileName = toUserFileName(uuid);
            if (!usersDirectory.contains(userFileName)) {
                return 0;
            }

            FileConfiguration config = usersDirectory.get(userFileName);
            return config.getLong("quest_completion_times." + questKey, 0);
        }
    }

    @Override
    public CompletableFuture<Void> deleteQuestData(UUID uuid, String questKey) {
        return CompletableFuture.runAsync(() -> {
            Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
            synchronized (lock) {
                String userFileName = toUserFileName(uuid);
                if (!usersDirectory.contains(userFileName)) {
                    return;
                }

                FileConfiguration config = usersDirectory.get(userFileName);

                List<String> completedQuests = new ArrayList<>(config.getStringList("completed_quests"));
                completedQuests.remove(questKey);
                config.set("completed_quests", completedQuests);
                config.set("active_progresses." + questKey, null);
                config.set("quest_completion_times." + questKey, null);

                usersDirectory.save(userFileName);
                ConsoleLogger.debug(plugin.getName(), "Deleted quest data for user %s, quest %s", uuid, questKey);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteQuestDataByKeys(Collection<String> questKeys) {
        if (questKeys == null || questKeys.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            for (String fileName : usersDirectory.getFileNames()) {
                FileConfiguration config = usersDirectory.get(fileName);
                if (config == null) {
                    continue;
                }
                List<String> completedQuests = new ArrayList<>(config.getStringList("completed_quests"));
                boolean changed = false;
                for (String questKey : questKeys) {
                    if (completedQuests.remove(questKey)) {
                        changed = true;
                    }
                    if (config.contains("active_progresses." + questKey)) {
                        config.set("active_progresses." + questKey, null);
                        changed = true;
                    }
                    if (config.contains("quest_completion_times." + questKey)) {
                        config.set("quest_completion_times." + questKey, null);
                        changed = true;
                    }
                }
                if (changed) {
                    config.set("completed_quests", completedQuests);
                    usersDirectory.save(fileName);
                }
            }
        });
    }
}
