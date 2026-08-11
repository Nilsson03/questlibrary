package ru.nilsson03.library.quest.daily.persistence;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import ru.nilsson03.hikaricp.SharedPoolRegistry;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;

public final class SqlDailyQuestPersistence implements DailyQuestPersistence {

    private final NPlugin plugin;
    private final QuestStorage questStorage;
    private final String sharedTable;
    private final String playerTable;
    private final String updateTable;

    public SqlDailyQuestPersistence(NPlugin plugin, QuestStorage questStorage) {
        this.plugin = plugin;
        this.questStorage = questStorage;
        String prefix = plugin.getName();
        this.sharedTable = prefix + "_daily_quests";
        this.playerTable = prefix + "_daily_player_quests";
        this.updateTable = prefix + "_daily_quests_update";
        initializeTables();
    }

    private Connection getConnection() throws SQLException {
        return SharedPoolRegistry.getConnection(plugin);
    }

    private void initializeTables() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + sharedTable + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "quest_key VARCHAR(255) NOT NULL,"
                    + "selected_at BIGINT NOT NULL"
                    + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + playerTable + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "user_uuid VARCHAR(36) NOT NULL,"
                    + "quest_key VARCHAR(255) NOT NULL,"
                    + "selected_at BIGINT NOT NULL"
                    + ")");
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + updateTable + " ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY,"
                    + "last_update BIGINT NOT NULL"
                    + ")");
            ConsoleLogger.info(plugin, "Daily quest SQL tables initialized");
        } catch (SQLException | RuntimeException e) {
            ConsoleLogger.error(plugin, "Failed to initialize daily quest tables: %s", e.getMessage());
            throw new RuntimeException("Failed to initialize daily quest tables", e);
        }
    }

    @Override
    public Optional<Long> getLastUpdateTime() {
        String sql = "SELECT last_update FROM " + updateTable + " ORDER BY id DESC LIMIT 1";
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            if (rs.next()) {
                return Optional.of(rs.getLong("last_update"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to read daily last_update: %s", e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void saveLastUpdateTime(long timestampMillis) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM " + updateTable);
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO " + updateTable + " (last_update) VALUES (?)")) {
                insert.setLong(1, timestampMillis);
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to save daily last_update: %s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<BaseQuest> loadSharedQuests() {
        String sql = "SELECT quest_key FROM " + sharedTable + " ORDER BY id ASC";
        List<BaseQuest> result = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                resolveQuest(rs.getString("quest_key")).ifPresent(result::add);
            }
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to load shared daily quests: %s", e.getMessage());
        }
        return result;
    }

    @Override
    public void saveSharedQuests(List<BaseQuest> quests, long selectedAtMillis) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate("DELETE FROM " + sharedTable);
                statement.executeUpdate("DELETE FROM " + updateTable);
            }
            if (quests != null) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO " + sharedTable + " (quest_key, selected_at) VALUES (?, ?)")) {
                    for (BaseQuest quest : quests) {
                        insert.setString(1, quest.questUniqueKey().toString());
                        insert.setLong(2, selectedAtMillis);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            try (PreparedStatement insert = connection.prepareStatement(
                    "INSERT INTO " + updateTable + " (last_update) VALUES (?)")) {
                insert.setLong(1, selectedAtMillis);
                insert.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to save shared daily quests: %s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clearSharedQuests() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM " + sharedTable);
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to clear shared daily quests: %s", e.getMessage());
        }
    }

    @Override
    public List<BaseQuest> loadPlayerQuests(UUID playerId) {
        String sql = "SELECT quest_key FROM " + playerTable + " WHERE user_uuid = ? ORDER BY id ASC";
        List<BaseQuest> result = new ArrayList<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    resolveQuest(rs.getString("quest_key")).ifPresent(result::add);
                }
            }
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to load player daily quests: %s", e.getMessage());
        }
        return result;
    }

    @Override
    public void savePlayerQuests(UUID playerId, List<BaseQuest> quests, long selectedAtMillis) {
        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement delete = connection.prepareStatement(
                    "DELETE FROM " + playerTable + " WHERE user_uuid = ?")) {
                delete.setString(1, playerId.toString());
                delete.executeUpdate();
            }
            if (quests != null && !quests.isEmpty()) {
                try (PreparedStatement insert = connection.prepareStatement(
                        "INSERT INTO " + playerTable + " (user_uuid, quest_key, selected_at) VALUES (?, ?, ?)")) {
                    for (BaseQuest quest : quests) {
                        insert.setString(1, playerId.toString());
                        insert.setString(2, quest.questUniqueKey().toString());
                        insert.setLong(3, selectedAtMillis);
                        insert.addBatch();
                    }
                    insert.executeBatch();
                }
            }
            connection.commit();
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to save player daily quests: %s", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<UUID, List<BaseQuest>> loadAllPlayerQuests() {
        String sql = "SELECT user_uuid, quest_key FROM " + playerTable + " ORDER BY id ASC";
        Map<UUID, List<BaseQuest>> result = new HashMap<>();
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("user_uuid"));
                resolveQuest(rs.getString("quest_key")).ifPresent(quest ->
                        result.computeIfAbsent(uuid, id -> new ArrayList<>()).add(quest));
            }
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to load all player daily quests: %s", e.getMessage());
        }
        return result;
    }

    @Override
    public void clearAllPlayerQuests() {
        try (Connection connection = getConnection(); Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM " + playerTable);
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to clear player daily quests: %s", e.getMessage());
        }
    }

    @Override
    public void clearPlayerQuests(UUID playerId) {
        try (Connection connection = getConnection();
                PreparedStatement statement = connection.prepareStatement(
                        "DELETE FROM " + playerTable + " WHERE user_uuid = ?")) {
            statement.setString(1, playerId.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            ConsoleLogger.error(plugin, "Failed to clear player daily quests: %s", e.getMessage());
        }
    }

    private Optional<BaseQuest> resolveQuest(String storedKey) {
        if (storedKey == null || storedKey.isBlank()) {
            return Optional.empty();
        }
        for (BaseQuest quest : questStorage.getQuests()) {
            if (quest.questUniqueKey().toString().equals(storedKey)
                    || quest.questUniqueKey().getKey().equals(storedKey)) {
                return Optional.of(quest);
            }
        }
        ConsoleLogger.warn(plugin, "Daily quest key not found in storage: %s", storedKey);
        return Optional.empty();
    }
}
