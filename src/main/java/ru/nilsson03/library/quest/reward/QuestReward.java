package ru.nilsson03.library.quest.reward;

import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;
import java.util.UUID;

public interface QuestReward {

    List<String> messageInfo();
    List<String> rewardCommands();
    List<String> getDescription();
    UUID uniqueIdentificationKey();

    void executeCommands(QuestUserData questUserData);
}
