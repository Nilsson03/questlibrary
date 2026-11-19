package ru.nilsson03.library.quest.daily.persistence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import ru.nilsson03.library.quest.quest.simple.BaseQuest;

public final class InMemoryDailyQuestPersistence implements DailyQuestPersistence {

    private volatile Long lastUpdate;
    private final List<BaseQuest> sharedQuests = new CopyOnWriteArrayList<>();
    private final Map<UUID, List<BaseQuest>> playerQuests = new ConcurrentHashMap<>();

    @Override
    public Optional<Long> getLastUpdateTime() {
        return Optional.ofNullable(lastUpdate);
    }

    @Override
    public void saveLastUpdateTime(long timestampMillis) {
        this.lastUpdate = timestampMillis;
    }

    @Override
    public List<BaseQuest> loadSharedQuests() {
        return List.copyOf(sharedQuests);
    }

    @Override
    public void saveSharedQuests(List<BaseQuest> quests, long selectedAtMillis) {
        sharedQuests.clear();
        if (quests != null) {
            sharedQuests.addAll(quests);
        }
        lastUpdate = selectedAtMillis;
    }

    @Override
    public void clearSharedQuests() {
        sharedQuests.clear();
    }

    @Override
    public List<BaseQuest> loadPlayerQuests(UUID playerId) {
        List<BaseQuest> quests = playerQuests.get(playerId);
        return quests == null ? List.of() : List.copyOf(quests);
    }

    @Override
    public void savePlayerQuests(UUID playerId, List<BaseQuest> quests, long selectedAtMillis) {
        playerQuests.put(playerId, new ArrayList<>(quests == null ? List.of() : quests));
        if (lastUpdate == null) {
            lastUpdate = selectedAtMillis;
        }
    }

    @Override
    public Map<UUID, List<BaseQuest>> loadAllPlayerQuests() {
        Map<UUID, List<BaseQuest>> copy = new ConcurrentHashMap<>();
        playerQuests.forEach((id, quests) -> copy.put(id, List.copyOf(quests)));
        return copy;
    }

    @Override
    public void clearAllPlayerQuests() {
        playerQuests.clear();
    }

    @Override
    public void clearPlayerQuests(UUID playerId) {
        playerQuests.remove(playerId);
    }
}
