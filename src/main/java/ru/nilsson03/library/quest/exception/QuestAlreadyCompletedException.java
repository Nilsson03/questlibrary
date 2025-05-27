package ru.nilsson03.library.quest.exception;

public class QuestAlreadyCompletedException extends IllegalStateException {
    public QuestAlreadyCompletedException(String message) {
        super(message);
    }
}
