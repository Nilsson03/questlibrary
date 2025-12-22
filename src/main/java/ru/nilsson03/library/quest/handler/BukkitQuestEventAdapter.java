package ru.nilsson03.library.quest.handler;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;

import java.util.List;

public class BukkitQuestEventAdapter implements Listener {

    private final QuestEventManager questEventManager;
    private final Plugin plugin;

    public BukkitQuestEventAdapter(Plugin plugin, QuestEventManager questEventManager) {
        this.plugin = plugin;
        this.questEventManager = questEventManager;
    }

    public void register() {
        Bukkit.getPluginManager()
              .registerEvents(this, plugin);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEvent(Event event) {
        Class<? extends Event> eventClass = event.getClass();
        List<QuestEventHandler<?>> handlers = questEventManager.getHandlers()
                                                               .get(eventClass);

        if (handlers != null) {
            for (QuestEventHandler<?> handler : handlers) {
                handleEvent(event, handler);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T extends Event> void handleEvent(T event, QuestEventHandler<?> handler) {
        ((QuestEventHandler<T>) handler).handle(event);
    }
}
