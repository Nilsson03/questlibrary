package ru.nilsson03.library.quest.quest.completer.impl;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;
import java.util.function.Consumer;

public class BaseQuestCompleter implements QuestCompleter {

    @Override
    public CompleteStatus completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        BaseQuest baseQuest = (BaseQuest) quest;

        Set<QuestCondition> completeConditions = baseQuest.conditions();

        for (QuestCondition questCondition : completeConditions) {
            if (!questCondition.isMet(user)) {
                return CompleteStatus.CONDITIONS_NOT_ACHIEVE;
            }
        }

        giveReward(user, quest);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }

        return CompleteStatus.SUCCESS;
    }
}
