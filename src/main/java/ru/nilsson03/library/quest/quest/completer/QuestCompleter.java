package ru.nilsson03.library.quest.quest.completer;

import java.util.function.Consumer;

import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.data.impl.QuestUserReceiptsRewardsData;

public interface QuestCompleter {

    void completeQuest(QuestUserData user, BaseQuest quest, Consumer<QuestUserData> questUserDataConsumer);

    default void giveReward(QuestUserData user, BaseQuest quest) {
        if (quest.rewards() == null) {
            return;
        }

        quest.rewards()
                .executeCommands(user);

        if (user.hasActiveReceiptsRewardsData()) {
            QuestUserReceiptsRewardsData questUserReceiptsRewardsData = user.getReceiptsRewardsData();
            questUserReceiptsRewardsData.takeReward(quest.rewards());
        }
    }
}
