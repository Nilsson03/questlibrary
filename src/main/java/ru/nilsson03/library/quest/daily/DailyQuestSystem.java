package ru.nilsson03.library.quest.daily;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.core.QuestSystemContext;
import ru.nilsson03.library.quest.daily.config.DailyQuestConfig;
import ru.nilsson03.library.quest.daily.persistence.DailyQuestPersistence;
import ru.nilsson03.library.quest.daily.persistence.InMemoryDailyQuestPersistence;
import ru.nilsson03.library.quest.daily.persistence.SqlDailyQuestPersistence;
import ru.nilsson03.library.quest.daily.placeholder.DailyQuestPlaceholders;
import ru.nilsson03.library.quest.daily.scheduler.DailyQuestScheduler;
import ru.nilsson03.library.quest.daily.selector.WeightedRaritySelector;
import ru.nilsson03.library.quest.meta.parser.RarityMetaParser;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

public final class DailyQuestSystem {

    private static final String DEFAULT_CONFIG_CONTENT = """
            limit: 5
            update-period: 1d
            assignment-mode: SHARED  # SHARED | PERSONAL

            # Dynamic rarities: add/remove keys freely; weights drive selection
            # Legacy form also works: EASY: 50
            rarities:
              EASY:
                weight: 50
                display-name: "EASY"
              HARD:
                weight: 30
                display-name: "HARD"
              EPIC:
                weight: 15
                display-name: "EPIC"
              MASTER:
                weight: 5
                display-name: "MASTER"
            """;

    private final NPlugin plugin;
    private final QuestSystemContext context;
    private final DailyQuestConfig config;
    private final String questsFolder;
    private final String configFileName;
    private final QuestLoader questLoader;
    private final DailyQuestPersistence persistence;
    private final WeightedRaritySelector selector;
    private final boolean schedulerEnabled;

    private final List<BaseQuest> pool = new CopyOnWriteArrayList<>();
    private final List<BaseQuest> sharedActive = new CopyOnWriteArrayList<>();

    private QuestStorage poolStorage;
    private DailyQuestScheduler scheduler;
    private boolean started;

    @lombok.Builder(builderClassName = "Builder", builderMethodName = "builderInternal")
    private DailyQuestSystem(
            QuestSystemContext context,
            DailyQuestConfig config,
            String questsFolder,
            String configFileName,
            QuestLoader questLoader,
            DailyQuestPersistence persistence,
            boolean schedulerEnabled,
            Random random) {
        this.plugin = context.getQuestService().getPlugin();
        this.context = context;
        this.questsFolder = questsFolder;
        this.configFileName = configFileName;
        this.questLoader = questLoader;
        this.persistence = persistence;
        this.schedulerEnabled = schedulerEnabled;
        this.config = config;
        this.selector = new WeightedRaritySelector(config, random != null ? random : new Random());
    }

    public static Builder builder(QuestSystemContext context) {
        return builderInternal()
                .context(Objects.requireNonNull(context, "context"))
                .questsFolder("daily_quests")
                .configFileName("daily_quests.yml")
                .schedulerEnabled(true);
    }

    public synchronized void start() {
        if (started) {
            throw new IllegalStateException("DailyQuestSystem already started");
        }
        bootstrapFiles();
        registerRarityParserIfNeeded();
        loadPool();
        context.getQuestStorage().registerQuests(pool);

        if (config.assignmentMode() == DailyAssignmentMode.SHARED) {
            ensureSharedSelection();
        } else {
            ensurePeriodInitialized();
        }

        if (schedulerEnabled) {
            scheduler = new DailyQuestScheduler(plugin, this::forceRotate, this::millisUntilNextReset);
            scheduler.scheduleNext();
        }

        started = true;
        ConsoleLogger.info(plugin, "DailyQuestSystem started (%s, pool=%d, limit=%d)",
                config.assignmentMode(), pool.size(), config.limit());
    }

    public synchronized void shutdown() {
        if (scheduler != null) {
            scheduler.cancel();
            scheduler = null;
        }
        started = false;
    }

    public List<BaseQuest> getPool() {
        return List.copyOf(pool);
    }

    public DailyQuestConfig getConfig() {
        return config;
    }

    public QuestUsersStorage getQuestUsersStorage() {
        return context.getQuestUsersStorage();
    }

    public DailyQuestPlaceholders placeholders() {
        return new DailyQuestPlaceholders(this);
    }

    public DailyAssignmentMode getAssignmentMode() {
        return config.assignmentMode();
    }

    public long getLastUpdateTime() {
        return persistence.getLastUpdateTime().orElse(0L);
    }

    public long millisUntilNextReset() {
        long last = getLastUpdateTime();
        if (last <= 0L) {
            return 0L;
        }
        long next = last + config.updatePeriodMillis();
        return Math.max(0L, next - System.currentTimeMillis());
    }

