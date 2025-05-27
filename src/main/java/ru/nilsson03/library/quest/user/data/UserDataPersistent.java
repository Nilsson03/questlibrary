package ru.nilsson03.library.quest.user.data;

import java.util.UUID;

public interface UserDataPersistent {

    void saveUserData(QuestUserData userData);

    QuestUserData loadUserData(UUID uuid);

    void deleteUserData(UUID uuid);
}
