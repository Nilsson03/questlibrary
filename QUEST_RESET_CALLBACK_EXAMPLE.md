# Пример использования QuestResetCallback

## Описание

`QuestResetCallback` позволяет интегрировать вашу систему сохранения с сервисом обновления дневных квестов. Callback вызывается **перед** сбросом квеста, что позволяет синхронизировать вашу систему с библиотекой.

## Базовый пример

```java

import ru.nilsson03.library.quest.quest.simple.BaseQuest;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MyQuestPlugin extends JavaPlugin {

    private QuestUpdateService questUpdateService;
    private MyCustomQuestStorage myStorage;

    @Override
    public void onEnable() {
        // Инициализация вашего хранилища
        myStorage = new MyCustomQuestStorage();

        // Инициализация QuestUpdateService
        questUpdateService = new QuestUpdateService(
                this,
                questUsersStorage,
                userDataPersistent,
                questStorage
        );

        // Регистрация callback для синхронизации
        questUpdateService.registerResetCallback(this::onQuestReset);

        // Запуск сервиса
        questUpdateService.start();
    }

    // Ваш обработчик сброса квеста
    private CompletableFuture<Void> onQuestReset(UUID uuid, BaseQuest quest) {
        return CompletableFuture.runAsync(() -> {
            // Ваша логика сохранения/удаления
            myStorage.saveQuestResetData(uuid, quest);
            myStorage.clearUserProgress(uuid, quest.questUniqueKey().getKey());

            getLogger().info("Custom storage synchronized for quest reset: " +
                    quest.questUniqueKey().getKey() + " for player " + uuid);
        });
    }

    @Override
    public void onDisable() {
        if (questUpdateService != null) {
            questUpdateService.stop();
        }
    }
}
```

## Продвинутый пример с базой данных

```java
public class DatabaseQuestCallback implements QuestResetCallback {
    
    private final DataSource dataSource;
    private final Logger logger;
    
    public DatabaseQuestCallback(DataSource dataSource, Logger logger) {
        this.dataSource = dataSource;
        this.logger = logger;
    }
    
    @Override
    public CompletableFuture<Void> onQuestReset(UUID uuid, BaseQuest quest) {
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                
                try {
                    // Сохранить историю сброса
                    saveResetHistory(conn, uuid, quest);
                    
                    // Очистить кастомные данные
                    clearCustomData(conn, uuid, quest);
                    
                    // Обновить статистику
                    updateStatistics(conn, uuid, quest);
                    
                    conn.commit();
                    logger.info("Database synchronized for quest reset");
                } catch (SQLException e) {
                    conn.rollback();
                    throw new RuntimeException("Failed to sync database", e);
                }
            } catch (SQLException e) {
                logger.severe("Database error during quest reset: " + e.getMessage());
                throw new RuntimeException(e);
            }
        });
    }
    
    private void saveResetHistory(Connection conn, UUID uuid, BaseQuest quest) throws SQLException {
        String sql = "INSERT INTO quest_reset_history (user_uuid, quest_key, reset_at) VALUES (?, ?, NOW())";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, quest.questUniqueKey().getKey());
            stmt.executeUpdate();
        }
    }
    
    private void clearCustomData(Connection conn, UUID uuid, BaseQuest quest) throws SQLException {
        String sql = "DELETE FROM my_custom_quest_data WHERE user_uuid = ? AND quest_key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, quest.questUniqueKey().getKey());
            stmt.executeUpdate();
        }
    }
    
    private void updateStatistics(Connection conn, UUID uuid, BaseQuest quest) throws SQLException {
        String sql = "UPDATE quest_statistics SET reset_count = reset_count + 1 WHERE user_uuid = ? AND quest_key = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setString(2, quest.questUniqueKey().getKey());
            stmt.executeUpdate();
        }
    }
}

// Использование:
DatabaseQuestCallback callback = new DatabaseQuestCallback(dataSource, getLogger());
questUpdateService.registerResetCallback(callback);
```

## Множественные callback'и

```java
// Можно регистрировать несколько callback'ов
questUpdateService.registerResetCallback(this::saveToDatabase);
questUpdateService.registerResetCallback(this::notifyPlayer);
questUpdateService.registerResetCallback(this::updateCache);

// Все callback'и выполняются параллельно и асинхронно
```

## Отмена регистрации

```java
QuestResetCallback myCallback = (uuid, quest) -> {
    // Ваша логика
    return CompletableFuture.completedFuture(null);
};

// Регистрация
questUpdateService.registerResetCallback(myCallback);

// Отмена регистрации
questUpdateService.unregisterResetCallback(myCallback);
```

## Важные моменты

1. **Асинхронность**: Callback выполняется асинхронно через `CompletableFuture`
2. **Порядок выполнения**: Callback вызывается **ДО** удаления данных из БД библиотеки
3. **Обработка ошибок**: Ошибки в callback логируются, но не останавливают процесс сброса
4. **Thread-safety**: Используется `CopyOnWriteArrayList` для безопасной работы с callback'ами
5. **Множественность**: Можно регистрировать несколько callback'ов, все выполнятся параллельно
