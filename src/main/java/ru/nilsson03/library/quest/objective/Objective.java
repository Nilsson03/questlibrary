package ru.nilsson03.library.quest.objective;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import com.google.common.base.Preconditions;

import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;

public class Objective {

    private final String key;
    private final List<Goal> goals = new ArrayList<>();
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final ObjectiveType objectiveType;

    /**
     * Конструктор для создания объекта Objective.
     *
     * @param objectiveType Тип цели.
     */
    public Objective(String key, ObjectiveType objectiveType, List<PotionEffect> potionEffects, List<Goal> goals) {
        this.key = Preconditions.checkNotNull(key, "Key cannot be null");
        this.objectiveType = Objects.requireNonNull(objectiveType, "ObjectiveType cannot be null");
        this.potionEffects.addAll(Objects.requireNonNull(potionEffects, "PotionEffects cannot be null"));
        this.goals.addAll(Objects.requireNonNull(goals, "Goals cannot be null"));
    }

    /**
     * Проверяет, содержит ли цель указанный объект.
     *
     * @param object Объект для проверки.
     * @return true, если объект содержится в целях, иначе false.
     */
    public boolean contains(Object object) {
        return this.goals.stream()
                         .anyMatch(goal -> goal.matches(object));
    }

    public Optional<Goal> getGoal(Object object) {
        return this.goals.stream()
                         .filter(goal -> goal.matches(object))
                         .findFirst();
    }

    /**
     * Проверяет, есть ли у игрока все эффекты зелий из списка.
     *
     * @param player Игрок для проверки.
     * @return true, если у игрока есть все эффекты, или список эффектов пуст, иначе false.
     */
    public boolean hasAllPotionEffects(Player player) {
        Objects.requireNonNull(player, "Player cannot be null");
        return this.potionEffects.isEmpty() || this.potionEffects.stream()
                                                                 .allMatch(potionEffect -> player.hasPotionEffect(
                                                                         potionEffect.getType()));
    }

    public long getRequiredProgress(Object valueType) {
        Objects.requireNonNull(valueType, "Goal cannot be null");

        return goals.stream()
                    .filter(goal -> goal.matches(valueType))
                    .mapToLong(Goal::targetValue)
                    .sum();
    }

    public Set<Goal> filterGoalsByType(Class<? extends Goal> filteredGoal) {
        return this.goals.stream()
                         .filter(filteredGoal::isInstance)
                         .collect(Collectors.toSet());
    }

    /**
     * Возвращает неизменяемую карту целей.
     *
     * @return Неизменяемая карта целей.
     */
    public List<Goal> goals() {
        return Collections.unmodifiableList(this.goals);
    }

    /**
     * Возвращает тип цели.
     *
     * @return Тип цели.
     */
    public ObjectiveType type() {
        return this.objectiveType;
    }

    /**
     * Возвращает сумму значений всех целей.
     *
     * @return Сумма значений целей.
     */
    public long sumGoalsValues() {
        return this.goals.stream()
                         .mapToLong(Goal::targetValue)
                         .sum();
    }

    /**
     * Возвращает неизменяемый список эффектов зелий.
     *
     * @return Неизменяемый список эффектов зелий.
     */
    public List<PotionEffect> getPotionEffects() {
        return Collections.unmodifiableList(this.potionEffects);
    }

    public String key() {
        return key;
    }
}
