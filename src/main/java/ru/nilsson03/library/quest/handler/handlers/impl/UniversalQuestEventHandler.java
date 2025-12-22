package ru.nilsson03.library.quest.handler.handlers.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.function.BiConsumer;

/**
 * Универсальный обработчик событий
 *
 * @param <T> событие Bukkit, которое должен обрабатывать данный слушатель
 * @see QuestEventHandler<T>
 */
public class UniversalQuestEventHandler<T extends Event> implements QuestEventHandler<T> {

    private final QuestUsersStorage questUsersStorage;
    private final BiConsumer<T, QuestUserData> eventHandlerLogic;

    public UniversalQuestEventHandler(
            QuestUsersStorage questUsersStorage, BiConsumer<T, QuestUserData> eventHandlerLogic) {
        this.questUsersStorage = questUsersStorage;
        this.eventHandlerLogic = eventHandlerLogic;
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

        QuestUserData questUserData = questUsersStorage.getQuestUserData(player.getUniqueId());
        if (questUserData != null) {
            eventHandlerLogic.accept(event, questUserData);
        }
    }

    private Player getPlayerFromEvent(T event) {
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

        return null;
    }
}
