package ru.nilsson03.library.quest.tracker;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.goal.impl.SurvivalConditionGoal;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurvivalConditionTracker {

    private final Plugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveType objectiveType;
    private BukkitRunnable tracker;
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public SurvivalConditionTracker(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveType objectiveType) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.objectiveType = objectiveType;
    }

    public void start() {
        tracker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkPlayerSurvivalConditions(player);
                }
            }
        };
        
        tracker.runTaskTimer(plugin, 20L, 20L);
    }
    
    private void checkPlayerSurvivalConditions(Player player) {
        UUID playerId = player.getUniqueId();
        QuestUserData userData = questUsersStorage.getQuestUserData(playerId);

        if (userData == null || !userData.hasActiveQuestWithCurrentObjectiveType(objectiveType)) {
            lastCheckTime.remove(playerId);
            return;
        }

        long currentTime = System.currentTimeMillis();
        Long lastCheck = lastCheckTime.get(playerId);
        
        if (lastCheck == null) {
            ConsoleLogger.info(plugin.getName(), 
                "SurvivalConditionTracker: First check for player %s", player.getName());
            lastCheckTime.put(playerId, currentTime);
            return;
        }

        long elapsedSeconds = (currentTime - lastCheck) / 1000;
        if (elapsedSeconds < 1) {
            return;
        }

        int effectCount = player.getActivePotionEffects().size();
        
        if (effectCount > 0) {
            for (PotionEffect effect : player.getActivePotionEffects()) {
                SurvivalConditionGoal.SurvivalData survivalData = new SurvivalConditionGoal.SurvivalData(
                    effect.getType(),
                    player.getWorld().getName(),
                    player.getLocation().getBlock().getBiome()
                );

                userData.incrementProgressQuestsWithObjectiveType(objectiveType, survivalData, elapsedSeconds);
            }
        } else {
            SurvivalConditionGoal.SurvivalData survivalData = new SurvivalConditionGoal.SurvivalData(
                null,
                player.getWorld().getName(),
                player.getLocation().getBlock().getBiome()
            );

            userData.incrementProgressQuestsWithObjectiveType(objectiveType, survivalData, elapsedSeconds);
        }

        lastCheckTime.put(playerId, currentTime);
    }

    public void stop() {
        if (tracker != null) {
            tracker.cancel();
            tracker = null;
        }
        lastCheckTime.clear();
    }

    public void removePlayer(UUID playerId) {
        lastCheckTime.remove(playerId);
    }
}
