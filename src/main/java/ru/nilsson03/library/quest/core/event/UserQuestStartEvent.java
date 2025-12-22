package ru.nilsson03.library.quest.core.event;

import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class UserQuestStartEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final QuestUserData questUserData;
    private final Quest quest;
    private boolean cancelled;

    public UserQuestStartEvent(QuestUserData questUserData, Quest quest) {
        super(!Bukkit.isPrimaryThread());
        this.questUserData = questUserData;
        this.quest = quest;
    }

    public QuestUserData getQuestUserData() {
        return questUserData;
    }

    public Quest getQuest() {
        return quest;
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
