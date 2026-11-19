package ru.nilsson03.library.quest.util.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.AtomicMoveNotSupportedException;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.util.QuestLog;

public class ConfigurationUtil {

    public static FileConfiguration load(Plugin plugin, File directory, String fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        File configFile = new File(directory, fileName);

        if (!configFile.getParentFile().exists() && !configFile.getParentFile().mkdirs()) {
            throw new IllegalStateException("Failed to create directory: " + configFile.getParent());
        }

        if (!configFile.exists()) {
            try (InputStream input = plugin.getResource(fileName)) {
                if (input != null) {
                    Files.copy(input, configFile.toPath());
                    QuestLog.info(plugin, "Successfully copied default config: %s", configFile.getPath());
                } else {
                    if (configFile.createNewFile()) {
                        QuestLog.info(plugin, "Created EMPTY config file: %s", configFile.getPath());
                    }
                }
            } catch (IOException e) {
                throw new IllegalStateException("Failed to initialize config: " + fileName, e);
            }
        } else if (configFile.length() == 0 && plugin.getResource(fileName) != null) {
            try (InputStream input = plugin.getResource(fileName)) {
                Files.copy(input, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                QuestLog.info(plugin, "Overwrote empty config with default: %s", configFile.getPath());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to overwrite empty config: " + fileName, e);
            }
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static Set<FileConfiguration> load(Plugin plugin, File dataFolder, String... fileName) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        Objects.requireNonNull(dataFolder, "dataFolder cannot be null");

        Set<FileConfiguration> fileConfigurations = new HashSet<>();

        for (String string : fileName) {
            fileConfigurations.add(load(plugin, dataFolder, string));
        }

        return fileConfigurations;
    }

    public static FileConfiguration load(Plugin plugin, String fileName) {
        return load(plugin, plugin.getDataFolder(), fileName);
    }

    public static FileConfiguration reload(File configFile) {
        Objects.requireNonNull(configFile, "configFile cannot be null");

        if (!configFile.exists()) {
            throw new IllegalStateException("Config file does not exist: " + configFile.getPath());
        }

        return YamlConfiguration.loadConfiguration(configFile);
    }

    public static void save(FileConfiguration config, File configFile) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(configFile, "configFile cannot be null");

        File parent = configFile.getAbsoluteFile().getParentFile();
        try {
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw new IOException("Failed to create directory: " + parent);
            }
            File temporary = File.createTempFile(configFile.getName(), ".tmp", parent);
            try {
                config.save(temporary);
                try {
                    Files.move(temporary.toPath(), configFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
                } catch (AtomicMoveNotSupportedException ignored) {
                    Files.move(temporary.toPath(), configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            } finally {
                Files.deleteIfExists(temporary.toPath());
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to save config: " + configFile.getPath(), e);
        }
    }

    public static void save(FileConfiguration config, File directory, String fileName) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(directory, "directory cannot be null");
        validateFileName(fileName);

        File configFile = new File(directory, fileName);
        save(config, configFile);
    }

    public static void remove(FileConfiguration config, String path) {
        Objects.requireNonNull(config, "config cannot be null");
        Objects.requireNonNull(path, "path cannot be null");
        config.set(path, null);
    }

    private static void validateFileName(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileName cannot be null or empty");
        }
    }
}
