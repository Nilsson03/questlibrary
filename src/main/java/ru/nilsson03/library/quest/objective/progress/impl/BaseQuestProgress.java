package ru.nilsson03.library.quest.objective.progress.impl;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.core.event.UserQuestProgressEvent;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class BaseQuestProgress implements QuestProgress {

    private final QuestUserData user;
    private final Quest quest;
    private final Objective objective;
    private Map<Goal, Long> progress;

    /**
     * Конструктор для создания объекта ObjectiveProgress.
     *
     * @param user      пользователь.
     * @param quest     Квест, к которому относится прогресс.
     * @param objective Цель, к которой относится прогресс.
     */
    public BaseQuestProgress(QuestUserData user, Quest quest, Objective objective) {
        this(user, quest, objective, objective.goals()
                .stream()
                .collect(Collectors.toMap(Function.identity(), goal -> 0L)));
    }

    /**
     * Конструктор для создания объекта ObjectiveProgress с начальным прогрессом.
     *
     * @param user      пользователь.
     * @param quest     Квест, к которому относится прогресс.
     * @param objective Цель, к которой относится прогресс.
     * @param progress  Начальный прогресс.
     */
    public BaseQuestProgress(QuestUserData user, Quest quest, Objective objective, Map<Goal, Long> progress) {
        this.user = Objects.requireNonNull(user, "userUuid cannot be null");
        this.quest = Objects.requireNonNull(quest, "Quest cannot be null");
        this.objective = Objects.requireNonNull(objective, "Objective cannot be null");
        this.progress = new HashMap<>(Objects.requireNonNull(progress, "Progress map cannot be null"));
    }

    /**
     * {@inheritDoc}
     */
    public long getValue(Goal goal) {
        return this.progress.getOrDefault(goal, 0L);
    }

    /**
     * {@inheritDoc}
     */
    public void setProgress(Goal goal, long progress, boolean checkPlayerEffects) {
        Objects.requireNonNull(goal, "Goal cannot be null");

        Player player = Bukkit.getPlayer(user.uuid());

        Preconditions.checkArgument(player != null, "Player not found");

        if (canIncrementProgress(goal, objective)) {

            if (checkPlayerEffects && !getObjective().hasAllPotionEffects(player)) {
                return;
            }

            long requiredProgress = objective.getRequiredProgress(goal);
            long currentProgress = getValue(goal);

            long newProgress = currentProgress + progress;
            if (newProgress > requiredProgress) {
                newProgress = requiredProgress;
            }

            UserQuestProgressEvent event = new UserQuestProgressEvent(user, quest, objective, goal, currentProgress, newProgress);
            Bukkit.getPluginManager()
                  .callEvent(event);
            if (event.isCancelled()) {
                return;
            }

            try {
                this.progress.put(goal, event.getNewValue());
            } catch (UnsupportedOperationException exception) {
                Map<Goal, Long> newProgressMap = new HashMap<>(this.progress);
                newProgressMap.put(goal, event.getNewValue());
                this.progress = newProgressMap;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean isCompleted() {

        List<Goal> goals = objective.goals();

        return goals.stream()
                .allMatch(goal -> progress.getOrDefault(goal, 0L) >= goal.targetValue());
    }

    /**
     * Возвращает сумму значений прогресса.
     *
     * @return Сумма значений прогресса.
     */
    public long sumProgressValues() {
        return this.progress.values()
                .stream()
                .mapToLong(Long::longValue)
                .sum();
    }

    /**
     * Возвращает цель, к которой относится прогресс.
     *
     * @return Цель.
     */
    public Objective getObjective() {
        return this.objective;
    }

    /**
     * {@inheritDoc}
     */
    public Quest quest() {
        return this.quest;
    }

    /**
     * {@inheritDoc}
     */
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

    @Override
    public Objective objective() {
        return this.objective;
    }

    @Override
    public QuestUserData getUser() {
        return user;
    }
}
