package ru.nilsson03.library.quest.daily.persistence;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import ru.nilsson03.library.quest.quest.simple.BaseQuest;

public interface DailyQuestPersistence {

    Optional<Long> getLastUpdateTime();

    void saveLastUpdateTime(long timestampMillis);

    List<BaseQuest> loadSharedQuests();

    void saveSharedQuests(List<BaseQuest> quests, long selectedAtMillis);

    void clearSharedQuests();

    List<BaseQuest> loadPlayerQuests(UUID playerId);

    void savePlayerQuests(UUID playerId, List<BaseQuest> quests, long selectedAtMillis);

    Map<UUID, List<BaseQuest>> loadAllPlayerQuests();

    void clearAllPlayerQuests();

    void clearPlayerQuests(UUID playerId);
}
