package ru.nilsson03.library.quest.tracker;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Трекер движения игроков через таймер.
 * Проверяет пройденное расстояние каждые 10 секунд.
 * Поддерживает различные типы передвижения через систему целей.
 */
public class MovementTracker {
    
    private final Plugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveRegistry objectiveRegistry;
    private final Map<UUID, Location> lastLocations = new HashMap<>();
    private BukkitRunnable tracker;
    
    public MovementTracker(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.objectiveRegistry = objectiveRegistry;
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
        
        if (distance > 0) {
            QuestUserData questUserData = questUsersStorage.getQuestUserData(uuid);
            
            if (questUserData != null) {
                MovementType movementType = determineMovementType(player);
                
                questUserData.incrementProgressQuestsWithObjectiveType(
                    objectiveRegistry.getObjectiveType("MOVE"), 
                    movementType, 
                    (long) Math.round(distance)
                );
            }
        }
        
        lastLocations.put(uuid, currentLocation.clone());
    }
    
    /**
     * Определяет тип движения игрока
     */
    private MovementType determineMovementType(Player player) {
        if (player.isGliding()) {
            return MovementType.FLY;
        } else if (player.isInsideVehicle() && player.getVehicle() != null) {
            EntityType vehicleType = player.getVehicle().getType();
            
            if (vehicleType == EntityType.HORSE || 
                vehicleType == EntityType.DONKEY || 
                vehicleType == EntityType.MULE || 
                vehicleType == EntityType.SKELETON_HORSE || 
                vehicleType == EntityType.ZOMBIE_HORSE) {
                return MovementType.HORSE;
            }
            else if (vehicleType == EntityType.BOAT || vehicleType.name().contains("BOAT")) {
                return MovementType.BOAT;
            }
            else if (vehicleType == EntityType.PIG) {
                return MovementType.PIG;
            }
            else if (vehicleType.name().equals("STRIDER")) {
                return MovementType.STRIDER;
            }
            else {
                return MovementType.VEHICLE;
            }
        } else {
            return MovementType.WALK;
        }
    }
    
    public void removePlayer(UUID uuid) {
        lastLocations.remove(uuid);
    }

    public enum MovementType {
        WALK,      // Ходьба/бег
        FLY,       // Полёт с элитрами
        BOAT,      // Лодка
        HORSE,     // Лошадь, осёл, мул и т.д.
        PIG,       // Свинья
        STRIDER,   // Страйдер
        VEHICLE,   // Другие транспортные средства
        ANY        // Любой тип движения
    }
}
