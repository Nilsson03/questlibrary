package ru.nilsson03.library.quest.handler;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;

import java.util.List;

public class BukkitQuestEventAdapter {

    private final QuestEventManager questEventManager;
    private final Plugin plugin;

    public BukkitQuestEventAdapter(Plugin plugin, QuestEventManager questEventManager) {
        this.plugin = plugin;
        this.questEventManager = questEventManager;
    }

    public void register() {
        for (Class<? extends Event> eventClass : questEventManager.getHandlers().keySet()) {
            EventExecutor executor = (listener, event) -> {
                if (!eventClass.isInstance(event)) {
                    return;
                }
                
                List<QuestEventHandler<?>> handlers = questEventManager.getHandlers().get(eventClass);
                if (handlers != null) {
                    for (QuestEventHandler<?> handler : handlers) {
                        try {
                            handleEvent(event, handler);
                        } catch (Exception e) {
                            plugin.getLogger().severe(
                                "Error handling quest event " + event.getEventName() + ": " + e.getMessage());
                        }
                    }
                }
            };
            
            Bukkit.getPluginManager().registerEvent(
                eventClass,
                new Listener() {},
                EventPriority.MONITOR,
                executor,
                plugin,
                true
            );
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void handleEvent(T event, QuestEventHandler<?> handler) {
        ((QuestEventHandler<T>) handler).handle(event);
    }
}
