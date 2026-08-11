package ru.nilsson03.library.quest.user.data;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserDataPersistent {

    void saveUserData(QuestUserData userData);

    QuestUserData loadUserData(UUID uuid);

    void deleteUserData(UUID uuid);
    
    default long getQuestCompletionTime(UUID uuid, String questKey) {
        return 0;
    }
    
    default CompletableFuture<Void> deleteQuestData(UUID uuid, String questKey) {
        return CompletableFuture.completedFuture(null);
    }

    default CompletableFuture<Void> deleteQuestDataByKeys(Collection<String> questKeys) {
        return CompletableFuture.completedFuture(null);
    }
}
