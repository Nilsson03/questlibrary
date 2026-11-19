package ru.nilsson03.library.quest.objective.progress;

import org.junit.jupiter.api.Test;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class GoalProgressTest {
    @Test void bothCompletionEntrypointsRequireEveryGoal() {
        var first = NumericGoal.fromParameters(Map.of("value", "7"));
        var second = NumericGoal.fromParameters(Map.of("value", 5));
        var named = Map.of("first", first, "second", second);
        assertFalse(GoalProgress.isComplete(named, key -> key.equals("first") ? 7 : 4));
        assertTrue(GoalProgress.isComplete(named, key -> 7));
        assertTrue(GoalProgress.isComplete(List.of(first, second), goal -> goal.targetValue()));
        assertFalse(GoalProgress.isComplete(List.of(first, second), goal -> goal.targetValue() - 1));
    }

    @Test void numericUpdatesSaturateAndUniqueObservationsRespectTheLimit() {
        assertEquals(Long.MAX_VALUE, GoalProgress.addSaturated(Long.MAX_VALUE - 1, 2));
        assertEquals(Long.MIN_VALUE, GoalProgress.addSaturated(Long.MIN_VALUE + 1, -2));
        assertEquals(8, GoalProgress.addSaturated(5, 3));
        Set<String> current = Set.of("a");
        assertSame(current, GoalProgress.collectDistinct(current, "a", 2));
        assertSame(current, GoalProgress.collectDistinct(current, "b", 1));
        assertEquals(Set.of("a", "b"), GoalProgress.collectDistinct(current, "b", 2));
    }
}
