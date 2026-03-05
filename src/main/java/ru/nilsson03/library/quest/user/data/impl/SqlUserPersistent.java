package ru.nilsson03.library.quest.user.data.impl;

import ru.nilsson03.hikaricp.SharedPoolRegistry;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;

import java.sql.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SqlUserPersistent implements UserDataPersistent {

    private final NPlugin plugin;
    private final QuestStorage questStorage;

    private final String QUEST_USERS_TABLE;
    private final String QUEST_COMPLETED_TABLE;
    private final String QUEST_ACITVE_PROGRESS_TABLE;
    private final String QUEST_PROGRESS_GOALS_TABLE;
    private final String QUEST_RECEIPTS_REWARDS_TABLE;

    public SqlUserPersistent(NPlugin plugin,
                             QuestStorage questStorage) {
        this.plugin = plugin;
        this.questStorage = questStorage;
        this.QUEST_USERS_TABLE = plugin.getName() + "_quest_users";
        this.QUEST_COMPLETED_TABLE = plugin.getName() + "_quest_completed";
        this.QUEST_ACITVE_PROGRESS_TABLE = plugin.getName() + "_quest_active_progress";
        this.QUEST_PROGRESS_GOALS_TABLE = plugin.getName() + "_quest_progress_goals";
        this.QUEST_RECEIPTS_REWARDS_TABLE = plugin.getName() + "_quest_receipts_rewards";
        initializeTables();
        ConsoleLogger.info(plugin, "SQL user persistent initialized");
    }

    private Connection getConnection() {
        try {
            return SharedPoolRegistry.getConnection(plugin);
        } catch (SQLException exception) {
            ConsoleLogger.error(plugin, "Failed to get connection: %s", exception.getMessage());
            throw new RuntimeException("Failed to get connection",  exception);
        } catch (NullPointerException exception) {
            ConsoleLogger.error(plugin, "Failed to get connection because pool is not found");
            throw new RuntimeException("Failed to get connection because pool is not found", exception);
        }
    }

    private void initializeTables() {
        try (Connection connection = getConnection()) {
            createUsersTable(connection);
            ConsoleLogger.info(plugin, "Created table: %s", QUEST_USERS_TABLE);
            
            createCompletedQuestsTable(connection);
            ConsoleLogger.info(plugin, "Created table: %s", QUEST_COMPLETED_TABLE);
            
            createActiveProgressTable(connection);
            ConsoleLogger.info(plugin, "Created table: %s", QUEST_ACITVE_PROGRESS_TABLE);
            
            createProgressGoalsTable(connection);
            ConsoleLogger.info(plugin, "Created table: %s", QUEST_PROGRESS_GOALS_TABLE);
            
            createReceiptsRewardsTable(connection);
            ConsoleLogger.info(plugin, "Created table: %s", QUEST_RECEIPTS_REWARDS_TABLE);
            
            ConsoleLogger.info(plugin, "SQL tables for quest user data initialized successfully.");
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to initialize SQL tables: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void createUsersTable(Connection connection) throws SQLException {
        String sql = String.format("""
            CREATE TABLE IF NOT EXISTS %s (
            uuid VARCHAR(36) PRIMARY KEY,
            created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
            updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
            )
        """, QUEST_USERS_TABLE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createCompletedQuestsTable(Connection connection) throws SQLException {
        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    user_uuid VARCHAR(36) NOT NULL,
                    quest_key VARCHAR(255) NOT NULL,
                    completed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (user_uuid, quest_key),
                    FOREIGN KEY (user_uuid) REFERENCES %s(uuid) ON DELETE CASCADE
                )
                """, QUEST_COMPLETED_TABLE, QUEST_USERS_TABLE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createActiveProgressTable(Connection connection) throws SQLException {
        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    user_uuid VARCHAR(36) NOT NULL,
                    quest_key VARCHAR(255) NOT NULL,
                    objective_key VARCHAR(255) NOT NULL,
                    objective_type VARCHAR(100) NOT NULL,
                    started_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (user_uuid, quest_key, objective_key),
                    FOREIGN KEY (user_uuid) REFERENCES %s(uuid) ON DELETE CASCADE
                )
                """, QUEST_ACITVE_PROGRESS_TABLE, QUEST_USERS_TABLE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createProgressGoalsTable(Connection connection) throws SQLException {
        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    progress_id INTEGER NOT NULL,
                    goal_key VARCHAR(255) NOT NULL,
                    current_value BIGINT NOT NULL DEFAULT 0,
                    target_value BIGINT NOT NULL,
                    UNIQUE (progress_id, goal_key),
                    FOREIGN KEY (progress_id) REFERENCES %s(id) ON DELETE CASCADE
                )
                """, QUEST_PROGRESS_GOALS_TABLE, QUEST_ACITVE_PROGRESS_TABLE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    private void createReceiptsRewardsTable(Connection connection) throws SQLException {
        String sql = String.format("""
                CREATE TABLE IF NOT EXISTS %s (
                    id INTEGER PRIMARY KEY AUTO_INCREMENT,
                    user_uuid VARCHAR(36) NOT NULL,
                    reward_uuid VARCHAR(36) NOT NULL,
                    taken_count INTEGER NOT NULL DEFAULT 0,
                    UNIQUE (user_uuid, reward_uuid),
                    FOREIGN KEY (user_uuid) REFERENCES %s(uuid) ON DELETE CASCADE
                )
                """, QUEST_RECEIPTS_REWARDS_TABLE, QUEST_USERS_TABLE);
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }

    public CompletableFuture<Void> saveUserDataAsync(QuestUserData userData) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
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
        });
    }

    @Override
    public void saveUserData(QuestUserData userData) {
        saveUserDataAsync(userData).join();
    }

    private void ensureUserExists(Connection connection, UUID uuid) throws SQLException {
        String sql = String.format("""
            INSERT INTO %s (uuid) 
            VALUES (?) 
            ON DUPLICATE KEY UPDATE uuid = uuid
        """, QUEST_USERS_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        }
    }

    private void saveCompletedQuests(Connection connection, QuestUserData userData) throws SQLException {
        String deleteSql = String.format("DELETE FROM %s WHERE user_uuid = ?", QUEST_COMPLETED_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, userData.uuid().toString());
            statement.executeUpdate();
        }

        String insertSql = String.format("INSERT INTO %s (user_uuid, quest_key) VALUES (?, ?)", QUEST_COMPLETED_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(insertSql)) {
            for (BaseQuest quest : userData.completeQuests()) {
                statement.setString(1, userData.uuid().toString());
                statement.setString(2, quest.questUniqueKey().getKey());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void saveActiveProgress(Connection connection, QuestUserData userData) throws SQLException {
        String deleteProgressSql = String.format("DELETE FROM %s WHERE user_uuid = ?", QUEST_ACITVE_PROGRESS_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(deleteProgressSql)) {
            statement.setString(1, userData.uuid().toString());
            int deleted = statement.executeUpdate();
            ConsoleLogger.debug(plugin.getName(), "Deleted %d old progress records for user %s", deleted, userData.uuid());
        }

        String insertProgressSql = String.format("""
                INSERT INTO %s (user_uuid, quest_key, objective_key, objective_type) 
                VALUES (?, ?, ?, ?)
                """, QUEST_ACITVE_PROGRESS_TABLE);
        String insertGoalSql = String.format("""
            INSERT INTO %s (progress_id, goal_key, current_value, target_value) 
            VALUES (?, ?, ?, ?) 
            ON DUPLICATE KEY UPDATE 
                current_value = VALUES(current_value),
                target_value = VALUES(target_value)
            """, QUEST_PROGRESS_GOALS_TABLE);

        for (QuestProgress progress : userData.getActiveQuests()) {
            try (PreparedStatement progressStmt = connection.prepareStatement(insertProgressSql, Statement.RETURN_GENERATED_KEYS)) {
                progressStmt.setString(1, userData.uuid().toString());
                progressStmt.setString(2, progress.quest().questUniqueKey().getKey());
                progressStmt.setString(3, progress.objective().key());
                progressStmt.setString(4, progress.objective().type().key());
                progressStmt.executeUpdate();

                ResultSet rs = progressStmt.getGeneratedKeys();
                if (rs.next()) {
                    int progressId = rs.getInt(1);
                    
                    String deleteOldGoalsSql = String.format("DELETE FROM %s WHERE progress_id = ?", QUEST_PROGRESS_GOALS_TABLE);
                    try (PreparedStatement deleteGoalsStmt = connection.prepareStatement(deleteOldGoalsSql)) {
                        deleteGoalsStmt.setInt(1, progressId);
                        deleteGoalsStmt.executeUpdate();
                    }

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

        String deleteSql = String.format("DELETE FROM %s WHERE user_uuid = ?", QUEST_RECEIPTS_REWARDS_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, userData.uuid().toString());
            statement.executeUpdate();
        }

        QuestUserReceiptsRewardsData receiptsData = userData.getReceiptsRewardsData();
        String insertSql = String.format("INSERT INTO %s (user_uuid, reward_uuid, taken_count) VALUES (?, ?, ?)", QUEST_RECEIPTS_REWARDS_TABLE);
        
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
            try (Connection connection = getConnection()) {
                if (!userExists(connection, uuid)) {
                    return new BaseQuestUserData(uuid,
                            new ArrayList<>(),
                            new ArrayList<>(),
                            new QuestUserReceiptsRewardsData());
                }

                List<BaseQuest> completedQuests = loadCompletedQuests(connection, uuid);
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
        });
    }

    @Override
    public QuestUserData loadUserData(UUID uuid) {
        return loadUserDataAsync(uuid).join();
    }

    private boolean userExists(Connection connection, UUID uuid) throws SQLException {
        String sql = String.format("SELECT 1 FROM %s WHERE uuid = ?", QUEST_USERS_TABLE);
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            return rs.next();
        }
    }

    private List<BaseQuest> loadCompletedQuests(Connection connection, UUID uuid) throws SQLException {
        List<BaseQuest> completedQuests = new ArrayList<>();
        String sql = String.format("SELECT quest_key FROM %s WHERE user_uuid = ?", QUEST_COMPLETED_TABLE);
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                String questKey = rs.getString("quest_key");
                try {
                    BaseQuest quest = questStorage.getQuestByUniqueKeyOrThrow(questKey);
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
        String sql = String.format("SELECT reward_uuid, taken_count FROM %s WHERE user_uuid = ?", QUEST_RECEIPTS_REWARDS_TABLE);
        
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
        String sql = String.format("""
                SELECT id, quest_key, objective_key, objective_type 
                FROM %s 
                WHERE user_uuid = ?
                """, QUEST_ACITVE_PROGRESS_TABLE);
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet rs = statement.executeQuery();
            
            while (rs.next()) {
                int progressId = rs.getInt("id");
                String questKey = rs.getString("quest_key");
                String objectiveKey = rs.getString("objective_key");
                
                try {
                    BaseQuest quest = questStorage.getQuestByUniqueKeyOrThrow(questKey);
                    Map<String, Long> goalProgress = loadGoalProgress(connection, progressId);
                    
                    QuestProgress progress = createQuestProgress(quest, objectiveKey, goalProgress, userData);
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
        String sql = String.format("SELECT goal_key, current_value FROM %s WHERE progress_id = ?", QUEST_PROGRESS_GOALS_TABLE);
        
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

    private QuestProgress createQuestProgress(BaseQuest quest, String objectiveKey,
                                             Map<String, Long> goalProgress, QuestUserData userData) {
        return quest.objectives().stream()
                .filter(objective -> objective.key().equals(objectiveKey))
                .findFirst()
                .map(objective -> {
                    BaseQuestProgress progress =
                            new BaseQuestProgress(
                                    userData, quest, objective);
                    for (Goal goal : objective.goals()) {
                        Long currentValue = goalProgress.get(goal.toString());
                        if (currentValue != null && currentValue > 0) {
                            progress.setProgressDirectly(goal, currentValue);
                        }
                    }
                    return progress;
                })
                .orElse(null);
    }

    public CompletableFuture<Void> deleteUserDataAsync(UUID uuid) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = getConnection()) {
                String sql = String.format("DELETE FROM %s WHERE uuid = ?", QUEST_USERS_TABLE);
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
        });
    }

    @Override
    public void deleteUserData(UUID uuid) {
        deleteUserDataAsync(uuid).join();
    }

    public CompletableFuture<Long> getQuestCompletionTimeAsync(UUID uuid, String questKey) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = getConnection()) {
                String sql = String.format("SELECT completed_at FROM %s WHERE user_uuid = ? AND quest_key = ?", QUEST_COMPLETED_TABLE);
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, questKey);
                    ResultSet rs = statement.executeQuery();
                    if (rs.next()) {
                        Timestamp timestamp = rs.getTimestamp("completed_at");
                        return timestamp != null ? timestamp.getTime() : 0L;
                    }
                }
            } catch (SQLException e) {
                ConsoleLogger.error(plugin, "Failed to get quest completion time: %s", e.getMessage());
            }
            return 0L;
        });
    }

    @Override
    public long getQuestCompletionTime(UUID uuid, String questKey) {
        try {
            return getQuestCompletionTimeAsync(uuid, questKey).get();
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Failed to get quest completion time synchronously: %s", e.getMessage());
            return 0L;
        }
    }

    public CompletableFuture<Void> deleteQuestDataAsync(UUID uuid, String questKey) {
        return CompletableFuture.runAsync(() -> {
            ConsoleLogger.info(plugin, "Starting deleteQuestData for user %s, quest %s", uuid, questKey);
            try (Connection connection = getConnection()) {
                connection.setAutoCommit(false);
                try {
                    String deleteCompletedSql = String.format("DELETE FROM %s WHERE user_uuid = ? AND quest_key = ?", QUEST_COMPLETED_TABLE);
                    try (PreparedStatement statement = connection.prepareStatement(deleteCompletedSql)) {
                        statement.setString(1, uuid.toString());
                        statement.setString(2, questKey);
                        statement.executeUpdate();
                    }

                    String deleteProgressSql = String.format("DELETE FROM %s WHERE user_uuid = ? AND quest_key = ?", QUEST_ACITVE_PROGRESS_TABLE);
                    try (PreparedStatement statement = connection.prepareStatement(deleteProgressSql)) {
                        statement.setString(1, uuid.toString());
                        statement.setString(2, questKey);
                        statement.executeUpdate();
                    }

                    connection.commit();
                    ConsoleLogger.info(plugin, "Successfully committed deletion for user %s, quest %s", uuid, questKey);
                } catch (SQLException e) {
                    connection.rollback();
                    ConsoleLogger.error(plugin, "SQL error during deletion, rolled back: %s", e.getMessage());
                    throw e;
                }
            } catch (SQLException e) {
                ConsoleLogger.error(plugin, "Failed to delete quest data: %s", e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Void> deleteQuestData(UUID uuid, String questKey) {
        return deleteQuestDataAsync(uuid, questKey);
    }
}
