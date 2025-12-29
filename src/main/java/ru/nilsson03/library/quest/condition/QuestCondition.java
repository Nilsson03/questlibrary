package ru.nilsson03.library.quest.condition;

import ru.nilsson03.library.quest.user.data.QuestUserData;

@FunctionalInterface
public interface QuestCondition {
    boolean isMet(QuestUserData user);
    
    /**
     * Возвращает тип условия.
     * START - проверяется только при старте квеста
     * PROGRESS - проверяется при каждом обновлении прогресса
     * По умолчанию START
     */
    default ConditionType getType() {
        return ConditionType.START;
    }
    
    enum ConditionType {
        START,   
        PROGRESS 
    }
}
