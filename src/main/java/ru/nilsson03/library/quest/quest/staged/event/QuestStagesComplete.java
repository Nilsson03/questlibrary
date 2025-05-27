package ru.nilsson03.library.quest.quest.staged.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import ru.nilsson03.library.quest.core.Quest;

public class QuestStagesComplete extends Event {

    protected static final HandlerList handlers = new HandlerList();

    private final Quest quest;
    private final Player player;

    public QuestStagesComplete(Quest quest, Player player) {
        this.quest = quest;
        this.player = player;
    }

    public Quest getQuest() {
        return quest;
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
