# QuestLibrary 1.3

Библиотека квестов для Paper и Purpur 26.2 на Java 25.

```sh
mvn clean verify
```

- `objectives/` — Maven-артефакт `ru.nilsson03:questlibrary-objectives` с `Goal`, `ObjectiveGoalFactory`, числовой фабрикой и чистыми правилами прогресса. Подходит для приложения со своей моделью хранения и событий.
- `paper/` — прежний Bukkit API квестов, событий, YAML и хранилищ. Собранный плагин `paper/target/QuestLibrary.jar` включает модуль целей.

## Переход с 1.2.2

Компилируйте интеграции заново для Java 25. В публичных конструкторах и методах, принимавших `NPlugin`, теперь используется `org.bukkit.plugin.Plugin`. Утилиты QuestLibrary находятся в `ru.nilsson03.library.quest.util`. Это относится и к типам, на которые ссылаются публичные методы, например `Namespace`, `Pair` и `ReplaceData`. В форматах сообщений поддерживаются MiniMessage и привычные цветовые коды `&`.

Для существующего MySQL-хранилища укажите свой `javax.sql.DataSource` через `QuestSystemBuilder.withDataSource(dataSource)`. Для daily-хранилища передайте тот же объект в конструктор `SqlDailyQuestPersistence(plugin, questStorage, dataSource)`. Владельцем пула остаётся вызывающий плагин. Также доступен прежний пул Database-core через адаптер, если Database-core установлен и зарегистрировал пул для плагина. Сохраняются текущие SQL-таблицы и префиксы. Для файлового режима передайте `FileUserPersistent` через `withUserPersistent(...)`.
