package ru.nilsson03.library.quest.core.service;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.meta.DailyQuestMeta;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class QuestUpdateService {

    private final NPlugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final UserDataPersistent userDataPersistent;
    private final QuestStorage questStorage;
    private BukkitTask updateTask;
    private final long checkIntervalTicks;

    public QuestUpdateService(NPlugin plugin, QuestUsersStorage questUsersStorage,
                             UserDataPersistent userDataPersistent, QuestStorage questStorage) {
        this(plugin, questUsersStorage, userDataPersistent, questStorage, TimeUnit.HOURS.toSeconds(1) * 20);
    }

    public QuestUpdateService(NPlugin plugin, QuestUsersStorage questUsersStorage,
                             UserDataPersistent userDataPersistent, QuestStorage questStorage, 
                             long checkIntervalTicks) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.userDataPersistent = userDataPersistent;
        this.questStorage = questStorage;
        this.checkIntervalTicks = checkIntervalTicks;
    }

    public void start() {
        if (updateTask != null) {
            ConsoleLogger.warn(plugin, "Quest update service is already running");
            return;
        }

        updateTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::checkAndResetExpiredQuests, 
                checkIntervalTicks, checkIntervalTicks);
        
        ConsoleLogger.info(plugin, "Quest update service started with check interval: %d ticks", checkIntervalTicks);
    }

    public void stop() {
        if (updateTask != null) {
            updateTask.cancel();
            updateTask = null;
            ConsoleLogger.info(plugin, "Quest update service stopped");
        }
    }

    private void checkAndResetExpiredQuests() {
        try {
            List<BaseQuest> dailyQuests = getDailyQuests();
            
            if (dailyQuests.isEmpty()) {
                return;
            }

            long currentTime = System.currentTimeMillis();
            
            for (BaseQuest quest : dailyQuests) {
                if (!(quest.questMeta() instanceof DailyQuestMeta dailyMeta)) {
                    continue;
                }

                long updateIntervalMillis = parseUpdateTime(dailyMeta.updateTime());
                if (updateIntervalMillis <= 0) {
                    ConsoleLogger.warn(plugin, "Invalid update time format for quest %s: %s", 
                            quest.questUniqueKey().getKey(), dailyMeta.updateTime());
                    continue;
                }

                resetExpiredQuestForAllUsers(quest, currentTime, updateIntervalMillis);
            }
        } catch (Exception e) {
            ConsoleLogger.error(plugin, "Error in quest update service: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void resetExpiredQuestForAllUsers(BaseQuest quest, long currentTime, long updateIntervalMillis) {
        List<CompletableFuture<UUID>> resetFutures = new ArrayList<>();

        for (QuestUserData userData : getAllLoadedUsers()) {
            if (userData.questIsComplete(quest)) {
                CompletableFuture<UUID> resetFuture = checkAndResetUserQuest(
                    userData.uuid(), quest, currentTime, updateIntervalMillis
                );
                resetFutures.add(resetFuture);
            }
        }

        CompletableFuture.allOf(resetFutures.toArray(new CompletableFuture[0]))
            .thenAccept(v -> {
                long resetCount = resetFutures.stream()
                    .map(CompletableFuture::join)
                    .filter(uuid -> uuid != null)
                    .count();
                
                if (resetCount > 0) {
                    ConsoleLogger.info(plugin, "Reset quest %s for %d users", 
                            quest.questUniqueKey().getKey(), resetCount);
                }
            });
    }

    private CompletableFuture<UUID> checkAndResetUserQuest(UUID uuid, BaseQuest quest,
                                                           long currentTime, long updateIntervalMillis) {
        return getQuestCompletionTimeAsync(uuid, quest)
            .thenCompose(completionTime -> {
                if (completionTime > 0 && (currentTime - completionTime) >= updateIntervalMillis) {
                    return resetQuestForUserAsync(uuid, quest)
                        .thenApply(v -> uuid);
                }
                return CompletableFuture.completedFuture(null);
            })
            .exceptionally(ex -> {
                ConsoleLogger.error(plugin, "Error checking/resetting quest for user %s: %s", 
                    uuid, ex.getMessage());
                return null;
            });
    }

    private CompletableFuture<Void> resetQuestForUserAsync(UUID uuid, BaseQuest quest) {
        return userDataPersistent.deleteQuestData(uuid, quest.questUniqueKey().getKey())
            .thenRun(() -> {
                QuestUserData userData = questUsersStorage.getQuestUserData(uuid);
                if (userData != null) {
                    userData.removeQuestProgress(quest);
                    
                    List<BaseQuest> completedQuests = userData.completeQuests();
                    completedQuests.removeIf(q -> q.questUniqueKey().equals(quest.questUniqueKey()));
                }
                
                ConsoleLogger.debug(plugin.getName(), "Reset quest %s for user %s", 
                        quest.questUniqueKey().getKey(), uuid);
            })
            .exceptionally(ex -> {
                ConsoleLogger.error(plugin, "Failed to reset quest %s for user %s: %s", 
                        quest.questUniqueKey().getKey(), uuid, ex.getMessage());
                return null;
            });
    }

    private List<BaseQuest> getDailyQuests() {
        List<BaseQuest> dailyQuests = new ArrayList<>();
        
        for (BaseQuest quest : questStorage.getQuests()) {
            if (quest.questMeta() instanceof DailyQuestMeta) {
                dailyQuests.add(quest);
            }
        }
        
        return dailyQuests;
    }

    private List<QuestUserData> getAllLoadedUsers() {
        return new ArrayList<>(questUsersStorage.getAllLoadedUsers());
    }

    private CompletableFuture<Long> getQuestCompletionTimeAsync(UUID uuid, BaseQuest quest) {
        return CompletableFuture.supplyAsync(() -> 
            userDataPersistent.getQuestCompletionTime(uuid, quest.questUniqueKey().getKey())
        );
    }

    public static long parseUpdateTime(String updateTime) {
        if (updateTime == null || updateTime.isEmpty()) {
            return -1;
        }

        updateTime = updateTime.trim().toLowerCase();
        
        try {
            if (updateTime.endsWith("d")) {
                int days = Integer.parseInt(updateTime.substring(0, updateTime.length() - 1));
                return TimeUnit.DAYS.toMillis(days);
            } else if (updateTime.endsWith("h")) {
                int hours = Integer.parseInt(updateTime.substring(0, updateTime.length() - 1));
                return TimeUnit.HOURS.toMillis(hours);
            } else if (updateTime.endsWith("m")) {
                int minutes = Integer.parseInt(updateTime.substring(0, updateTime.length() - 1));
                return TimeUnit.MINUTES.toMillis(minutes);
            } else if (updateTime.endsWith("s")) {
                int seconds = Integer.parseInt(updateTime.substring(0, updateTime.length() - 1));
                return TimeUnit.SECONDS.toMillis(seconds);
            }
        } catch (NumberFormatException e) {
            return -1;
        }

        return -1;
    }
}
