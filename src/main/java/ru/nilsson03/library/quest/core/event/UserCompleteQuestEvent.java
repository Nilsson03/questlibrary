package ru.nilsson03.library.quest.core.event;

import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class UserCompleteQuestEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();

    private final NPlugin plugin;
    private final QuestUserData questUserData;
    private final BaseQuest quest;
    private final CompleteStatus status;
    private final UUID actorId;
    private boolean cancelled;

    public UserCompleteQuestEvent(NPlugin plugin, QuestUserData questUserData, BaseQuest quest, CompleteStatus status) {
        this(plugin, questUserData, quest, status, (UUID) null);
    }

    public UserCompleteQuestEvent(
            NPlugin plugin, QuestUserData questUserData, BaseQuest quest, CompleteStatus status, Player actor) {
        this(plugin, questUserData, quest, status, actor != null ? actor.getUniqueId() : null);
    }

    public UserCompleteQuestEvent(
            NPlugin plugin, QuestUserData questUserData, BaseQuest quest, CompleteStatus status, UUID actorId) {
        super(!Bukkit.isPrimaryThread());
        this.plugin = plugin;
        this.questUserData = questUserData;
        this.quest = quest;
        this.status = status;
        this.actorId = actorId;
    }

    public NPlugin getPlugin() {
        return plugin;
    }

    public QuestUserData getQuestUserData() {
        return questUserData;
    }

    public BaseQuest getQuest() {
        return quest;
    }

    public CompleteStatus getStatus() {
        return status;
    }

    /**
     * Optional actor who triggered completion (e.g. last contributor).
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
