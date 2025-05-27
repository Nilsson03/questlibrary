package ru.nilsson03.library.quest.objective.progress.impl;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import ru.nilsson03.library.collection.Pair;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.quest.staged.event.QuestStagesComplete;
import ru.nilsson03.library.quest.stage.QuestStage;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class StagedQuestProgress implements QuestProgress {

    private final QuestUserData user;
    private final StagedQuest quest;
    private final Map<Goal, Long> progress;
    private QuestStage currentStage;
    private final List<QuestStage> stages;

    private boolean complete;

    /**
     * Конструктор для создания объекта ObjectiveProgress.
     *
     * @param user  пользователь
     * @param quest Квест, к которому относится прогресс.
     */
    public StagedQuestProgress(QuestUserData user, StagedQuest quest) {
        this.user = Objects.requireNonNull(user, "userUuid cannot be null");
        ;
        this.quest = Objects.requireNonNull(quest, "quest cannot be null");
        ;
        this.stages = quest.stages();
        this.currentStage = getMinStage();
        this.progress = currentStage.objective()
                                    .goals()
                                    .stream()
                                    .collect(Collectors.toMap(Function.identity(), goal -> 0L));
        complete = false;
    }

    public StagedQuestProgress(
            QuestUserData user, StagedQuest quest, Map<Goal, Long> progress,
            QuestStage currentStage) {
        this.user = Objects.requireNonNull(user, "userUuid cannot be null");
        ;
        this.quest = Objects.requireNonNull(quest, "quest cannot be null");
        ;
        this.stages = quest.stages();
        this.currentStage = Objects.requireNonNull(currentStage, "currentStage cannot be null");
        this.progress = Objects.requireNonNull(progress, "progress cannot be null");
        complete = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setProgress(Goal goal, long progress, boolean checkPlayerEffects) {
        Objects.requireNonNull(goal, "Goal cannot be null");

        Player player = Objects.requireNonNull(Bukkit.getOfflinePlayer(user.uuid())
                                                     .getPlayer(), "Player not found");

        Objective objective = currentStage.objective();

        if (canIncrementProgress(goal, objective)) {

            if (checkPlayerEffects && !objective.hasAllPotionEffects(player)) {
                return;
            }

            Pair<Long, Long> progressPair = calculateIncrProgressAndReturnRemain(objective, goal, progress);
            long remains = progressPair.getRight();
            long incrProgress = progressPair.getLeft();

            this.progress.merge(goal, incrProgress, Long::sum);

            if (isStageCompleted(currentStage)) {
                if (!isLastStage()) {
                    QuestStage nextStage = getNextStage(currentStage);
                    clearProgressAndPutLastProgress(nextStage, remains);
                }
                goToNextStage();
            }
        }
    }

    private void clearProgressAndPutLastProgress(QuestStage nextStage, long remains) {
        List<Goal> previousStageGoals = currentStage.objective()
                                                    .goals();
        clearProgressAndPutNewObjectives(nextStage);
    }

    private void clearProgressAndPutNewObjectives(QuestStage nextStage) {
        this.progress.clear();
        this.progress.putAll(nextStage.objective()
                                      .goals()
                                      .stream()
                                      .collect(Collectors.toMap(Function.identity(), goal -> 0L)));
    }

    private boolean stageHasObjective(QuestStage questStage, Goal goal) {
        if (goal instanceof ObjectiveGoal objectiveGoal) {
            return questStage.objective()
                             .contains(objectiveGoal.targetType());
        } else {
            return true;
        }
    }

    private boolean isLastStage() {
        QuestStage lastStage = getLastStage();
        return currentStage.compareTo(lastStage) >= 0;
    }

    public QuestStage currentStage() {
        return currentStage;
    }

    private QuestStage getLastStage() {
        return stages.stream()
                     .max(Comparator.comparing(QuestStage::weight))
                     .orElseGet(() -> currentStage);
    }

    /**
     * {@inheritDoc}
     */
    public long getValue(Goal goal) {
        return this.progress.getOrDefault(goal, 0L);
    }

    /**
     * Получение минимальной по весу стадии, используется для определения первой
     *
     * @return начальная стадия квеста
     */
    private QuestStage getMinStage() {
        Optional<QuestStage> optionalQuestStage = stages.stream()
                                                        .min(Comparator.comparing(QuestStage::weight));

        return optionalQuestStage.orElseThrow();
    }

    /**
     * Проверяет, выполнена ли текущая стадия.
     */
    private boolean isStageCompleted(QuestStage stage) {
        Objective objective = stage.objective();
        for (Map.Entry<Goal, Long> entry : progress.entrySet()) {
            Goal goal = entry.getKey();
            long currentProgress = entry.getValue();
            long requiredProgress = objective.getRequiredProgress(goal);

            if (currentProgress < requiredProgress) {
                return false;
            }
        }
        return true;
    }

    /**
     * Переходит на следующую стадию или завершает квест, если это последняя стадия.
     */
    private void goToNextStage() {
        QuestStage nextStage = getNextStage(currentStage);

        if (isLastStage()) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(user.uuid());
            Preconditions.checkArgument(offlinePlayer.hasPlayedBefore() && offlinePlayer.isOnline(),
                                        "Player not found");
            QuestStagesComplete questStagesComplete = new QuestStagesComplete(quest, offlinePlayer.getPlayer());
            Bukkit.getPluginManager()
                  .callEvent(questStagesComplete);
            this.complete = true;
        } else {
            this.currentStage = nextStage;
        }
    }

    /**
     * Возвращает следующую стадию квеста.
     */
    private QuestStage getNextStage(QuestStage currentStage) {
        List<QuestStage> stages = this.stages;
        int currentIndex = stages.indexOf(currentStage);

        if (currentIndex >= 0 && currentIndex < stages.size() - 1) {
            return stages.get(currentIndex + 1);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCompleted() {
        return complete;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Quest quest() {
        return quest;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<Goal, Long> getProgress() {
        return Collections.unmodifiableMap(this.progress);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID userUuid() {
        return user.uuid();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Objective objective() {
        return currentStage.objective();
    }
}
