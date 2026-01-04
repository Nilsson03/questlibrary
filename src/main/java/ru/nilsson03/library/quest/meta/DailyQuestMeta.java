package ru.nilsson03.library.quest.meta;


public interface DailyQuestMeta extends QuestMeta {

    String updateTime();
    
    @Override
    default boolean daily() {
        return true;
    }
}
