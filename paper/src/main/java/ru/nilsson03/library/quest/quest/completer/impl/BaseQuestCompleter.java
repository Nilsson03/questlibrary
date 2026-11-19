package ru.nilsson03.library.quest.quest.completer.impl;

import java.util.function.Consumer;

import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class BaseQuestCompleter implements QuestCompleter {

    @Override
    public void completeQuest(QuestUserData user, BaseQuest quest, Consumer<QuestUserData> questUserDataConsumer) {
        giveReward(user, quest);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }
    }
}
