package ru.nilsson03.library.quest.user.data.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.BukkitDirectory;
import ru.nilsson03.library.bukkit.file.FileRepository;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.parser.BaseProgressParser;
import ru.nilsson03.library.quest.objective.progress.saver.BaseProgressSaver;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class FileUserPersistent implements UserDataPersistent {

    private final FileRepository fileRepository;
    private final NPlugin plugin;
    private final QuestStorage questStorage;
    private final Parser<QuestProgress> questProgressParser;
    private final ProgressSaver progressSaver;
    private final BukkitDirectory usersDirectory;
    private final Map<UUID, Object> fileLocks = new ConcurrentHashMap<>();

    public FileUserPersistent(NPlugin plugin,
                              QuestStorage questStorage,
                              ObjectiveGoalFactoryRegistry objectiveGoalFactoryRegistry) {
        this.plugin = plugin;
        this.fileRepository = plugin.fileRepository();
        this.questStorage = questStorage;
        this.questProgressParser = new BaseProgressParser(questStorage, objectiveGoalFactoryRegistry);
        this.progressSaver = new BaseProgressSaver();
        Optional<BukkitDirectory> usersDirectoryOptional = fileRepository.getDirectoryOrLoad("users");

        if (usersDirectoryOptional.isEmpty())
            throw new NullPointerException("Users directory not found, class FileUserDataStorage");
        else
            this.usersDirectory = usersDirectoryOptional.get();
    }

    @Override
    public void saveUserData(QuestUserData userData) {
        UUID uuid = userData.uuid();
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {
            String userFileName = uuid.toString();
            BukkitConfig userFile;
            if (usersDirectory.containsFileWithName(userFileName)) {
                userFile = usersDirectory.getBukkitConfig(userFileName);
            } else {
                Optional<BukkitConfig> createdFile = fileRepository.create(usersDirectory, userFileName);
                if (createdFile.isPresent()) userFile = createdFile.get();
                else {
                    throw new NullPointerException("Не удалось создать пустую конфигурацию для игрока " + userFileName);
                }
            }

            FileConfiguration config = userFile.getFileConfiguration();

        config.set("uuid", userData.uuid().toString());

        config.set("completed_quests", userData.completeQuests().stream()
                .map(q -> q.questUniqueKey().getKey())
                .toList());

        userData.completeQuests().forEach(quest -> {
            String questKey = quest.questUniqueKey().getKey();
            if (!config.contains("quest_completion_times." + questKey)) {
                config.set("quest_completion_times." + questKey, System.currentTimeMillis());
            }
        });

        userData.getActiveQuests().forEach(progress -> {
            BaseQuest quest = progress.quest();
            Namespace namespace = quest.questUniqueKey();
            ConfigurationSection configurationSection = config.createSection("active_progresses." + namespace.getKey());

            progressSaver.save(progress, configurationSection);
        });

        if (userData.hasActiveReceiptsRewardsData()) {
            QuestUserReceiptsRewardsData receiptsRewardsData = userData.getReceiptsRewardsData();

            for (Map.Entry<UUID, Integer> entry : receiptsRewardsData.getTakenRewardsAndCount().entrySet()) {
                config.set("receipts_rewards." + entry.getKey().toString(), entry.getValue());
            }
        }

            userFile.saveConfiguration();
        }
    }

    @Override
    public QuestUserData loadUserData(UUID uuid) {
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {

        String userFileName = uuid.toString();
        BukkitConfig userFile;
        if (usersDirectory.containsFileWithName(userFileName)) {
            userFile = usersDirectory.getBukkitConfig(userFileName);
        } else {
            return new BaseQuestUserData(uuid,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new QuestUserReceiptsRewardsData());
        }

        FileConfiguration config = userFile.getFileConfiguration();

        List<QuestProgress> questProgressList = new ArrayList<>();

        Map<UUID, Integer> receiptsRewards = new HashMap<>();
        if (config.contains("receipts_rewards")) {
            for (String keyUUID : config.getConfigurationSection("receipts_rewards").getKeys(false)) {
                UUID rewardUUID = UUID.fromString(keyUUID);
                int takenCount = config.getInt("receipts_rewards." + keyUUID);
                receiptsRewards.put(rewardUUID, takenCount);
            }
        }

        QuestUserReceiptsRewardsData questUserReceiptsRewardsData = new QuestUserReceiptsRewardsData(receiptsRewards);

        List<BaseQuest> completedQuests = new ArrayList<>(config.getStringList("completed_quests")
                .stream()
                .map(questStorage::getQuestByUniqueKeyOrThrow)
                .toList());

        QuestUserData userData = new BaseQuestUserData(uuid,
                completedQuests,
                new ArrayList<>(),
                questUserReceiptsRewardsData);

        if (config.contains("active_progresses")) {
            ConfigurationSection configurationSection = config.getConfigurationSection("active_progresses");

            for (String keyQuest : configurationSection.getKeys(false)) {

                ConfigurationSection progressSection = configurationSection.getConfigurationSection(keyQuest);
                if (progressSection == null) {
                    continue;
                }

                QuestProgress questProgress = ((BaseProgressParser) questProgressParser).parse(progressSection, userData);
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
        String userFileName = uuid.toString();
        if (usersDirectory.containsFileWithName(userFileName)) {
            BukkitConfig userFile = usersDirectory.getBukkitConfig(userFileName);
            usersDirectory.removeAndDeleteConfig(userFile);
            ConsoleLogger.info(plugin, "Данные игрока %s были успешно удалены.", userFileName);
            fileLocks.remove(uuid); // Удаляем lock после удаления данных
        }
        }
    }

    @Override
    public long getQuestCompletionTime(UUID uuid, String questKey) {
        Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
        synchronized (lock) {
            String userFileName = uuid.toString();
            if (!usersDirectory.containsFileWithName(userFileName)) {
                return 0;
            }

            BukkitConfig userFile = usersDirectory.getBukkitConfig(userFileName);
            FileConfiguration config = userFile.getFileConfiguration();
            
            return config.getLong("quest_completion_times." + questKey, 0);
        }
    }

    @Override
    public CompletableFuture<Void> deleteQuestData(UUID uuid, String questKey) {
        return CompletableFuture.runAsync(() -> {
            Object lock = fileLocks.computeIfAbsent(uuid, k -> new Object());
            synchronized (lock) {
                String userFileName = uuid.toString();
                if (!usersDirectory.containsFileWithName(userFileName)) {
                    return;
                }

                BukkitConfig userFile = usersDirectory.getBukkitConfig(userFileName);
                FileConfiguration config = userFile.getFileConfiguration();

                // Удаляем квест из списка завершенных
                List<String> completedQuests = new ArrayList<>(config.getStringList("completed_quests"));
                completedQuests.remove(questKey);
                config.set("completed_quests", completedQuests);

                // Удаляем прогресс квеста
                config.set("active_progresses." + questKey, null);

                // Удаляем время завершения
                config.set("quest_completion_times." + questKey, null);

                userFile.saveConfiguration();
                ConsoleLogger.debug(plugin.getName(), "Deleted quest data for user %s, quest %s", uuid, questKey);
            }
        });
    }
}