# QuestLibrary

Библиотека для создания и управления квестами в Minecraft с поддержкой ежедневных квестов и автоматического сброса.

## Структура квеста

### Основные поля

- **id**: Уникальный идентификатор квеста
- **meta**: Метаданные квеста
  - **displayName**: Отображаемое имя
  - **weight**: Вес квеста (для сортировки)
  - **daily**: Ежедневный квест (true/false)
  - **description**: Список строк описания
  - **updateTime**: Интервал сброса для daily квестов (только для DailyQuestMeta)
- **conditions**: Условия для начала квеста
- **objectives**: Список целей квеста
- **reward**: Награда за выполнение

## Типы метаданных квестов

### SimpleQuestMeta
Стандартные метаданные для обычных квестов (по умолчанию):
```yaml
meta:
  displayName: "Обычный квест"
  weight: 1
  description:
    - "Описание квеста"
```

Или явно указать тип:
```yaml
meta:
  type: "simple"
  displayName: "Обычный квест"
  weight: 1
  description:
    - "Описание квеста"
```

### DailyQuestMeta
Расширенные метаданные для ежедневных квестов с автоматическим сбросом:
```yaml
meta:
  type: "daily"  # Обязательно для daily квестов
  displayName: "Ежедневный квест"
  weight: 1
  updateTime: "1d"  # Интервал сброса
  description:
    - "Ежедневный квест с автоматическим сбросом"
```

**Важно:** Параметр `type: "daily"` обязателен для ежедневных квестов. Система автоматически определяет тип квеста по этому параметру и использует соответствующий парсер из `MetaParserRegistry`.

## Расширение системы метаданных

### MetaParserRegistry

Система использует `MetaParserRegistry` для управления парсерами метаданных. Это позволяет легко добавлять кастомные типы квестов:

```java
// Получение реестра парсеров
BaseQuestLoader loader = new BaseQuestLoader(questService);
MetaParserRegistry registry = loader.getMetaParserRegistry();

// Регистрация кастомного парсера
registry.registerParser("weekly", new WeeklyMetaParser());
registry.registerParser("monthly", new MonthlyMetaParser());
```

**Стандартные парсеры:**
- `"simple"` (по умолчанию) → `SimpleMetaParser` → `SimpleQuestMeta`
- `"daily"` → `DailyMetaParser` → `DailyQuestMeta`

**Создание кастомного парсера:**
```java
public class WeeklyMetaParser implements Parser<QuestMeta> {
    @Override
    public QuestMeta parse(ConfigurationSection section) {
        // Ваша логика парсинга
        return new WeeklyQuestMeta(...);
    }
}
```

### Обратная совместимость

Все существующие квесты без параметра `type` автоматически используют `SimpleMetaParser` и продолжат работать без изменений.

**Форматы updateTime:**
- `"1d"` - 1 день
- `"12h"` - 12 часов
- `"7d"` - 7 дней (еженедельный)
- `"30m"` - 30 минут
- `"60s"` - 60 секунд

## Система автоматического сброса квестов

### Как это работает

1. **Создание daily квеста**: Используйте `DailyQuestMeta` с параметром `updateTime`
2. **Завершение квеста**: Время завершения автоматически сохраняется в БД
3. **Автоматическая проверка**: Сервис проверяет квесты каждый час (настраивается)
4. **Сброс данных**: Если прошло >= `updateTime`, данные удаляются из БД и памяти
5. **Повторное прохождение**: Игрок может снова начать квест

### Инициализация сервиса обновления

```java

QuestManager questManager = questService.getQuestManager();
QuestStorage questStorage = questLibrary.getQuestStorage(plugin);

questManager.startQuestUpdateService(questStorage);
```

### Настройка интервала проверки

```java
// Кастомный интервал проверки (в тиках, 20 тиков = 1 секунда)
long checkIntervalTicks = 20 * 60 * 30; 

QuestUpdateService updateService = new QuestUpdateService(
    plugin,
    questUsersStorage,
    userDataPersistent,
    questStorage,
    checkIntervalTicks
);
updateService.start();
```

### Пример конфигурации daily квеста

