package ru.nilsson03.library.quest.user.data.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.BukkitDirectory;
import ru.nilsson03.library.bukkit.file.FileRepository;
import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.parser.BaseProgressParser;
import ru.nilsson03.library.quest.objective.progress.saver.BaseProgressSaver;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import java.util.*;

public class FileUserPersistent implements UserDataPersistent {

    private final FileRepository fileRepository;
    private final NPlugin plugin;
    private final QuestStorage questStorage;
    private final Parser<QuestProgress> questProgressParser;
    private final ProgressSaver progressSaver;
    private final BukkitDirectory usersDirectory;

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
        String userFileName = userData.uuid().toString();
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

        userData.getActiveQuests().forEach(progress -> {
            Quest quest = progress.quest();
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

    @Override
    public QuestUserData loadUserData(UUID uuid) {

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

        List<Quest> completedQuests = new ArrayList<>(config.getStringList("completed_quests")
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

    @Override
    public void deleteUserData(UUID uuid) {
        String userFileName = uuid.toString();
        if (usersDirectory.containsFileWithName(userFileName)) {
            BukkitConfig userFile = usersDirectory.getBukkitConfig(userFileName);
            usersDirectory.removeAndDeleteConfig(userFile);
            ConsoleLogger.info(plugin, "Данные игрока %s были успешно удалены.", userFileName);
        }
    }
}