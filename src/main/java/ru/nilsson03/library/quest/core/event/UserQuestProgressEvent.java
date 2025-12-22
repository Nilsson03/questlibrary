package ru.nilsson03.library.quest.core.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class UserQuestProgressEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestUserData questUserData;
    private final Quest quest;
    private final Objective objective;
    private final Goal goal;
    private final long previousValue;
    private final long newValue;
    private boolean cancelled;

    public UserQuestProgressEvent(
            QuestUserData questUserData,
            Quest quest,
            Objective objective,
            Goal goal,
            long previousValue,
            long newValue) {
        super(!Bukkit.isPrimaryThread());
        this.questUserData = questUserData;
        this.quest = quest;
        this.objective = objective;
        this.goal = goal;
        this.previousValue = previousValue;
        this.newValue = newValue;
    }

    public QuestUserData getQuestUserData() {
        return questUserData;
    }

    public Quest getQuest() {
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
