package ru.nilsson03.library.quest.tracker;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

public class PlaytimeObjectiveTracker {

    private static final int TICKS_PER_SECOND = 20;
    private static final long UPDATE_INTERVAL_TICKS = 20L;

    private final JavaPlugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveType playtimeObjectiveType;

    private final Map<UUID, Map<BaseQuest, Integer>> questStartTicks = new HashMap<>();
    private final Map<UUID, Integer> lastKnownTicks = new HashMap<>();
    private final Map<UUID, Integer> pendingTicks = new HashMap<>();

    private BukkitTask asyncTask;

    public PlaytimeObjectiveTracker(JavaPlugin plugin,
            QuestUsersStorage questUsersStorage,
            ObjectiveType playtimeObjectiveType) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.playtimeObjectiveType = playtimeObjectiveType;
    }

    public void start() {
        if (asyncTask != null) {
            return;
        }

        asyncTask = Bukkit.getScheduler().runTaskTimer(
                plugin,
                this::processPlaytime,
                UPDATE_INTERVAL_TICKS,
                UPDATE_INTERVAL_TICKS);
    }

    public void stop() {
        if (asyncTask != null) {
            asyncTask.cancel();
            asyncTask = null;
        }
        questStartTicks.clear();
        lastKnownTicks.clear();
        pendingTicks.clear();
    }

    private void processPlaytime() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!Config.isWorldEnabled(player.getWorld())) {
                continue;
            }

            UUID uuid = player.getUniqueId();

            QuestUserData questUserData = questUsersStorage.getQuestUserData(uuid);
            if (questUserData == null) {
                continue;
            }

            Collection<QuestProgress> activePlaytimeProgress = questUserData
                    .getProgressByObjectiveType(playtimeObjectiveType);

            if (activePlaytimeProgress.isEmpty()) {
                questStartTicks.remove(uuid);
                lastKnownTicks.remove(uuid);
                pendingTicks.remove(uuid);
                continue;
            }

            int currentTicks = player.getStatistic(Statistic.PLAY_ONE_MINUTE);
            ensureQuestStartTimes(uuid, currentTicks, activePlaytimeProgress);
            updatePlayerPlaytime(questUserData, uuid, currentTicks);
        }

        cleanupOfflinePlayers();
    }

    private void ensureQuestStartTimes(UUID uuid, int currentTicks, Collection<QuestProgress> activePlaytimeProgress) {
        Map<BaseQuest, Integer> playerQuestStarts = questStartTicks.computeIfAbsent(uuid, k -> new HashMap<>());

        for (QuestProgress progress : activePlaytimeProgress) {
            BaseQuest quest = progress.quest();
            playerQuestStarts.putIfAbsent(quest, currentTicks);
        }

        playerQuestStarts.keySet().removeIf(quest -> activePlaytimeProgress.stream().map(QuestProgress::quest)
                .noneMatch(activeQuest -> activeQuest.equals(quest)));
    }

    private void updatePlayerPlaytime(QuestUserData questUserData, UUID uuid, int currentTicks) {
        if (!lastKnownTicks.containsKey(uuid)) {
            lastKnownTicks.put(uuid, currentTicks);
            return;
        }

        int lastTicks = lastKnownTicks.get(uuid);
        int deltaTicks = Math.max(0, currentTicks - lastTicks);

        if (deltaTicks == 0) {
            return;
        }

        int accumulatedTicks = deltaTicks + pendingTicks.getOrDefault(uuid, 0);
        long secondsToAdd = accumulatedTicks / TICKS_PER_SECOND;
        int remainder = accumulatedTicks % TICKS_PER_SECOND;

        pendingTicks.put(uuid, remainder);
        lastKnownTicks.put(uuid, currentTicks);

        if (secondsToAdd > 0) {
            questUserData.incrementProgressQuestsWithValueGoals(playtimeObjectiveType, secondsToAdd);
        }
    }

    private void cleanupOfflinePlayers() {
        questStartTicks.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        lastKnownTicks.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
        pendingTicks.keySet().removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
    }
}
