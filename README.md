# Примеры квестов для QuestLibrary

Этот каталог содержит примеры конфигурационных файлов квестов для демонстрации возможностей библиотеки.

## Структура квеста

### Основные поля

- **id**: Уникальный идентификатор квеста
- **meta**: Метаданные квеста
  - **displayName**: Отображаемое имя
  - **weight**: Вес квеста (для сортировки)
  - **daily**: Ежедневный квест (true/false)
  - **description**: Список строк описания
- **conditions**: Условия для начала квеста
- **objectives**: Список целей квеста
- **reward**: Награда за выполнение

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
