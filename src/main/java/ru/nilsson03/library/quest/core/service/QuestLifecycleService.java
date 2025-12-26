package ru.nilsson03.library.quest.core.service;

import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.event.UserCompleteQuestEvent;
import ru.nilsson03.library.quest.core.event.UserQuestStartEvent;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class QuestLifecycleService {

    private final QuestProgressService questProgressService;
    private final QuestCompleterRegistry questCompleterRegistry;

    public QuestLifecycleService(QuestProgressService questProgressService, QuestCompleterRegistry questCompleterRegistry) {
        this.questProgressService = questProgressService;
        this.questCompleterRegistry = questCompleterRegistry;
    }

    public void startQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        if (user.questIsComplete(quest)) {
            return;
        }

        UserQuestStartEvent event = new UserQuestStartEvent(user, quest);
        Bukkit.getPluginManager()
              .callEvent(event);
        if (event.isCancelled()) {
            return;
        }

        Set<QuestProgress> objectiveProgressSet = questProgressService.createEmptyProgressForQuest(user, quest);
        user.addNewProgressFromSet(objectiveProgressSet);

        if (questUserDataConsumer != null) {
            questUserDataConsumer.accept(user);
        }
    }

    public void startQuest(QuestUserData user, Quest quest) {
        this.startQuest(user, quest, null);
    }

    public CompleteStatus completeQuest(QuestUserData user, Quest quest, Consumer<QuestUserData> questUserDataConsumer) {
        if (user.questIsComplete(quest)) {
            return CompleteStatus.ALREADY_COMPLETE;
        }

        QuestCompleter completer = questCompleterRegistry.getCompleter(quest);
        if (completer == null) {
            throw new IllegalArgumentException("No completer found for quest type: " + quest.getClass()
                                                                                            .getSimpleName());
        }

        CompleteStatus status = completer.completeQuest(user, quest, questUserDataConsumer);
        
        if (status == CompleteStatus.SUCCESS) {
            UserCompleteQuestEvent event = new UserCompleteQuestEvent(user, quest, status);
            Bukkit.getPluginManager()
                  .callEvent(event);
            
            if (event.isCancelled()) {
                return CompleteStatus.CONDITIONS_NOT_ACHIEVE;
            }
            
            user.addCompletedQuest(quest);
        }

        return status;
    }

    public CompleteStatus completeQuest(QuestUserData user, Quest quest) {
        return completeQuest(user, quest, null);
    }
}
