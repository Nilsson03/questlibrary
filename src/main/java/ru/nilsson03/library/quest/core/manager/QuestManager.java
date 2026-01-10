package ru.nilsson03.library.quest.core.manager;

import java.util.function.Consumer;

import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.core.service.QuestLifecycleService;
import ru.nilsson03.library.quest.core.service.QuestProgressService;
import ru.nilsson03.library.quest.handler.QuestEventManager;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.objective.factory.registry.QuestProgressFactoryRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.tracker.PlaytimeObjectiveTracker;
import ru.nilsson03.library.quest.tracker.SurvivalConditionTracker;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;
import ru.nilsson03.library.quest.tracker.MovementTracker;

@Getter
public class QuestManager implements Listener {

    private final NPlugin plugin;
    private final QuestUsersStorage questUsersStorage;
    private final QuestLifecycleService questLifecycleService;
    private final QuestEventManager questEventManager;
    private final QuestProgressService questProgressService;

    private final MovementTracker movementTracker;
    private final PlaytimeObjectiveTracker playtimeObjectiveTracker;
    private final SurvivalConditionTracker survivalConditionTracker;

    /**
     * Конструктор класса
     * Во время инициализации класса инициализируется менеджер, который так же принимает в качестве параметров javaPlugin, а так же
     * хранилище игроков
     *
     * @param plugin            плагин, к которому должен относится данный менеджер
     * @param questUsersStorage реализация хранилища игроков
     * @see QuestEventManager
     */
    public QuestManager(NPlugin plugin, QuestUsersStorage questUsersStorage, ObjectiveRegistry objectiveRegistry) {
        this.plugin = plugin;
        this.questUsersStorage = questUsersStorage;
        this.questEventManager = new QuestEventManager(plugin, questUsersStorage, objectiveRegistry);

        QuestProgressFactoryRegistry factoryRegistry = new QuestProgressFactoryRegistry();
        QuestCompleterRegistry questCompleterRegistry = new QuestCompleterRegistry(questUsersStorage);
        questCompleterRegistry.onRegisterInit();

        this.questProgressService = new QuestProgressService(factoryRegistry);
        this.questLifecycleService = new QuestLifecycleService(questProgressService, questCompleterRegistry);
        
        this.movementTracker = new MovementTracker(plugin, questUsersStorage, objectiveRegistry);
        ObjectiveType survivalType = objectiveRegistry.getObjectiveType("SURVIVAL_CONDITION");
        survivalConditionTracker = new SurvivalConditionTracker(plugin, questUsersStorage, survivalType);
        ObjectiveType playtimeType = objectiveRegistry.getObjectiveType("PLAYTIME");
        playtimeObjectiveTracker = new PlaytimeObjectiveTracker(plugin, questUsersStorage, playtimeType);
    }

    public void registerEventHandlers() {
        questEventManager.register();
        movementTracker.start();
        playtimeObjectiveTracker.start();
        survivalConditionTracker.start();
    }

    public void shutdown() {
        if (this.movementTracker != null) {
            this.movementTracker.stop();
        }
        if (playtimeObjectiveTracker != null) {
            playtimeObjectiveTracker.stop();
        }
        if (survivalConditionTracker != null) {
            survivalConditionTracker.stop();
        }
    }

    /**
     * Используется для завершения квеста игроком
     *
     * @param user                  игрок
     * @param quest                 квест
     * @param questUserDataConsumer дополнительные действия, которые могут быть совершены с игроком
     */
    public CompleteStatus completeQuest(QuestUserData user, BaseQuest quest, Consumer<QuestUserData> questUserDataConsumer) {
        return questLifecycleService.completeQuest(user, quest, questUserDataConsumer);
    }

    /**
     * Используется для начала выполнения квеста игроком
     *
     * @param user                  игрок, который должен начать выполнять
     * @param quest                 квест, который будет выполнять игрок
     * @param questUserDataConsumer дополнительные действия, которые могут быть связаны с игроком
     */
    public void startQuest(QuestUserData user, BaseQuest quest, Consumer<QuestUserData> questUserDataConsumer) {
        questLifecycleService.startQuest(user, quest, questUserDataConsumer);
    }

    @EventHandler
    public void onDisable(PluginDisableEvent event) {
        shutdown();
    }

    @EventHandler
    public void onLeft(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (movementTracker != null) {
            movementTracker.removePlayer(player.getUniqueId());
        }
        if (survivalConditionTracker != null) {
            survivalConditionTracker.removePlayer(player.getUniqueId());
        }
    }
}
