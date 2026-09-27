package ru.nilsson03.library.quest.core.service;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.bukkit.Bukkit;

import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition.ConditionType;
import ru.nilsson03.library.quest.core.event.UserCompleteQuestEvent;
import ru.nilsson03.library.quest.core.event.UserQuestStartEvent;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.registry.QuestCompleterRegistry;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.util.QuestLogContext;

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

        boolean unmetPrerequisite;
        try {
            unmetPrerequisite = quest.conditions()
                    .stream()
                    .filter(condition -> condition.getType() == ConditionType.START)
                    .allMatch(condition -> condition.isMet(ConditionContext.of(user)));
        } catch (RuntimeException exception) {
            logQuestError("Не удалось проверить условия запуска", user, quest, exception);
            throw exception;
        }

        if (!unmetPrerequisite) {
            return;
        }

        UserQuestStartEvent event = new UserQuestStartEvent(plugin, user, quest);
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (RuntimeException exception) {
            logQuestError("Ошибка обработчика события запуска квеста", user, quest, exception);
            throw exception;
        }
        if (event.isCancelled()) {
            return;
        }

        try {
            Set<QuestProgress> objectiveProgressSet = questProgressService.createEmptyProgressForQuest(user, quest);
            user.addNewProgressFromSet(objectiveProgressSet);

            if (questUserDataConsumer != null) {
                questUserDataConsumer.accept(user);
            }
        } catch (RuntimeException exception) {
            logQuestError("Не удалось создать прогресс квеста", user, quest, exception);
            throw exception;
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

        List<QuestProgress> allProgress;
        try {
            allProgress = user.getAllProgressForQuest(quest);
        } catch (RuntimeException exception) {
            logQuestError("Не удалось получить прогресс квеста", user, quest, exception);
            throw exception;
        }

        boolean allObjectivesCompleted = allProgress.stream()
                .allMatch(QuestProgress::isCompleted);

        if (!allObjectivesCompleted) {
            return CompleteStatus.GOAL_NOT_ACHIEVE;
        }

        UserCompleteQuestEvent event = new UserCompleteQuestEvent(plugin, user, quest, CompleteStatus.SUCCESS);
        try {
            Bukkit.getPluginManager().callEvent(event);
        } catch (RuntimeException exception) {
            logQuestError("Ошибка обработчика события завершения квеста", user, quest, exception);
            throw exception;
        }

        if (event.isCancelled()) {
            return CompleteStatus.CANCELLED;
        }

        QuestCompleter completer = questCompleterRegistry.getCompleter(quest);
        if (completer == null) {
            completer = questCompleterRegistry.getDefaultCompleter();
        }

        user.addCompletedQuest(quest);
        user.removeQuestProgress(quest);

        try {
            completer.completeQuest(user, quest, questUserDataConsumer);
        } catch (RuntimeException exception) {
            logQuestError("Ошибка completer-а или выдачи награды", user, quest, exception);
            throw exception;
        }

        return CompleteStatus.SUCCESS;
    }

    public CompleteStatus completeQuest(QuestUserData user, BaseQuest quest) {
        return completeQuest(user, quest, null);
    }

    private void logQuestError(String message, QuestUserData user, BaseQuest quest, RuntimeException exception) {
        ConsoleLogger.error(plugin, "%s: quest=%s, player=%s, error=%s",
                message,
                quest != null && quest.questUniqueKey() != null ? quest.questUniqueKey().getKey() : "null",
                user != null ? QuestLogContext.player(user.uuid()) : "null",
                exception.getMessage());
    }
}
