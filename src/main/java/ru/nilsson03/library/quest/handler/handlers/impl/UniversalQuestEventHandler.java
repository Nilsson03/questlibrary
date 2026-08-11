package ru.nilsson03.library.quest.handler.handlers.impl;

import java.util.UUID;
import java.util.function.BiConsumer;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.core.progress.ProgressTargetResolver;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

/**
 * Универсальный обработчик событий
 *
 * @param <T> событие Bukkit, которое должен обрабатывать данный слушатель
 * @see QuestEventHandler
 */
public class UniversalQuestEventHandler<T extends Event> implements QuestEventHandler<T> {

    private final ProgressTargetResolver progressTargetResolver;
    private final QuestEventProgressLogic<T> eventHandlerLogic;

    @FunctionalInterface
    public interface QuestEventProgressLogic<T extends Event> {
        void accept(T event, Player actor, QuestUserData owner);
    }

    public UniversalQuestEventHandler(
            QuestUsersStorage questUsersStorage, BiConsumer<T, QuestUserData> eventHandlerLogic) {
        this(ProgressTargetResolver.identity(questUsersStorage),
                (event, actor, owner) -> eventHandlerLogic.accept(event, owner));
    }

    public UniversalQuestEventHandler(
            ProgressTargetResolver progressTargetResolver,
            QuestEventProgressLogic<T> eventHandlerLogic) {
        this.progressTargetResolver = progressTargetResolver;
        this.eventHandlerLogic = eventHandlerLogic;
    }

    public UniversalQuestEventHandler(
            QuestUsersStorage questUsersStorage,
            ProgressTargetResolver progressTargetResolver,
            QuestEventProgressLogic<T> eventHandlerLogic) {
        this(progressTargetResolver != null
                ? progressTargetResolver
                : ProgressTargetResolver.identity(questUsersStorage), eventHandlerLogic);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handle(T event) {
        Player player = getPlayerFromEvent(event);
        if (player == null) {
            return;
        }

        if (!Config.isWorldEnabled(player.getWorld())) {
            return;
        }

        QuestUserData questUserData = progressTargetResolver.resolve(player);
        if (questUserData != null) {
            eventHandlerLogic.accept(event, player, questUserData);
        }
    }

    private Player getPlayerFromEvent(T event) {
        if (event instanceof PlayerInteractEntityEvent interactEntityEvent) {
            return interactEntityEvent.getPlayer();
        }

        if (event instanceof PlayerEvent playerEvent) {
            return playerEvent.getPlayer();
        }

        if (event instanceof BlockBreakEvent blockBreakEvent) {
            return blockBreakEvent.getPlayer();
        }

        if (event instanceof BlockPlaceEvent blockPlaceEvent) {
            return blockPlaceEvent.getPlayer();
        }

        if (event instanceof InventoryClickEvent inventoryClickEvent) {
            return inventoryClickEvent.getWhoClicked() instanceof Player player ? player : null;
        }

        if (event instanceof CraftItemEvent craftItemEvent) {
            return craftItemEvent.getWhoClicked() instanceof Player player ? player : null;
        }

        if (event instanceof EnchantItemEvent enchantItemEvent) {
            return enchantItemEvent.getEnchanter();
        }

        if (event instanceof EntityTameEvent entityTameEvent) {
            return entityTameEvent.getOwner() instanceof Player player ? player : null;
        }

        if (event instanceof EntityDeathEvent entityDeathEvent) {
            Player killer = entityDeathEvent.getEntity().getKiller();
            if (killer != null) {
                return killer;
            }
            if (entityDeathEvent.getEntity() instanceof Player player) {
                return player;
            }
        }

        if (event instanceof FurnaceExtractEvent furnaceExtractEvent) {
            return furnaceExtractEvent.getPlayer();
        }

        if (event instanceof EntityTransformEvent entityTransformEvent) {
            if (entityTransformEvent.getEntity() instanceof Player player) {
                return player;
            }

            if (entityTransformEvent.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
                try {
                    NamespacedKey key = new NamespacedKey("questlibrary", "transform_player");
                    String playerUuidStr = entityTransformEvent.getEntity().getPersistentDataContainer()
                            .get(key, org.bukkit.persistence.PersistentDataType.STRING);

                    if (playerUuidStr != null) {
                        UUID playerUuid = UUID.fromString(playerUuidStr);
                        return org.bukkit.Bukkit.getPlayer(playerUuid);
                    }
                } catch (Exception e) {
                }
            }
        }

        if (event instanceof EntityBreedEvent entityBreedEvent) {
            return entityBreedEvent.getBreeder() instanceof Player player ? player : null;
        }

        if (event instanceof EntityResurrectEvent entityResurrectEvent) {
            return entityResurrectEvent.getEntity() instanceof Player player ? player : null;
        }

        if (event instanceof PlayerShearEntityEvent playerShearEntityEvent) {
            return playerShearEntityEvent.getPlayer();
        }

        if (event instanceof EntityDamageByEntityEvent entityDamageEvent) {
            if (entityDamageEvent.getEntity() instanceof Player defender) {
                return defender;
            }
        }

        if (event instanceof EntityExplodeEvent) {
            return null;
        }

        return null;
    }
}
