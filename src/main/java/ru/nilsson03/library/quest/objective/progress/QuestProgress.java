package ru.nilsson03.library.quest.objective.progress;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.nilsson03.library.collection.Pair;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
        Player player = Bukkit.getPlayer(userUuid());

        Preconditions.checkArgument(player != null, "Player not found");

        boolean conditionsAchieved = progressConditionsIsAchieve();
        
        if (!conditionsAchieved) {
            return;
        }

        long currentProgress = getValue(goal);
        setProgress(goal, currentProgress + amount, true);
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
    
    default boolean progressConditionsIsAchieve() {
        Set<QuestCondition> conditions = quest().conditions();
        for (QuestCondition questCondition : conditions) {
            if (questCondition.getType() == QuestCondition.ConditionType.PROGRESS) {
                if (!questCondition.isMet(getUser())) {
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
    void setProgress(Goal goal, long progress, boolean checkPlayerEffects);
    
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
