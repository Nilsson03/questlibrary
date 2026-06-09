package ru.nilsson03.library.quest.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class QuestUtil {

    public static boolean questIsCompleted(QuestUserData user, BaseQuest quest) {
        return user.completeQuests()
                   .contains(quest);
    }

    /**
     * Проверка, выполнил ли игрок квесты
     *
     * @param user       игрок
     * @param needQuests набор квестов, которые должен игрок выполнить
     * @return true, если все квесты выполнены, false в противном случае
     */
    public static boolean allPreviousQuestComplete(QuestUserData user, Set<BaseQuest> needQuests) {
        Set<Namespace> completeQuests = user.completeQuests()
                                                 .stream()
                                                 .map(BaseQuest::questUniqueKey)
                                                 .collect(Collectors.toSet());

        return needQuests.stream()
                         .allMatch(needQuest -> completeQuests.contains(needQuest.questUniqueKey()));
    }
}
