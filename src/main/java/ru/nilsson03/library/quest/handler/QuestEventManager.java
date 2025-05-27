package ru.nilsson03.library.quest.handler;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Инициализация данного класса и остальных компонентов системы слушателей
 * происходит в классе
 *
 * @see QuestManager
 */
public class QuestEventManager {

    private final QuestEventHandlers questEventHandlers;
    private final BukkitQuestEventAdapter bukkitQuestEventAdapter;
    private final Map<Class<? extends Event>, List<QuestEventHandler<?>>> eventHandlers = new HashMap<>();
    private final Plugin plugin;

    public QuestEventManager(Plugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this.plugin = plugin;

        this.questEventHandlers = new QuestEventHandlers(this, questUsersStorage, objectiveRegistry);
        this.bukkitQuestEventAdapter = new BukkitQuestEventAdapter(plugin, this);
    }

    /**
     * Метод для регистрации всех обработчиков событий, включая слушателя событий для Bukkit
     *
     * @see BukkitQuestEventAdapter
     * @see QuestEventHandlers
     */
    public void register() {
        questEventHandlers.registerHandlers();
        bukkitQuestEventAdapter.register();
    }

    protected void registerHandler(ObjectiveType eventType, QuestEventHandler<?> handler) {
        Class<? extends Event> eventClass = eventType.eventClass();
        this.registerHandler(eventClass, handler);
    }

    /**
     * Регистрация обработчика событий для событий
     * В случае, если обработчик для данного события уже существует, то будет добавлен и этот
     * В случае, когда вызывается данное событие, то будут задействованы все обработчики, связанные с этим событием
     *
     * @param eventClass класс, который наследуется Event
     * @param handler    обработчик, который будет отвечать за данное событие
     * @see org.bukkit.event.Event
     * @see BukkitQuestEventAdapter
     */
    public void registerHandler(Class<? extends Event> eventClass, QuestEventHandler<?> handler) {
        eventHandlers.computeIfAbsent(eventClass, k -> new ArrayList<>())
                     .add(handler);
    }

    private BukkitQuestEventAdapter getBukkitQuestEventAdapter() {
        return bukkitQuestEventAdapter;
    }

    private QuestEventHandlers getQuestEventHandlers() {
        return questEventHandlers;
    }

    protected Map<Class<? extends Event>, List<QuestEventHandler<?>>> getHandlers() {
        return eventHandlers;
    }

    /**
     * Плагин Java, к которому относится данный менеджер обработчиков событий
     * Необходимо, чтобы при использовании в нескольких плагинов можно было использовать
     * разные обработчики
     *
     * @return плагин, для которого работает менеджер
     */
    public Plugin getPlugin() {
        return plugin;
    }
}
