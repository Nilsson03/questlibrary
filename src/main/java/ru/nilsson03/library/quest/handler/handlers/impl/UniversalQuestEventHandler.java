package ru.nilsson03.library.quest.handler.handlers.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
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
        try {
            return (Player) event.getClass()
                                 .getMethod("getPlayer")
                                 .invoke(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get player from event", e);
        }
    }
}
