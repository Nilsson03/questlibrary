package ru.nilsson03.library.quest.util.file;

import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.util.QuestLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class DirectoryHelper {
    private static final Map<Plugin, DirectoryHelper> instances = new ConcurrentHashMap<>();
    private final Plugin plugin;
    private final Map<String, Directory> directories;

    {
        directories = new ConcurrentHashMap<>();
    }

    public static DirectoryHelper of(Plugin plugin) {
        return instances.computeIfAbsent(plugin, DirectoryHelper::new);
    }

    private DirectoryHelper(Plugin plugin) {
        Objects.requireNonNull(plugin, "plugin cannot be null");
        this.plugin = plugin;
    }

    public void unregister() {
        directories.values().forEach(Directory::closeAll);
        directories.clear();
        instances.remove(plugin);
    }

    public Directory getOrLoad(String directoryName) {
        if (directories.containsKey(directoryName)) {
            return directories.get(directoryName);
        }
        return load(directoryName);
    }

    public Directory load(String directoryName) {
        Objects.requireNonNull(directoryName, "directoryName cannot be null");

        if (directories.containsKey(directoryName)) {
            return directories.get(directoryName);
        }

        Path directoryPath = plugin.getDataFolder().toPath().resolve(directoryName);
        try {
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }
        } catch (IOException e) {
            QuestLog.error(plugin, "Couldn't create directory %s: %s", directoryPath, e.getMessage());
            return null;
        }

        Map<String, FileConfiguration> configs = loadConfigurations(directoryPath);
        Directory directory = new Directory(plugin, directoryPath, configs);
        directories.put(directoryName, directory);
        return directory;
    }

    private Map<String, FileConfiguration> loadConfigurations(Path directory) {
        Map<String, FileConfiguration> result = new HashMap<>();

        try (Stream<Path> stream = Files.list(directory)) {
            List<Path> yamlFiles = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> {
                        String name = path.toString().toLowerCase();
                        return name.endsWith(".yml") || name.endsWith(".yaml");
                    })
                    .toList();

            QuestLog.debug(plugin, "Loading configurations from directory: %s (found %d files)",
                    directory, yamlFiles.size());

            for (Path file : yamlFiles) {
                String fileName = file.getFileName().toString();

                if (fileName.trim().isEmpty()) {
                    QuestLog.warn(plugin, "Skipping file with empty name in directory: %s", directory);
                    continue;
                }

                if (result.containsKey(fileName)) {
                    QuestLog.warn(plugin, "Duplicate config %s found in directory: %s", fileName, directory);
                    continue;
                }

                try {
                    FileConfiguration config = ConfigurationUtil.reload(file.toFile());
                    result.put(fileName, config);
                    QuestLog.debug(plugin, "Loaded config: %s", fileName);
                } catch (Exception e) {
                    QuestLog.error(plugin, "Failed to load config %s: %s", fileName, e.getMessage());
                }
            }

            QuestLog.debug(plugin, "Loaded %d configurations from directory: %s", result.size(), directory);
        } catch (IOException e) {
            QuestLog.error(plugin, "Failed to list files in directory %s: %s", directory, e.getMessage());
        }

        return result;
    }

    public void reloadDirectory(Directory directory) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Map<String, FileConfiguration> configs = loadConfigurations(directory.getPath());
        directory.replaceAll(configs);
        QuestLog.debug(plugin, "Reloaded %d configs from directory: %s", configs.size(), directory.getPath());
    }

    public FileConfiguration create(Directory directory, String fileName) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Objects.requireNonNull(fileName, "fileName cannot be null");

        if (!directories.containsValue(directory)) {
            QuestLog.warn(plugin, "Directory %s not found", directory.getPath());
            return null;
        }

        if (directory.contains(fileName)) {
            QuestLog.warn(plugin, "Config %s already exists", fileName);
            return null;
        }

        try {
            Path filePath = directory.getPath().resolve(fileName);
            if (Files.exists(filePath)) {
                QuestLog.warn(plugin, "File %s exists on disk", fileName);
                return null;
            }

            FileConfiguration config = directory.addNew(fileName);
            if (config != null) {
                ConfigurationUtil.save(config, filePath.toFile());
                return config;
            }
            return null;
        } catch (Exception e) {
            QuestLog.error(plugin, "Failed to create config %s: %s", fileName, e.getMessage());
            return null;
        }
    }

    public FileConfiguration get(Directory directory, String fileName) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Objects.requireNonNull(fileName, "fileName cannot be null");

        if (!directories.containsValue(directory)) {
            QuestLog.debug(plugin, "Directory %s not in cache", directory.getPath());
            return null;
        }

        return directory.get(fileName);
    }

    public boolean delete(Directory directory, String fileName) {
        Objects.requireNonNull(directory, "directory cannot be null");
        Objects.requireNonNull(fileName, "fileName cannot be null");

        if (!directories.containsValue(directory)) {
            QuestLog.warn(plugin, "Directory %s not found", directory.getPath());
            return false;
        }

        try {
            Path filePath = directory.getPath().resolve(fileName);
            directory.remove(fileName);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
                QuestLog.debug(plugin, "Deleted config file: %s", fileName);
                return true;
            }
            return false;
        } catch (IOException e) {
            QuestLog.error(plugin, "Failed to delete config %s: %s", fileName, e.getMessage());
            return false;
        }
    }


    public static class Directory {
        private final Plugin plugin;
        @Getter
        private final Path path;
        private final Map<String, FileConfiguration> cached;

        protected Directory(Plugin plugin, Path path, Map<String, FileConfiguration> configs) {
            this.plugin = plugin;
            this.path = path;
            this.cached = new ConcurrentHashMap<>(configs);
        }

        public FileConfiguration get(String fileName) {
            return cached.get(fileName);
        }

        public FileConfiguration addNew(String fileName) {
            Objects.requireNonNull(fileName, "fileName cannot be null");

            if (contains(fileName)) {
                QuestLog.debug(plugin, "Config %s already exists in directory", fileName);
                return null;
            }

            FileConfiguration config = ConfigurationUtil.load(plugin, path.toFile(), fileName);
            cached.put(fileName, config);
            QuestLog.debug(plugin, "Added new config: %s", fileName);
            return config;
        }

        public void remove(String fileName) {
            cached.remove(fileName);
        }

        public boolean contains(String fileName) {
            return cached.containsKey(fileName);
        }

        public void save(String fileName) {
            if (!contains(fileName)) {
                QuestLog.warn(plugin, "Config %s not found in directory", fileName);
                return;
            }

            FileConfiguration config = cached.get(fileName);
            File file = path.resolve(fileName).toFile();

            if (config != null) {
                ConfigurationUtil.save(config, file);
            }
        }

        public void reload(String fileName) {
            if (!contains(fileName)) {
                QuestLog.warn(plugin, "Config %s not found in directory", fileName);
                return;
            }

            File file = path.resolve(fileName).toFile();
            if (file.exists()) {
                FileConfiguration reloaded = ConfigurationUtil.reload(file);
                cached.put(fileName, reloaded);
            }
        }

        public void saveAll() {
            cached.forEach((fileName, config) -> {
                File file = path.resolve(fileName).toFile();
                ConfigurationUtil.save(config, file);
            });
        }

        public void reloadAll() {
            if (cached.isEmpty()) {
                QuestLog.debug(plugin, "No cached configs to reload in directory: %s", path);
                return;
            }

            int total = cached.size();
            int reloaded = 0;
            int failed = 0;

            for (Map.Entry<String, FileConfiguration> entry : cached.entrySet()) {
                try {
                    String fileName = entry.getKey();
                    File file = path.resolve(fileName).toFile();

                    if (file.exists()) {
                        FileConfiguration config = ConfigurationUtil.reload(file);
                        cached.put(fileName, config);
                        reloaded++;
                    }
                } catch (Exception e) {
                    failed++;
                    QuestLog.warn(plugin, "Failed to reload config %s: %s",
                            entry.getKey(), e.getMessage());
                }
            }

            QuestLog.info(plugin, "Reloaded %d/%d configs in directory %s (failed: %d)",
                    reloaded, total, path, failed);
        }

        public void closeAll() {
            cached.clear();
        }

        public void replaceAll(Map<String, FileConfiguration> configs) {
            cached.clear();
            cached.putAll(configs);
        }

        public List<FileConfiguration> getAll() {
            return new ArrayList<>(cached.values());
        }

        public Set<String> getFileNames() {
            return new HashSet<>(cached.keySet());
        }

        public String getDirectoryName() {
            return path.getFileName().toString();
        }

        public int size() {
            return cached.size();
        }

        public boolean isEmpty() {
            return cached.isEmpty();
        }
    }
}
