package ru.nilsson03.library.quest.objective.progress;

import ru.nilsson03.library.quest.objective.goal.Goal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.ToLongFunction;

/** Pure objective rules. Applications own storage, concurrency, and event delivery. */
public final class GoalProgress {
    private GoalProgress() {}

    public static boolean isComplete(Collection<? extends Goal> goals, ToLongFunction<Goal> current) {
        return goals.stream().allMatch(goal -> current.applyAsLong(goal) >= goal.targetValue());
    }

    public static <K> boolean isComplete(Map<K, ? extends Goal> goals, ToLongFunction<K> current) {
        return goals.entrySet().stream().allMatch(entry ->
                current.applyAsLong(entry.getKey()) >= entry.getValue().targetValue());
    }

    public static long addSaturated(long current, long amount) {
        if (amount > 0 && current > Long.MAX_VALUE - amount) return Long.MAX_VALUE;
        if (amount < 0 && current < Long.MIN_VALUE - amount) return Long.MIN_VALUE;
        return current + amount;
    }

    /** The caller supplies an immutable state, which is returned unchanged on a duplicate or a full set. */
    public static <T> Set<T> collectDistinct(Set<T> current, T target, int limit) {
        if (current.size() >= limit || current.contains(target)) return current;
        var updated = new HashSet<>(current);
        updated.add(target);
        return Set.copyOf(updated);
    }
}
