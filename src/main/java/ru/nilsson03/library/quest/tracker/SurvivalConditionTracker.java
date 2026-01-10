package ru.nightvision.quests.objective.survival;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitTask;

import ru.nightvision.quests.goal.SurvivalConditionGoal;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SurvivalConditionTracker {

    private final Plugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveType objectiveType;
    private BukkitTask task;
    private final Map<UUID, Long> lastCheckTime = new HashMap<>();

    public SurvivalConditionTracker(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveType objectiveType) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.objectiveType = objectiveType;
    }

    public void start() {
        if (task != null) {
            task.cancel();
        }

        task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long currentTime = System.currentTimeMillis();

            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID playerId = player.getUniqueId();
                QuestUserData userData = questUsersStorage.getQuestUserData(playerId);

                if (userData == null || !userData.hasActiveQuestWithCurrentObjectiveType(objectiveType)) {
                    continue;
                }

                Long lastCheck = lastCheckTime.get(playerId);
                if (lastCheck == null) {
                    lastCheckTime.put(playerId, currentTime);
                    continue;
                }

                long elapsedSeconds = (currentTime - lastCheck) / 1000;
                if (elapsedSeconds < 1) {
                    continue;
                }

                for (PotionEffect effect : player.getActivePotionEffects()) {
                    SurvivalConditionGoal.SurvivalData survivalData = new SurvivalConditionGoal.SurvivalData(
                        effect.getType(),
                        player.getWorld(),
                        player.getLocation().getBlock().getBiome()
                    );

                    userData.incrementProgressQuestsWithObjectiveType(objectiveType, survivalData, elapsedSeconds);
                }

                lastCheckTime.put(playerId, currentTime);
            }
        }, 20L, 20L);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        lastCheckTime.clear();
    }

    public void removePlayer(UUID playerId) {
        lastCheckTime.remove(playerId);
    }
}
