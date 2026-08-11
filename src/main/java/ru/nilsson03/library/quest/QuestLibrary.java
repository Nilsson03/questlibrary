package ru.nilsson03.library.quest;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.file.ConfigurationUtil;
import ru.nilsson03.library.bukkit.util.file.DirectoryHelper;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.core.listener.QuestProgressListener;
import ru.nilsson03.library.quest.exception.QuestStorageDuplicateException;
import ru.nilsson03.library.quest.exception.QuestStorageNotLoadedException;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.QuestStorageManager;

public class QuestLibrary extends NPlugin {

    private static QuestLibrary instance;

    @Getter
    private FileConfiguration configuration;
    private DirectoryHelper directoryHelper;
    private QuestStorageManager questStorageManager;

    @Override
    public void enable() {
        instance = this;

        try {
            questStorageManager = new QuestStorageManager();
        } catch (IllegalStateException exception) {
            ConsoleLogger.error(this, "Quest storage manager already initialized. Disabling the Library, exception %s",
                    exception.getMessage());
            Bukkit.getPluginManager()
                    .disablePlugin(this);
            return;
        }

        ConfigurationUtil.load(this, getDataFolder(), "config.yml");
        directoryHelper = DirectoryHelper.of(this);
        DirectoryHelper.Directory rootDirectory = directoryHelper.getOrLoad("");
        configuration = rootDirectory.get("config.yml");
        getServer().getPluginManager().registerEvents(new QuestProgressListener(), this);
    }

    @Override
    public void disable() {
        if (directoryHelper != null) {
            directoryHelper.unregister();
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
