package ru.nilsson03.library.quest.util;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.stream.Collectors;

public class QuestUtil {

    public static boolean questIsCompleted(QuestUserData user, Quest quest) {
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
    public static boolean allPreviousQuestComplete(QuestUserData user, Set<Quest> needQuests) {
        Set<Namespace> completeQuests = user.completeQuests()
                                                 .stream()
                                                 .map(Quest::questUniqueKey)
                                                 .collect(Collectors.toSet());

        return needQuests.stream()
                         .allMatch(needQuest -> completeQuests.contains(needQuest.questUniqueKey()));
    }
}
