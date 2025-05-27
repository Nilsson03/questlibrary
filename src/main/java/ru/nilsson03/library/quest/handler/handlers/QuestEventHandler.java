package ru.nilsson03.library.quest.handler.handlers;

import org.bukkit.event.Event;

@FunctionalInterface
public interface QuestEventHandler<T extends Event> {

    /**
     * Метод, который будет вызываться каждый раз, когда событие будет вызываться
     *
     * @param event событие, для которого должен вызываться данный обработчик
     */
    void handle(T event);
}
