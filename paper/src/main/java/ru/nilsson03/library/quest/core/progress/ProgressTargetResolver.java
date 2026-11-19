package ru.nilsson03.library.quest.core.progress;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

import org.bukkit.entity.Player;

import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

@FunctionalInterface
public interface ProgressTargetResolver {

    QuestUserData resolve(Player actor);

    static ProgressTargetResolver identity(QuestUsersStorage storage) {
        Objects.requireNonNull(storage, "storage");
        return actor -> {
            if (actor == null) {
                return null;
            }
            return storage.getQuestUserData(actor.getUniqueId());
        };
    }

    static ProgressTargetResolver mapping(QuestUsersStorage storage, Function<UUID, UUID> actorToOwnerId) {
        Objects.requireNonNull(storage, "storage");
        Objects.requireNonNull(actorToOwnerId, "actorToOwnerId");
        return actor -> {
            if (actor == null) {
                return null;
            }
            UUID ownerId = actorToOwnerId.apply(actor.getUniqueId());
            if (ownerId == null) {
                return null;
            }
            return storage.getQuestUserData(ownerId);
        };
    }
}
