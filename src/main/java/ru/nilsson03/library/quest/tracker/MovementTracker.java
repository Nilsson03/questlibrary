package ru.nilsson03.library.quest.tracker;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.core.progress.ProgressTargetResolver;
import ru.nilsson03.library.quest.objective.goal.impl.MovementTypeGoal;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

/**
 * Трекер движения игроков через таймер.
 * Проверяет пройденное расстояние каждые 10 секунд.
 * Поддерживает различные типы передвижения через систему целей.
 */
public class MovementTracker {

    private final Plugin plugin;
    private final ObjectiveRegistry objectiveRegistry;
    private final ProgressTargetResolver progressTargetResolver;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private BukkitRunnable tracker;

    public MovementTracker(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this(plugin, questUsersStorage, objectiveRegistry, ProgressTargetResolver.identity(questUsersStorage));
    }

    public MovementTracker(
            Plugin plugin,
            QuestUsersStorage questUsersStorage,
            ObjectiveRegistry objectiveRegistry,
            ProgressTargetResolver progressTargetResolver) {
        this.plugin = plugin;
        this.objectiveRegistry = objectiveRegistry;
        this.progressTargetResolver = progressTargetResolver != null
                ? progressTargetResolver
                : ProgressTargetResolver.identity(questUsersStorage);
    }

    public void start() {
        tracker = new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    checkPlayerMovement(player);
                }
            }
        };

        tracker.runTaskTimer(plugin, 200L, 200L);
    }

    public void stop() {
        if (tracker != null) {
            tracker.cancel();
            tracker = null;
        }
        lastLocations.clear();
    }

    private void checkPlayerMovement(Player player) {
        UUID uuid = player.getUniqueId();
        Location currentLocation = player.getLocation();

        Location lastLocation = lastLocations.get(uuid);

        if (lastLocation == null) {
            lastLocations.put(uuid, currentLocation.clone());
            return;
        }

        if (!lastLocation.getWorld().equals(currentLocation.getWorld())) {
            lastLocations.put(uuid, currentLocation.clone());
            return;
        }

        double distance = lastLocation.distance(currentLocation);

        if (distance > 0 && Config.isWorldEnabled(currentLocation.getWorld())) {
            QuestUserData questUserData = progressTargetResolver.resolve(player);

            if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                    objectiveRegistry.getObjectiveType("MOVE"))) {
                MovementTypeGoal.MovementType movementType = determineMovementType(player);
                long roundedDistance = (long) Math.round(distance);

                questUserData.incrementProgressQuestsWithObjectiveType(
                        objectiveRegistry.getObjectiveType("MOVE"),
                        movementType,
                        roundedDistance,
                        player);
            }
        }

        lastLocations.put(uuid, currentLocation.clone());
    }

    /**
     * Определяет тип движения игрока
     */
    private MovementTypeGoal.MovementType determineMovementType(Player player) {
        if (player.isGliding()) {
            return MovementTypeGoal.MovementType.FLY;
        } else if (player.isInsideVehicle() && player.getVehicle() != null) {
            EntityType vehicleType = player.getVehicle().getType();

            if (vehicleType == EntityType.HORSE ||
                    vehicleType == EntityType.DONKEY ||
                    vehicleType == EntityType.MULE ||
                    vehicleType == EntityType.SKELETON_HORSE ||
                    vehicleType == EntityType.ZOMBIE_HORSE) {
                return MovementTypeGoal.MovementType.HORSE;
            } else if (vehicleType == EntityType.BOAT || vehicleType.name().contains("BOAT")) {
                return MovementTypeGoal.MovementType.BOAT;
            } else if (vehicleType == EntityType.PIG) {
                return MovementTypeGoal.MovementType.PIG;
            } else if (vehicleType.name().equals("STRIDER")) {
                return MovementTypeGoal.MovementType.STRIDER;
            } else {
                return MovementTypeGoal.MovementType.VEHICLE;
            }
        } else {
            return MovementTypeGoal.MovementType.WALK;
        }
    }

    public void removePlayer(UUID uuid) {
        lastLocations.remove(uuid);
    }
}
