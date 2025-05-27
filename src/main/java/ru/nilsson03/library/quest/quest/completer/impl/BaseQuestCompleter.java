package ru.nilsson03.library.quest.quest.completer.impl;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.function.Consumer;

public class BaseQuestCompleter implements QuestCompleter {
    private final QuestUsersStorage questUsersStorage;

    public BaseQuestCompleter(QuestUsersStorage questUsersStorage) {
        this.questUsersStorage = questUsersStorage;
    }

    @Override
    public void completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        BaseQuest baseQuest = (BaseQuest) quest;

        for (QuestCondition questCondition : baseQuest.conditions()) {
            if (!questCondition.isMet(user)) {
                return;
            }
        }

        giveReward(user, quest);

        questUsersStorage.addCompleteQuest(user.uuid(), baseQuest);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }
    }
}