```yaml
id: "daily_mining_quest"

meta:
  type: "daily"
  displayName: "§6Ежедневная добыча"
  weight: 1
  updateTime: "1d"
  description:
    - "§7Добудьте 64 алмазной руды"
    - "§7Сбрасывается каждый день"

objectives:
  mine_diamonds:
    key: "mine_diamonds"
    type: "BREAK_BLOCK"
    goals:
      - type: "MATERIAL"
        material: "DIAMOND_ORE"
        target: 64

reward:
  uniqueIdentificationKey: "daily-mining-reward-uuid"
  commands:
    - "eco give %player% 1000"
    - "give %player% diamond 10"
```

### Типы условий (conditions)

#### quest_completed
Проверяет, выполнен ли другой квест:
```yaml
conditions:
  quest_completed:
    type: "quest_completed"
    quest_completed: "tutorial_quest"
```

#### has_item
Проверяет наличие предмета в инвентаре:
```yaml
conditions:
  has_item:
    type: "has_item"
    material: "IRON_HOE"
    amount: 1
```

#### and
Логическое И (все условия должны быть выполнены):
```yaml
conditions:
  and:
    condition1:
      type: "has_item"
      material: "DIAMOND_PICKAXE"
      amount: 1
    condition2:
      type: "quest_completed"
      quest_completed: "miner_quest"
```

#### or
Логическое ИЛИ (хотя бы одно условие должно быть выполнено):
```yaml
conditions:
  or:
    quest_1:
      type: "quest_completed"
      quest_completed: "gather_wood"
    quest_2:
      type: "quest_completed"
      quest_completed: "farmer_quest"
```

### Типы целей (objectives)

Каждая цель имеет:
- **key**: Уникальный ключ цели
- **type**: Тип события (см. ObjectiveType)
- **goals**: Список конкретных задач
- **potionEffects** (опционально): Эффекты зелий, которые должны быть у игрока

#### Доступные типы целей:

- **BREAK_BLOCK**: Сломать блоки
- **BLOCK_PLACE**: Поставить блоки
- **KILL_ENTITY**: Убить сущность
- **CRAFT_ITEM**: Скрафтить предмет
- **SMELT_ITEM**: Переплавить предмет
- **EAT_ITEM**: Съесть предмет
- **DRINK_POTION**: Выпить зелье
- **TAME_ENTITY**: Приручить сущность
- **CATCH_FISH**: Поймать рыбу
- **ENCHANT**: Зачаровать предмет
- **ANVIL**: Использовать наковальню
- **MOVE**: Пройти расстояние
- **EXP_CHANGE**: Получить опыт
- **TRANSFORM_ENTITY**: Трансформация сущности
- **TRADE_VILLAGER**: Торговля с жителем
- **ITEM_DESTROY**: Сломать инструмент
- **RIDE_HORSE**: Ездить на лошади
- **DEATH**: Умереть

### Типы задач (goals)

#### MATERIAL
Для блоков/предметов:
```yaml
goals:
  - type: "MATERIAL"
    material: "DIAMOND_ORE"
    target: 16
```

#### ENTITY_TYPE
Для сущностей:
```yaml
goals:
  - type: "ENTITY_TYPE"
    entity_type: "ZOMBIE"
    target: 20
```

#### VALUE
Для числовых значений (расстояние, опыт и т.д.):
```yaml
goals:
  - type: "VALUE"
    target: 10000
```

### Награды (reward)

```yaml
reward:
  uniqueIdentificationKey: "уникальный-uuid"
  commands:
    - "eco give %player% 100"
    - "give %player% diamond 5"
    - "tellraw %player% {\"text\":\"Квест выполнен!\",\"color\":\"green\"}"
```

Плейсхолдеры:
- `%player%` - имя игрока

## Примеры

1. **simple_quest.yml** - Простой квест на сбор ресурсов
2. **multi_objective_quest.yml** - Квест с несколькими целями
3. **combat_quest.yml** - Боевой квест с условиями
4. **exploration_quest.yml** - Квест на исследование

## Использование

1. Скопируйте нужный пример в папку `plugins/QuestLibrary/quests/`
2. Измените `id` и `uniqueIdentificationKey` на уникальные значения
3. Настройте цели, условия и награды под свои нужды
4. Перезагрузите плагин или сервер
