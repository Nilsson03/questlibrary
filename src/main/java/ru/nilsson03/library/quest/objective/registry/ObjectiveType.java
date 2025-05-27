package ru.nilsson03.library.quest.objective.registry;

import org.bukkit.event.Event;

public interface ObjectiveType {

    String key();

    Class<? extends Event> eventClass();

    static ObjectiveType create(String key, Class<? extends Event> eventClass) {
        return new ObjectiveType() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public Class<? extends Event> eventClass() {
                return eventClass;
            }
        };
    }


}
