package ru.nilsson03.library.quest.util;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public final class QuestLogContext {
    private QuestLogContext() { }

    public static String player(UUID uuid) {
        Player player = uuid != null ? Bukkit.getPlayer(uuid) : null;
        return player != null ? player.getName() + " (" + uuid + ")" : String.valueOf(uuid);
    }
}
