package ru.nilsson03.library.quest.reward.impl;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Абстрактный класс, представляющий награду за выполнение квеста
 * Вызывается, обычно, если игрок завершает квест
 *
 * @see QuestManager
 */
public class SimpleQuestReward implements QuestReward {

    private final UUID uniqueIdentificationKey; // Должно быть уникальным
    private final List<String> commands;

    public SimpleQuestReward(final UUID uniqueIdentificationKey, final List<String> commands) {
        this.uniqueIdentificationKey = uniqueIdentificationKey;
        this.commands = commands;
    }

    public List<String> rewardCommands() {
        return commands;
    }

    public UUID uniqueIdentificationKey() {
        return uniqueIdentificationKey;
    }

    /**
     * Выполнение команд для выдачи награды за выполнение квеста игроку
     *
     * @param questUserData представляет игрока
     */
    public void executeCommands(QuestUserData questUserData) {
        Objects.requireNonNull(questUserData, "User cannot be null");

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(questUserData.uuid());

        if (offlinePlayer.hasPlayedBefore() && offlinePlayer.isOnline()) {

            Player player = offlinePlayer.getPlayer();

            if (!commands.isEmpty()) {
                commands.forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                                                                   command.replace("{player}", player.getName())));
            }
        }
    }
}