    public long timeUntilNextReset() {
        return millisUntilNextReset();
    }

    public synchronized List<BaseQuest> getActiveDailyQuests(UUID ownerId) {
        Objects.requireNonNull(ownerId, "ownerId");
        ensureStarted();
        if (isPeriodExpired()) {
            forceRotate();
        }

        if (config.assignmentMode() == DailyAssignmentMode.SHARED) {
            return List.copyOf(sharedActive);
        }

        List<BaseQuest> existing = persistence.loadPlayerQuests(ownerId);
        if (!existing.isEmpty()) {
            return existing;
        }
        return assignPersonalQuests(ownerId);
    }

    public synchronized void clearDailyProgressForOwners(Collection<? extends BaseQuest> quests) {
        clearProgressForQuests(quests);
    }

    public synchronized void forceRotate() {
        ensureStartedOrAllowBootstrap();
        long now = System.currentTimeMillis();

        if (config.assignmentMode() == DailyAssignmentMode.SHARED) {
            List<BaseQuest> previous = persistence.loadSharedQuests();
            if (previous.isEmpty()) {
                previous = List.copyOf(sharedActive);
            }
            clearProgressForQuests(previous);
            List<BaseQuest> selected = selector.select(pool);
            persistence.saveSharedQuests(selected, now);
            sharedActive.clear();
            sharedActive.addAll(selected);
            ConsoleLogger.info(plugin, "Shared daily quests rotated, selected %d", selected.size());
            return;
        }

        Map<UUID, List<BaseQuest>> previousByPlayer = persistence.loadAllPlayerQuests();
        previousByPlayer.forEach(this::clearProgressForPlayer);
        persistence.clearAllPlayerQuests();
        persistence.saveLastUpdateTime(now);
        ConsoleLogger.info(plugin, "Personal daily quests invalidated for %d players", previousByPlayer.size());
    }

    private void ensureSharedSelection() {
        long now = System.currentTimeMillis();
        Long last = persistence.getLastUpdateTime().orElse(null);
        List<BaseQuest> saved = persistence.loadSharedQuests();

        if (last == null || now - last >= config.updatePeriodMillis() || saved.isEmpty()) {
            if (!saved.isEmpty()) {
                clearProgressForQuests(saved);
            }
            List<BaseQuest> selected = selector.select(pool);
            persistence.saveSharedQuests(selected, now);
            sharedActive.clear();
            sharedActive.addAll(selected);
        } else {
            sharedActive.clear();
            sharedActive.addAll(saved);
        }
    }

    private void ensurePeriodInitialized() {
        long now = System.currentTimeMillis();
        Long last = persistence.getLastUpdateTime().orElse(null);
        if (last == null) {
            persistence.saveLastUpdateTime(now);
            return;
        }
        if (now - last >= config.updatePeriodMillis()) {
            forceRotate();
        }
    }

    private boolean isPeriodExpired() {
        Long last = persistence.getLastUpdateTime().orElse(null);
        if (last == null) {
            return true;
        }
        return System.currentTimeMillis() - last >= config.updatePeriodMillis();
    }

    private List<BaseQuest> assignPersonalQuests(UUID playerId) {
        long now = persistence.getLastUpdateTime().orElse(System.currentTimeMillis());
        List<BaseQuest> selected = selector.select(pool);
        persistence.savePlayerQuests(playerId, selected, now);
        return selected;
    }

    private void clearProgressForQuests(Collection<? extends BaseQuest> quests) {
        if (quests == null || quests.isEmpty()) {
            return;
        }
        QuestUsersStorage usersStorage = context.getQuestUsersStorage();
        UserDataPersistent userPersistent = context.getDataPersistent();
        // Clears every loaded owner, including GROUP subjects (guilds).
        usersStorage.clearCachedQuestData(quests);

        List<String> keys = quests.stream()
                .map(quest -> quest.questUniqueKey().getKey())
                .collect(Collectors.toList());
        try {
            userPersistent.deleteQuestDataByKeys(keys).join();
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Failed to clear shared daily progress: %s", e.getMessage());
        }
    }

    private void clearProgressForPlayer(UUID playerId, Collection<? extends BaseQuest> quests) {
        if (quests == null || quests.isEmpty()) {
            return;
        }
        QuestUsersStorage usersStorage = context.getQuestUsersStorage();
        UserDataPersistent userPersistent = context.getDataPersistent();
        usersStorage.clearCachedQuestData(playerId, quests);
        for (BaseQuest quest : quests) {
            try {
                userPersistent.deleteQuestData(playerId, quest.questUniqueKey().getKey()).join();
            } catch (Exception e) {
                ConsoleLogger.error(plugin, "Failed to clear personal daily progress: %s", e.getMessage());
            }
        }
    }

    private void bootstrapFiles() {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IllegalStateException("Cannot create data folder for " + plugin.getName());
        }

