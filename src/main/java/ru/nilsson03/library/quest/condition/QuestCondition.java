package ru.nilsson03.library.quest.condition;

import ru.nilsson03.library.quest.user.data.QuestUserData;

@FunctionalInterface
public interface QuestCondition {
    boolean isMet(QuestUserData user);
}
