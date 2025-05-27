package ru.nilsson03.library.quest.quest.completer.impl;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.function.Consumer;

public class StagedQuestCompleter implements QuestCompleter {

    private final QuestUsersStorage questUsersStorage;

    public StagedQuestCompleter(QuestUsersStorage questUsersStorage) {
        this.questUsersStorage = questUsersStorage;
    }

    @Override
    public void completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        StagedQuest stagedQuest = (StagedQuest) quest;

        giveReward(user, quest);

        questUsersStorage.addCompleteQuest(user.uuid(), stagedQuest);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }
    }
}
