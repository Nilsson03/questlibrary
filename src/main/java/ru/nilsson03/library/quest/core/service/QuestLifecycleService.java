package ru.nilsson03.library.quest.core.service;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.condition.QuestCondition.ConditionType;
import ru.nilsson03.library.quest.core.event.UserCompleteQuestEvent;
import ru.nilsson03.library.quest.core.event.UserQuestStartEvent;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class QuestLifecycleService {

    private final NPlugin plugin;
    private final QuestProgressService questProgressService;
    private final QuestCompleterRegistry questCompleterRegistry;

    public QuestLifecycleService(NPlugin plugin, QuestProgressService questProgressService,
            QuestCompleterRegistry questCompleterRegistry) {
        this.plugin = plugin;
        this.questProgressService = questProgressService;
        this.questCompleterRegistry = questCompleterRegistry;
    }

    public void startQuest(QuestUserData user, BaseQuest quest, Consumer<QuestUserData> questUserDataConsumer) {
        if (user.questIsComplete(quest)) {
            return;
        }

        if (user.questIsStarted(quest)) {
            return;
        }

        boolean unmetPrerequisite = quest.conditions()
                .stream()
                .filter(condition -> condition.getType() == ConditionType.START)
                .allMatch(condition -> condition.isMet(user));

        if (!unmetPrerequisite) {
            return;
        }

        UserQuestStartEvent event = new UserQuestStartEvent(plugin, user, quest);
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

    public void startQuest(QuestUserData user, BaseQuest quest) {
        this.startQuest(user, quest, null);
    }

    public CompleteStatus completeQuest(QuestUserData user, BaseQuest quest,
            Consumer<QuestUserData> questUserDataConsumer) {
        if (user.questIsComplete(quest)) {
            return CompleteStatus.ALREADY_COMPLETE;
        }

        List<QuestProgress> allProgress = user.getAllProgressForQuest(quest);

        boolean allObjectivesCompleted = allProgress.stream()
                .allMatch(QuestProgress::isCompleted);

        if (!allObjectivesCompleted) {
            return CompleteStatus.GOAL_NOT_ACHIEVE;
        }

        UserCompleteQuestEvent event = new UserCompleteQuestEvent(plugin, user, quest, CompleteStatus.SUCCESS);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return CompleteStatus.CANCELLED;
        }

        QuestCompleter completer = questCompleterRegistry.getCompleter(quest);
        if (completer == null) {
            completer = questCompleterRegistry.getDefaultCompleter();
        }

        user.addCompletedQuest(quest);
        user.removeQuestProgress(quest);

        completer.completeQuest(user, quest, questUserDataConsumer);

        return CompleteStatus.SUCCESS;
    }

    public CompleteStatus completeQuest(QuestUserData user, BaseQuest quest) {
        return completeQuest(user, quest, null);
    }
}
