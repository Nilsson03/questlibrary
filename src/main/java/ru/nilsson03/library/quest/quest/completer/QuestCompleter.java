package ru.nilsson03.library.quest.quest.completer;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.impl.QuestUserReceiptsRewardsData;

import java.util.function.Consumer;

public interface QuestCompleter {

    void completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer);

    default void giveReward(QuestUserData user, Quest quest) {
        quest.rewards()
             .executeCommands(user);

        if (user.hasActiveReceiptsRewardsData()) {
            QuestUserReceiptsRewardsData questUserReceiptsRewardsData = user.getReceiptsRewardsData();
            questUserReceiptsRewardsData.takeReward(quest.rewards());
        }
    }
}
