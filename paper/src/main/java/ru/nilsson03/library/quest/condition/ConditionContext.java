package ru.nilsson03.library.quest.condition;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.entity.Player;

import ru.nilsson03.library.quest.user.data.QuestSubjectKind;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public final class ConditionContext {

    private final QuestUserData owner;
    private final Player actor;

    public ConditionContext(QuestUserData owner) {
        this(owner, null);
    }

    public ConditionContext(QuestUserData owner, Player actor) {
        this.owner = Objects.requireNonNull(owner, "owner");
        this.actor = actor;
    }

    public static ConditionContext of(QuestUserData owner) {
        return new ConditionContext(owner);
    }

    public static ConditionContext of(QuestUserData owner, Player actor) {
        return new ConditionContext(owner, actor);
    }

    public QuestUserData owner() {
        return owner;
    }

    public Optional<Player> actor() {
        return Optional.ofNullable(actor);
    }

    public Optional<UUID> actorId() {
        return actor().map(Player::getUniqueId);
    }

    public QuestSubjectKind subjectKind() {
        return owner.subjectKind();
    }

    public boolean isGroupOwner() {
        return subjectKind() == QuestSubjectKind.GROUP;
    }

    public Optional<Player> playerForChecks() {
        if (actor != null) {
            return Optional.of(actor);
        }
        if (owner.subjectKind() == QuestSubjectKind.PLAYER) {
            return Optional.ofNullable(org.bukkit.Bukkit.getPlayer(owner.uuid()));
        }
        return Optional.empty();
    }
}
