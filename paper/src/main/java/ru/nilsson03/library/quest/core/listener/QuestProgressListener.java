package ru.nilsson03.library.quest.core.listener;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.core.event.UserQuestProgressEvent;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.util.text.QuestText;
import ru.nilsson03.library.quest.util.text.ReplaceData;

public class QuestProgressListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onProgress(UserQuestProgressEvent event) {
        if (event.isCancelled()) {
            return;
        }

        BaseQuest quest = event.getQuest();
        QuestUserData userData = event.getQuestUserData();
        Goal goal = event.getGoal();

        long requiredValue = goal.targetValue();

        if (userData == null || quest == null) {
            return;
        }

        Player player = event.getActorId()
                .map(Bukkit::getPlayer)
                .orElseGet(() -> Bukkit.getPlayer(userData.uuid()));

        if (player == null) {
            return;
        }

        long progress = event.getNewValue();
        QuestText.send(player, QuestText.replacePlaceholders(Config.messages_QuestProgress(),
                new ReplaceData("{quest}", quest.questMeta().displayName()),
                new ReplaceData("{progress}", progress),
                new ReplaceData("{required}", requiredValue)));
    }
}
