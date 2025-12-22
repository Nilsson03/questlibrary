package ru.nilsson03.library.quest.objective.progress;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.nilsson03.library.collection.Pair;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

public interface QuestProgress {

    default void incrementProgress(Goal goal, long amount, boolean checkPlayerEffects, Runnable runnable) {
        incrementProgress(goal, amount, checkPlayerEffects);
        runnable.run();
    }

    default void incrementProgress(Goal goal, long amount, Runnable runnable) {
        incrementProgress(goal, amount, true);
        runnable.run();
    }

    default boolean canIncrementProgress(Goal goal, Objective objective) {
        return objective.contains(goal) && !isCompleted();
    }

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
    default void incrementProgress(Goal goal, long amount, boolean checkPlayerEffects) {
        Player player = Bukkit.getPlayer(userUuid());

        Preconditions.checkArgument(player != null, "Player not found");

        if (!conditionsIsAchieve())
            return;

        setProgress(goal, amount, checkPlayerEffects);
    }

    default boolean conditionsIsAchieve() {
        Set<QuestCondition> conditions = quest().conditions();
        for (QuestCondition questCondition : conditions) {
            if (!questCondition.isMet(getUser())) {
                return false;
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
     */
    void setProgress(Goal goal, long progress, boolean checkPlayerEffects);

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
    Quest quest();

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
