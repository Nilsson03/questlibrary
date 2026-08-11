package ru.nilsson03.library.quest.objective.progress;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import ru.nilsson03.library.collection.Pair;
import ru.nilsson03.library.quest.condition.ConditionContext;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public interface QuestProgress {

    default void incrementProgress(Goal goal, long amount, Runnable runnable) {
        incrementProgress(goal, amount);
        runnable.run();
    }

    boolean canIncrementProgress(Goal goal, Objective objective);

    default Pair<Long, Long> calculateIncrProgressAndReturnRemain(Objective objective, Goal goal, long progress) {
        long requiredProgress = objective.getRequiredProgress(goal);
        long currentProgress = getValue(goal);

        long incrProgress = 0;
        long remains = 0;
        if (currentProgress + progress > requiredProgress) {
            remains = currentProgress + progress - requiredProgress;
            incrProgress = requiredProgress - currentProgress;
        }
        return Pair.of(incrProgress, remains);
    }

    /**
     * Увеличивает прогресс по указанной цели на заданное количество.
     *
     * @param goal   Цель, для которой увеличивается прогресс.
     * @param amount Количество, на которое увеличивается прогресс.
     */
    default void incrementProgress(Goal goal, long amount) {
        incrementProgress(goal, amount, (Player) null);
    }

    /**
     * Increments progress using the acting player for conditions and potion checks.
     * Owner need not be an online player (group/guild owners).
     */
    default void incrementProgress(Goal goal, long amount, Player actor) {
        if (!progressConditionsIsAchieve(actor)) {
            return;
        }

        long currentProgress = getValue(goal);
        setProgress(goal, currentProgress + amount, true, actor);
    }

    default boolean conditionsIsAchieve() {
        return conditionsIsAchieve(null);
    }

    default boolean conditionsIsAchieve(Player actor) {
        ConditionContext context = ConditionContext.of(getUser(), actor);
        Set<QuestCondition> conditions = quest().conditions();
        for (QuestCondition questCondition : conditions) {
            if (!questCondition.isMet(context)) {
                return false;
            }
        }
        return true;
    }

    default boolean progressConditionsIsAchieve() {
        return progressConditionsIsAchieve(null);
    }

    default boolean progressConditionsIsAchieve(Player actor) {
        ConditionContext context = ConditionContext.of(getUser(), actor);
        Set<QuestCondition> conditions = quest().conditions();
        for (QuestCondition questCondition : conditions) {
            if (questCondition.getType() == QuestCondition.ConditionType.PROGRESS) {
                if (!questCondition.isMet(context)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Возвращает текущее значение прогресса по указанной цели.
     *
     * @param object Цель, для которой запрашивается прогресс.
     * @return Текущее значение прогресса.
     */
    long getValue(Goal object);

    /**
     * Устанавливает прогресс по указанной цели.
     *
     * @param goal     Цель, для которой устанавливается прогресс.
     * @param progress Значение прогресса.
     * @param checkPlayerEffects Проверять ли эффекты игрока.
     */
    default void setProgress(Goal goal, long progress, boolean checkPlayerEffects) {
        setProgress(goal, progress, checkPlayerEffects, null);
    }

    /**
     * Sets progress using an optional actor for potion-effect checks and events.
     */
    void setProgress(Goal goal, long progress, boolean checkPlayerEffects, Player actor);

    /**
     * Напрямую устанавливает прогресс без проверок и событий.
     * Используется при загрузке данных из БД.
     *
     * @param goal     Цель, для которой устанавливается прогресс.
     * @param progress Значение прогресса.
     */
    void setProgressDirectly(Goal goal, long progress);

    /**
     * Проверяет, выполнена ли цель.
     *
     * @return true, если цель выполнена, иначе false.
     */
    boolean isCompleted();

    /**
     * Возвращает квест, к которому относится прогресс.
     *
     * @return Квест.
     */
    BaseQuest quest();

    /**
     * Возвращает неизменяемую карту прогресса.
     *
     * @return Неизменяемая карта прогресса.
     */
    Map<Goal, Long> getProgress();

    /**
     * Возвращает идентификатор пользователя, к которому привязан прогресс
     *
     * @return идентификатор пользователя
     */
    UUID userUuid();

    QuestUserData getUser();

    /**
     * Получение задачи, к которой относится прогресс
     * В случае с StagedQuest задача берётся для текущей стадии игрока
     *
     * @return Задача, к которой относится прогресс,
     */
    Objective objective();
}
