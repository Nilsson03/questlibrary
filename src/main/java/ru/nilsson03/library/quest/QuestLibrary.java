package ru.nilsson03.library.quest;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.file.FileHelper;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.exception.QuestStorageDuplicateException;
import ru.nilsson03.library.quest.exception.QuestStorageNotLoadedException;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.QuestStorageManager;

import java.io.File;
import java.nio.file.Paths;

public class QuestLibrary extends NPlugin {

    private static QuestLibrary instance;

    private QuestStorageManager questStorageManager;

    @Override
    public void enable() {
        instance = this;

        createDataFolders();

        FileHelper.loadConfigurations(this, "examples/combat_quest",
                "examples/exploration_quest",
                "examples/multi_objective_quest",
                "examples/simple_quest");

        try {
            questStorageManager = new QuestStorageManager();
        } catch (IllegalStateException exception) {
            ConsoleLogger.error(this, "Quest storage manager already initialized. Disabling the Library, exception %s",
                    exception.getMessage());
            Bukkit.getPluginManager()
                    .disablePlugin(this);
        }
    }

    public QuestStorage getQuestStorage(Plugin plugin) {
        QuestStorageManager questStorageManager = QuestLibrary.getApi()
                .getQuestStorageManager();

        if (!questStorageManager.isQuestStorageLoadedAndNotEmpty(plugin)) {
            throw new QuestStorageNotLoadedException(
                    "Error on loading QuestService. Quest storage for plugin " + plugin.getName() + " not loaded.");
        }

        try {
            return questStorageManager.getQuestStorageByPlugin(plugin);

        } catch (QuestStorageDuplicateException exception) {
            throw new QuestStorageDuplicateException(
                    "Error on loading QuestService. Quest storage for plugin " + plugin.getName() + " already loaded.");
        }
    }

    private void createDataFolders() {
        File dataFolder = getDataFolder();
        createFolder(dataFolder, "Plugin data");

        createFolder(Paths.get(dataFolder.getPath(), "users")
                .toFile(), "Users data");
        createFolder(Paths.get(dataFolder.getPath(), "quests")
                .toFile(), "Quests data");
        createFolder(Paths.get(dataFolder.getPath(), "examples")
                .toFile(), "Example quests");
    }

    private void createFolder(File folder, String folderName) {
        if (!folder.exists()) {
            getLogger().info(folderName + " folder does not exist. Creating...");
            boolean created = folder.mkdirs();

            if (!created) {
                getLogger().severe("Error on creating " + folderName.toLowerCase() + " folder.");
            } else {
                getLogger().info(folderName + " folder created.");
            }
        }
    }

    public static QuestLibrary getApi() {
        return instance;
    }

    /**
     * Менеджер для управления хранилищами квестов со всеми вытекающими
     *
     * @return менеджер для управления хранилищами квестов
     */
    public QuestStorageManager getQuestStorageManager() {
        return questStorageManager;
    }
}
