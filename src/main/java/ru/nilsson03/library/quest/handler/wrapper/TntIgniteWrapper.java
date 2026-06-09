package ru.nilsson03.library.quest.handler.wrapper;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.handler.QuestEventManager;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.handler.handlers.impl.UniversalQuestEventHandler;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

public class TntIgniteWrapper {

    private static final NamespacedKey TNT_IGNITER_KEY = new NamespacedKey("questlibrary", "tnt_igniter");
    private final QuestEventManager eventManager;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveRegistry objectiveRegistry;
    private final Plugin plugin;

    public TntIgniteWrapper(QuestEventManager eventManager, QuestUsersStorage questUsersStorage,
            ObjectiveRegistry objectiveRegistry, Plugin plugin) {
        this.eventManager = eventManager;
        this.questUsersStorage = questUsersStorage;
        this.objectiveRegistry = objectiveRegistry;
        this.plugin = plugin;
    }

    public void registerHandlers() {
        // Отложенная проверка и регистрация обработчика dynamitesticks
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (isDynamiteSticksEnabled()) {
                ConsoleLogger.info(plugin.getName(), "DynamiteSticks plugin detected! Registering custom handler.");
                registerDynamiteSticksHandler();
            } else {
                ConsoleLogger.info(plugin.getName(), "DynamiteSticks plugin not found. Using vanilla TNT handler.");
            }
        });
        
        registerVanillaHandler();
    }

    private boolean isDynamiteSticksEnabled() {
        Plugin dynamitePlugin = Bukkit.getPluginManager().getPlugin("dynamitesticks");
        return dynamitePlugin != null && dynamitePlugin.isEnabled();
    }

    private void registerDynamiteSticksHandler() {
        try {
            Class<?> eventClass = Class.forName("ru.nilsson03.dynamitesticks.api.events.DynamiteIgniteEvent");
            QuestEventHandler<Event> handler = (event) -> {
                try {
                    Object igniter = event.getClass().getMethod("getIgniter").invoke(event);
                    if (igniter instanceof Player player) {
                        var questUserData = questUsersStorage.getQuestUserData(player.getUniqueId());
                        
                        if (questUserData != null) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("IGNITE_TNT"), 1);

                            Object tntPrimed = event.getClass().getMethod("getTntPrimed").invoke(event);
                            ((TNTPrimed) tntPrimed).getPersistentDataContainer().set(
                                    TNT_IGNITER_KEY,
                                    PersistentDataType.STRING,
                                    player.getUniqueId().toString());
                        }
                    }
                } catch (Exception e) {
                    ConsoleLogger.error(plugin.getName(), "Произошла ошибка при обработке события поджога динамита (DynamiteSticks) %s", e.getMessage());
                }
            };

            eventManager.registerHandler((Class) eventClass, handler);

            Bukkit.getPluginManager().registerEvent(
                (Class<? extends Event>) eventClass,
                new Listener() {},
                EventPriority.MONITOR,
                (listener, event) -> {
                    if (eventClass.isInstance(event)) {
                        handler.handle(event);
                    }
                },
                plugin,
                true
            );

        } catch (ClassNotFoundException e) {
            ConsoleLogger.error(plugin.getName(), "Failed to load DynamiteIgniteEvent class: %s", e.getMessage());
        } catch (Exception e) {
            ConsoleLogger.error(plugin.getName(), "Error registering DynamiteIgniteEvent handler: %s", e.getMessage());
            e.printStackTrace();
        }
    }

    private void registerVanillaHandler() {
        QuestEventHandler<PlayerInteractEvent> vanillaHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (isDynamiteSticksEnabled()) {
                        return;
                    }

                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Block block = event.getClickedBlock();
                        if (block != null && block.getType() == Material.TNT) {
                            ItemStack item = event.getItem();
                            if (item != null && (item.getType() == Material.FLINT_AND_STEEL
                                    || item.getType() == Material.FIRE_CHARGE)) {
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("IGNITE_TNT"), 1);

                                Location tntLocation = block.getLocation();
                                UUID playerId = questUserData.uuid();

                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    tntLocation.getWorld().getNearbyEntities(tntLocation, 1.5, 1.5, 1.5).stream()
                                            .filter(entity -> entity.getType() == EntityType.PRIMED_TNT)
                                            .findFirst()
                                            .ifPresent(tnt -> {
                                                tnt.getPersistentDataContainer().set(
                                                        TNT_IGNITER_KEY,
                                                        PersistentDataType.STRING,
                                                        playerId.toString());
                                            });
                                }, 1L);
                            }
                        }
                    }
                });

        eventManager.registerHandler(PlayerInteractEvent.class, vanillaHandler);
    }
}
