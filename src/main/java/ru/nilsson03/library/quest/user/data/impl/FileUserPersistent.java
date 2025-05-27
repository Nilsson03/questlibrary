package ru.nilsson03.library.quest.user.data.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.BukkitDirectory;
import ru.nilsson03.library.bukkit.file.FileRepository;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.namespace.QuestNamespace;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.objective.progress.registry.QuestProgressRegistry;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;

import java.util.*;

public class FileUserPersistent implements UserDataPersistent {

    private final NPlugin plugin;
    private final FileRepository fileRepository;
    private final QuestStorage questStorage;
    private final BukkitDirectory usersDirectory;
    private final QuestProgressRegistry questProgressRegistry;

    public FileUserPersistent(NPlugin plugin,
                              FileRepository fileRepository,
                              QuestStorage questStorage,
                              QuestProgressRegistry questProgressRegistry) {
        this.plugin = plugin;
        this.fileRepository = fileRepository;
        this.questStorage = questStorage;
        this.questProgressRegistry = questProgressRegistry;
        Optional<BukkitDirectory> usersDirectoryOptional = fileRepository.getDirectoryOrLoad("users");
        if (usersDirectoryOptional.isEmpty())
            throw new NullPointerException("Users directory not found, class FileUserDataStorage");
        else
            this.usersDirectory = usersDirectoryOptional.get();
    }

    @Override
    public void saveUserData(QuestUserData userData) {
        String userFileName = userData.uuid().toString();
        BukkitConfig bukkitConfig;
        if (usersDirectory.containsFileWithName(userFileName)) {
            Optional<BukkitConfig> userFile = usersDirectory.getConfig(userFileName);
            bukkitConfig = userFile.get();
        } else {
            Optional<BukkitConfig> userFile = fileRepository.create(usersDirectory, userFileName);
            bukkitConfig = userFile.get();
        }

        FileConfiguration config = bukkitConfig.getFileConfiguration();

        config.set("uuid", userData.uuid().toString());

        config.set("completed_quests", userData.completeQuests().stream()
                .map(q -> q.questUniqueKey().getKey())
                .toList());

        userData.getActiveQuests().forEach(progress -> {
            Quest quest = progress.quest();
            QuestNamespace namespace = quest.questUniqueKey();
            ConfigurationSection configurationSection = config.createSection("active_progresses." + namespace.getKey());

            String saverKey = quest instanceof BaseQuestProgress ? "base" : "staged";
            ProgressSaver saver = questProgressRegistry.getSaver(saverKey);
            saver.save(progress, configurationSection);
        });

        if (userData.hasActiveReceiptsRewardsData()) {
            QuestUserReceiptsRewardsData receiptsRewardsData = userData.getReceiptsRewardsData();

            for (Map.Entry<UUID, Integer> entry : receiptsRewardsData.getTakenRewardsAndCount().entrySet()) {
                config.set("receipts_rewards." + entry.getKey().toString(), entry.getValue());
            }
        }

        bukkitConfig.saveConfiguration();
    }

    @Override
    public QuestUserData loadUserData(UUID uuid) {

        String userFileName = uuid.toString();
        BukkitConfig bukkitConfig;
        if (usersDirectory.containsFileWithName(userFileName)) {
            Optional<BukkitConfig> userFile = usersDirectory.getConfig(userFileName);
            bukkitConfig = userFile.get();
        } else {
            return new BaseQuestUserData(uuid,
                    plugin,
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new QuestUserReceiptsRewardsData());
        }

        FileConfiguration config = bukkitConfig.getFileConfiguration();

        List<QuestProgress> questProgressList  = new ArrayList<>();
        if (config.contains("active_progresses")) {
            ConfigurationSection configurationSection = config.getConfigurationSection("active_progresses");

            for (String keyQuest : configurationSection.getKeys(false)) {

                String parserKey = configurationSection.contains("current_stage") ? "staged" : "base";
                Parser<QuestProgress> parser = questProgressRegistry.getParser(parserKey);
                ConfigurationSection progressSection = configurationSection.getConfigurationSection(keyQuest);
                QuestProgress questProgress = parser.parse(progressSection);
                questProgressList.add(questProgress);
            }
        }

        Map<UUID, Integer> receiptsRewards = new HashMap<>();
        if (config.contains("receipts_rewards")) {
            for (String keyUUID : config.getConfigurationSection("receipts_rewards").getKeys(false)) {
                UUID rewardUUID = UUID.fromString(keyUUID);
                int takenCount = config.getInt("receipts_rewards." + keyUUID);
                receiptsRewards.put(rewardUUID, takenCount);
            }
        }

        QuestUserReceiptsRewardsData questUserReceiptsRewardsData = new QuestUserReceiptsRewardsData(receiptsRewards);

        List<Quest> completedQuests = new ArrayList<>(config.getStringList("completed_quests")
                .stream()
                .map(questStorage::getQuestByUniqueKeyOrThrow)
                .toList());

        return new BaseQuestUserData(uuid,
                plugin,
                completedQuests,
                questProgressList,
                questUserReceiptsRewardsData);
    }

    @Override
    public void deleteUserData(UUID uuid) {
        String userFileName = uuid.toString();
        if (usersDirectory.containsFileWithName(userFileName)) {
            Optional<BukkitConfig> userFile = usersDirectory.getConfig(userFileName);
            BukkitConfig bukkitConfig = userFile.get();
            usersDirectory.removeAndDeleteConfig(bukkitConfig);
        }
    }
}