        try {
            Files.createDirectories(new File(dataFolder, questsFolder).toPath());
        } catch (IOException e) {
            throw new IllegalStateException("Cannot create daily quests folder", e);
        }

        File configFile = new File(dataFolder, configFileName);
        if (!configFile.exists()) {
            try {
                Files.writeString(configFile.toPath(), DEFAULT_CONFIG_CONTENT, StandardCharsets.UTF_8);
                ConsoleLogger.info(plugin, "Created %s in %s data folder", configFileName, plugin.getName());
            } catch (IOException e) {
                throw new IllegalStateException("Failed to create " + configFileName + " for " + plugin.getName(), e);
            }
        }
    }

    private void registerRarityParserIfNeeded() {
        var registry = context.getQuestService().getMetaParserRegistry();
        if (!registry.isParserRegistered("rarity")) {
            registry.registerParser(plugin.getName(), "rarity", new RarityMetaParser());
        }
    }

    private void loadPool() {
        pool.clear();
        poolStorage = new QuestStorage(plugin, questLoader);
        poolStorage.loadQuests(questsFolder);
        pool.addAll(poolStorage.getQuests());
        ConsoleLogger.info(plugin, "Loaded %d daily quest definitions from %s", pool.size(), questsFolder);
    }

    private void ensureStarted() {
        if (!started) {
            throw new IllegalStateException("DailyQuestSystem is not started");
        }
    }

    private void ensureStartedOrAllowBootstrap() {
        if (pool.isEmpty() && !started) {
            throw new IllegalStateException("DailyQuestSystem is not started");
        }
    }

    public boolean isStarted() {
        return started;
    }

    public List<BaseQuest> getSharedActiveSnapshot() {
        return List.copyOf(sharedActive);
    }

    public static class Builder {
        private Integer limitOverride;
        private String updatePeriodOverride;
        private DailyAssignmentMode modeOverride;
        private boolean useSqlPersistence = true;
        private FileConfiguration providedConfig;

        public Builder questsFolder(String folder) {
            this.questsFolder = Objects.requireNonNull(folder, "folder");
            return this;
        }

        public Builder configFileName(String fileName) {
            this.configFileName = Objects.requireNonNull(fileName, "fileName");
            return this;
        }

        public Builder withConfig(FileConfiguration configuration) {
            this.providedConfig = Objects.requireNonNull(configuration, "configuration");
            return this;
        }

        public Builder limit(int limit) {
            this.limitOverride = limit;
            return this;
        }

        public Builder updatePeriod(String period) {
            this.updatePeriodOverride = period;
            return this;
        }

        public Builder assignmentMode(DailyAssignmentMode mode) {
            this.modeOverride = mode;
            return this;
        }

        public Builder withQuestLoader(QuestLoader loader) {
            return questLoader(loader);
        }

        public Builder withPersistence(DailyQuestPersistence persistence) {
            this.useSqlPersistence = false;
            return persistence(persistence);
        }

        public Builder withRandom(Random random) {
            return random(random);
        }

        public DailyQuestSystem build() {
            NPlugin plugin = context.getQuestService().getPlugin();
            ensureDefaultConfigFile(plugin);

            DailyQuestConfig loaded;
            if (providedConfig != null) {
                loaded = DailyQuestConfig.fromYaml(providedConfig);
            } else {
                File configFile = new File(plugin.getDataFolder(), configFileName);
                FileConfiguration yaml = YamlConfiguration.loadConfiguration(configFile);
                loaded = DailyQuestConfig.fromYaml(yaml);
            }
            DailyQuestConfig resolvedConfig = loaded.withOverrides(limitOverride, updatePeriodOverride, modeOverride);

            DailyQuestPersistence resolvedPersistence = persistence;
            if (resolvedPersistence == null) {
                resolvedPersistence = useSqlPersistence
                        ? new SqlDailyQuestPersistence(plugin, context.getQuestStorage())
                        : new InMemoryDailyQuestPersistence();
            }

            if (questLoader == null) {
                throw new IllegalStateException(
                        "QuestLoader is required. Call withQuestLoader(...) using the same loader as the main quest system.");
            }

            return new DailyQuestSystem(
                    context,
                    resolvedConfig,
                    questsFolder,
                    configFileName,
                    questLoader,
                    resolvedPersistence,
                    schedulerEnabled,
                    random);
        }

        private void ensureDefaultConfigFile(NPlugin plugin) {
            File dataFolder = plugin.getDataFolder();
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File configFile = new File(dataFolder, configFileName);
            if (!configFile.exists()) {
                try {
                    Files.writeString(configFile.toPath(), DEFAULT_CONFIG_CONTENT, StandardCharsets.UTF_8);
                } catch (IOException e) {
                    throw new IllegalStateException("Failed to create " + configFileName, e);
                }
            }
        }
    }
}
