package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class HasItemCondition implements QuestCondition {
    private final Material itemType;
    private final int amount;

    public HasItemCondition(Material itemType, int amount) {
        this.itemType = itemType;
        this.amount = amount;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        int count = 0;

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.uuid());

        if (!offlinePlayer.hasPlayedBefore() && !offlinePlayer.isOnline()) {
            return false;
        }

        Player player = offlinePlayer.getPlayer();

        for (ItemStack item : player.getInventory()
                                    .getContents()) {
            if (item != null && item.getType() == itemType) {
                count += item.getAmount();
            }
        }
        return count >= amount;
    }
}
