package ru.nilsson03.library.quest.objective.registry;

import org.bukkit.event.Event;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public interface ObjectiveType {

    String key();

    Class<? extends Event> eventClass();

    ConcurrentHashMap<String, ObjectiveType> GLOBAL_CACHE = new ConcurrentHashMap<>();

    static ObjectiveType create(String key, Class<? extends Event> eventClass) {
        String cacheKey = key.toLowerCase() + ":" + eventClass.getName();

        return GLOBAL_CACHE.computeIfAbsent(cacheKey, k -> new ObjectiveType() {
            @Override
            public String key() {
                return key;
            }

            @Override
            public Class<? extends Event> eventClass() {
                return eventClass;
            }
            
            @Override
            public boolean equals(Object obj) {
                if (this == obj) return true;
                if (!(obj instanceof ObjectiveType)) return false;
                ObjectiveType other = (ObjectiveType) obj;
                return Objects.equals(key(), other.key()) && 
                       Objects.equals(eventClass(), other.eventClass());
            }
            
            @Override
            public int hashCode() {
                return Objects.hash(key(), eventClass());
            }
            
            @Override
            public String toString() {
                return "ObjectiveType{key='" + key() + "', eventClass=" + eventClass().getSimpleName() + "}";
            }
        });
    }
}
