package ru.nilsson03.library.quest.exception;

public class UserAlreadyHasQuestProgressException extends IllegalStateException {
    public UserAlreadyHasQuestProgressException(String message) {
        super(message);
    }
}
