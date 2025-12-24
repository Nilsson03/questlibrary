package ru.nilsson03.library.quest.user.data.impl;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SqlUserPersistent implements UserDataPersistent {

    private final NPlugin plugin;
    private final DataSource dataSource;
    private final QuestStorage questStorage;
    private final ExecutorService executorService;

    public SqlUserPersistent(NPlugin plugin,
                             DataSource dataSource,
                             QuestStorage questStorage) {
        this(plugin, dataSource, questStorage, 3);
    }

    public SqlUserPersistent(NPlugin plugin,
                             DataSource dataSource,
                             QuestStorage questStorage,
                             int threadPoolSize) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        this.questStorage = questStorage;
        this.executorService = Executors.newFixedThreadPool(
                threadPoolSize,
                r -> {
                    Thread thread = new Thread(r);
                    thread.setName("QuestSQL-Worker-" + thread.getId());
                    thread.setDaemon(true);
                    return thread;
                }
        );
        initializeTables();
        ConsoleLogger.info(plugin, "SQL user persistent initialized with %d async threads", threadPoolSize);
    }

    /**
     * Инициализация таблиц в базе данных
     */
    private void initializeTables() {
        try (Connection connection = dataSource.getConnection()) {
            createUsersTable(connection);
            createCompletedQuestsTable(connection);
            createActiveProgressTable(connection);
            createProgressGoalsTable(connection);
            createReceiptsRewardsTable(connection);
            ConsoleLogger.info(plugin, "SQL tables for quest user data initialized successfully.");
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to initialize SQL tables: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void createUsersTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_users (
                    uuid VARCHAR(36) PRIMARY KEY,
                    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createCompletedQuestsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_completed (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    quest_key VARCHAR(255) NOT NULL,
                    completed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_user_quest (user_uuid, quest_key),
                    FOREIGN KEY (user_uuid) REFERENCES quest_users(uuid) ON DELETE CASCADE
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createActiveProgressTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_active_progress (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    quest_key VARCHAR(255) NOT NULL,
                    objective_type VARCHAR(100) NOT NULL,
                    started_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE KEY unique_user_quest_progress (user_uuid, quest_key),
                    FOREIGN KEY (user_uuid) REFERENCES quest_users(uuid) ON DELETE CASCADE
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createProgressGoalsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_progress_goals (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    progress_id INT NOT NULL,
                    goal_key VARCHAR(255) NOT NULL,
                    current_value BIGINT NOT NULL DEFAULT 0,
                    target_value BIGINT NOT NULL,
                    FOREIGN KEY (progress_id) REFERENCES quest_active_progress(id) ON DELETE CASCADE
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createReceiptsRewardsTable(Connection connection) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS quest_receipts_rewards (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    user_uuid VARCHAR(36) NOT NULL,
                    reward_uuid VARCHAR(36) NOT NULL,
                    taken_count INT NOT NULL DEFAULT 0,
                    UNIQUE KEY unique_user_reward (user_uuid, reward_uuid),
                    FOREIGN KEY (user_uuid) REFERENCES quest_users(uuid) ON DELETE CASCADE
                )
                """;
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public CompletableFuture<Void> saveUserDataAsync(QuestUserData userData) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);
                try {
                    ensureUserExists(connection, userData.uuid());
                    saveCompletedQuests(connection, userData);
                    saveActiveProgress(connection, userData);
                    saveReceiptsRewards(connection, userData);
                    
                    connection.commit();
                    ConsoleLogger.debug(plugin.getName(), "User data saved successfully for UUID: %s", userData.uuid());
                } catch (SQLException e) {
                    connection.rollback();
                    ConsoleLogger.error(plugin, "Failed to save user data, transaction rolled back: %s", e.getMessage());
                    throw new RuntimeException(e);
                }
            } catch (SQLException e) {
                ConsoleLogger.error(plugin, "Failed to save user data for UUID %s: %s", userData.uuid(), e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void saveUserData(QuestUserData userData) {
        saveUserDataAsync(userData).join();
    }

    private void ensureUserExists(Connection connection, UUID uuid) throws SQLException {
        String sql = "INSERT IGNORE INTO quest_users (uuid) VALUES (?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    private void saveCompletedQuests(Connection connection, QuestUserData userData) throws SQLException {
        String deleteSql = "DELETE FROM quest_completed WHERE user_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, userData.uuid().toString());
            statement.executeUpdate();
        }

        String insertSql = "INSERT INTO quest_completed (user_uuid, quest_key) VALUES (?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Quest quest : userData.completeQuests()) {
                statement.setString(1, userData.uuid().toString());
                statement.setString(2, quest.questUniqueKey().getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveActiveProgress(Connection connection, QuestUserData userData) throws SQLException {
        String deleteProgressSql = "DELETE FROM quest_active_progress WHERE user_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteProgressSql)) {
            statement.setString(1, userData.uuid().toString());
            statement.executeUpdate();
        }

        String insertProgressSql = """
                INSERT INTO quest_active_progress (user_uuid, quest_key, objective_type) 
                VALUES (?, ?, ?)
                """;
        String insertGoalSql = """
                INSERT INTO quest_progress_goals (progress_id, goal_key, current_value, target_value) 
                VALUES (?, ?, ?, ?)
                """;

        for (QuestProgress progress : userData.getActiveQuests()) {
            try (PreparedStatement progressStmt = connection.prepareStatement(insertProgressSql, Statement.RETURN_GENERATED_KEYS)) {
                progressStmt.setString(1, userData.uuid().toString());
                progressStmt.setString(2, progress.quest().questUniqueKey().getKey());
                progressStmt.setString(3, progress.objective().type().key());
                progressStmt.executeUpdate();

                ResultSet rs = progressStmt.getGeneratedKeys();
                if (rs.next()) {
                    int progressId = rs.getInt(1);

                    try (PreparedStatement goalStmt = connection.prepareStatement(insertGoalSql)) {
                        for (Goal goal : progress.objective().goals()) {
                            goalStmt.setInt(1, progressId);
                            goalStmt.setString(2, goal.toString());
                            goalStmt.setLong(3, progress.getValue(goal));
                            goalStmt.setLong(4, goal.targetValue());
                            goalStmt.addBatch();
                        }
                        goalStmt.executeBatch();
                    }
                }
            }
        }
    }

    private void saveReceiptsRewards(Connection connection, QuestUserData userData) throws SQLException {
        if (!userData.hasActiveReceiptsRewardsData()) {
            return;
        }

        String deleteSql = "DELETE FROM quest_receipts_rewards WHERE user_uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, userData.uuid().toString());
            statement.executeUpdate();
        }

        QuestUserReceiptsRewardsData receiptsData = userData.getReceiptsRewardsData();
        String insertSql = "INSERT INTO quest_receipts_rewards (user_uuid, reward_uuid, taken_count) VALUES (?, ?, ?)";
        
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (Map.Entry<UUID, Integer> entry : receiptsData.getTakenRewardsAndCount().entrySet()) {
                statement.setString(1, userData.uuid().toString());
                statement.setString(2, entry.getKey().toString());
                statement.setInt(3, entry.getValue());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    public CompletableFuture<QuestUserData> loadUserDataAsync(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                if (!userExists(connection, uuid)) {
                    return new BaseQuestUserData(uuid,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new QuestUserReceiptsRewardsData());
                }

                List<Quest> completedQuests = loadCompletedQuests(connection, uuid);
                Map<UUID, Integer> receiptsRewards = loadReceiptsRewards(connection, uuid);
                QuestUserReceiptsRewardsData receiptsData = new QuestUserReceiptsRewardsData(receiptsRewards);

                QuestUserData userData = new BaseQuestUserData(uuid,
                        completedQuests,
                        new ArrayList<>(),
                        receiptsData);

                List<QuestProgress> activeProgress = loadActiveProgress(connection, uuid, userData);
                userData.addActiveQuests(activeProgress);

                ConsoleLogger.debug(plugin.getName(), "User data loaded successfully for UUID: %s", uuid);
                return userData;
            } catch (SQLException e) {
                ConsoleLogger.error(plugin, "Failed to load user data for UUID %s: %s", uuid, e.getMessage());
                e.printStackTrace();
                return new BaseQuestUserData(uuid,
                        new ArrayList<>(),
                        new ArrayList<>(),
                        new QuestUserReceiptsRewardsData());
            }
        }, executorService);
    }

    @Override
    public QuestUserData loadUserData(UUID uuid) {
        return loadUserDataAsync(uuid).join();
    }

    private boolean userExists(Connection connection, UUID uuid) throws SQLException {
        String sql = "SELECT 1 FROM quest_users WHERE uuid = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }

    private List<Quest> loadCompletedQuests(Connection connection, UUID uuid) throws SQLException {
        List<Quest> completedQuests = new ArrayList<>();
        String sql = "SELECT quest_key FROM quest_completed WHERE user_uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String questKey = rs.getString("quest_key");
                try {
                    Quest quest = questStorage.getQuestByUniqueKeyOrThrow(questKey);
                    completedQuests.add(quest);
                } catch (Exception e) {
                    plugin.getLogger().warning("Quest with key '" + questKey + "' not found in storage, skipping.");
                }
            }
        }
        return completedQuests;
    }

    private Map<UUID, Integer> loadReceiptsRewards(Connection connection, UUID uuid) throws SQLException {
        Map<UUID, Integer> receiptsRewards = new HashMap<>();
        String sql = "SELECT reward_uuid, taken_count FROM quest_receipts_rewards WHERE user_uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                UUID rewardUuid = UUID.fromString(rs.getString("reward_uuid"));
                int takenCount = rs.getInt("taken_count");
                receiptsRewards.put(rewardUuid, takenCount);
            }
        }
        return receiptsRewards;
    }

    private List<QuestProgress> loadActiveProgress(Connection connection, UUID uuid, QuestUserData userData) throws SQLException {
        List<QuestProgress> progressList = new ArrayList<>();
        String sql = """
                SELECT id, quest_key, objective_type 
                FROM quest_active_progress 
                WHERE user_uuid = ?
                """;
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                int progressId = rs.getInt("id");
                String questKey = rs.getString("quest_key");
                String objectiveType = rs.getString("objective_type");
                
                try {
                    Quest quest = questStorage.getQuestByUniqueKeyOrThrow(questKey);
                    Map<String, Long> goalProgress = loadGoalProgress(connection, progressId);
                    
                    QuestProgress progress = createQuestProgress(quest, objectiveType, goalProgress, userData);
                    if (progress != null) {
                        progressList.add(progress);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load progress for quest '" + questKey + "': " + e.getMessage());
                }
            }
        }
        return progressList;
    }

    private Map<String, Long> loadGoalProgress(Connection connection, int progressId) throws SQLException {
        Map<String, Long> goalProgress = new HashMap<>();
        String sql = "SELECT goal_key, current_value FROM quest_progress_goals WHERE progress_id = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, progressId);
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String goalKey = rs.getString("goal_key");
                long currentValue = rs.getLong("current_value");
                goalProgress.put(goalKey, currentValue);
            }
        }
        return goalProgress;
    }

    private QuestProgress createQuestProgress(Quest quest, String objectiveTypeKey, 
                                             Map<String, Long> goalProgress, QuestUserData userData) {
        if (!(quest instanceof BaseQuest)) {
            return null;
        }
        
        BaseQuest baseQuest = (BaseQuest) quest;
        return baseQuest.objectives().stream()
                .filter(objective -> objective.type().key().equals(objectiveTypeKey))
                .findFirst()
                .map(objective -> {
                    QuestProgress progress = new ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress(
                            userData, quest, objective);
                    for (Goal goal : objective.goals()) {
                        Long currentValue = goalProgress.get(goal.toString());
                        if (currentValue != null && currentValue > 0) {
                            progress.incrementProgress(goal, currentValue, false);
                        }
                    }
                    return progress;
                })
                .orElse(null);
    }

    public CompletableFuture<Void> deleteUserDataAsync(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "DELETE FROM quest_users WHERE uuid = ?";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());
                    int rowsAffected = statement.executeUpdate();
                    
                    if (rowsAffected > 0) {
                        ConsoleLogger.info(plugin, "User data deleted successfully for UUID: %s", uuid);
                    } else {
                        plugin.getLogger().warning("No user data found to delete for UUID: " + uuid);
                    }
                }
            } catch (SQLException e) {
                ConsoleLogger.error(plugin, "Failed to delete user data for UUID %s: %s", uuid, e.getMessage());
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    @Override
    public void deleteUserData(UUID uuid) {
        deleteUserDataAsync(uuid).join();
    }

    public void shutdown() {
        ConsoleLogger.info(plugin, "Shutting down SQL executor service...");
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
                plugin.getLogger().warning("SQL executor service did not terminate in time, forcing shutdown");
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
