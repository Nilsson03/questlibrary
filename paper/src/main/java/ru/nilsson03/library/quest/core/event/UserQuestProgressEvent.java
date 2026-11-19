package ru.nilsson03.library.quest.core.event;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class UserQuestProgressEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestUserData questUserData;
    private final BaseQuest quest;
    private final Objective objective;
    private final Goal goal;
    private final long previousValue;
    private final long newValue;
    private final UUID actorId;
    private boolean cancelled;

    public UserQuestProgressEvent(
            QuestUserData questUserData,
            BaseQuest quest,
            Objective objective,
            Goal goal,
            long previousValue,
            long newValue) {
        this(questUserData, quest, objective, goal, previousValue, newValue, (Player) null);
    }

    public UserQuestProgressEvent(
            QuestUserData questUserData,
            BaseQuest quest,
            Objective objective,
            Goal goal,
            long previousValue,
            long newValue,
            Player actor) {
        super(!Bukkit.isPrimaryThread());
        this.questUserData = questUserData;
        this.quest = quest;
        this.objective = objective;
        this.goal = goal;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.actorId = actor != null ? actor.getUniqueId() : null;
    }

    public UserQuestProgressEvent(
            QuestUserData questUserData,
            BaseQuest quest,
            Objective objective,
            Goal goal,
            long previousValue,
            long newValue,
            UUID actorId) {
        super(!Bukkit.isPrimaryThread());
        this.questUserData = questUserData;
        this.quest = quest;
        this.objective = objective;
        this.goal = goal;
        this.previousValue = previousValue;
        this.newValue = newValue;
        this.actorId = actorId;
    }

    public QuestUserData getQuestUserData() {
        return questUserData;
    }

    public BaseQuest getQuest() {
        return quest;
    }

    public Objective getObjective() {
        return objective;
    }

    public Goal getGoal() {
        return goal;
    }

    public long getPreviousValue() {
        return previousValue;
    }

    public long getNewValue() {
        return newValue;
    }

    /**
     * Absolute progress delta applied by this update ({@code newValue - previousValue}).
     */
    public long getDelta() {
        return newValue - previousValue;
    }

    /**
     * Player who caused the progress update, when known (e.g. guild member action).
     */
    public Optional<UUID> getActorId() {
        return Optional.ofNullable(actorId);
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
