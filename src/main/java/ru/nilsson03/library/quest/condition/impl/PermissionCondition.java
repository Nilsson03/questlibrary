package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import lombok.AllArgsConstructor;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

@AllArgsConstructor
public class PermissionCondition implements QuestCondition {
    
    private final String permission;

    @Override
    public boolean isMet(QuestUserData questUserData) {
        Player player = Bukkit.getPlayer(questUserData.uuid());
        return player.hasPermission(permission);
    }
}
