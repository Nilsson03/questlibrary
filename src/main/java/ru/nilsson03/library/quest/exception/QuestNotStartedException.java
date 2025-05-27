package ru.nilsson03.library.quest.exception;

public class QuestNotStartedException extends IllegalStateException {

    public QuestNotStartedException(String message) {
        super(message);
    }
}
