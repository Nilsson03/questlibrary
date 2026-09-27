package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

import java.util.Objects;

public class SmithingGoal implements ObjectiveGoal {
    private final ItemStack input;
    private final ItemStack result;
    private final long targetValue;

    public SmithingGoal(ItemStack input, ItemStack result, long targetValue) {
        this.input = Objects.requireNonNull(input, "Smithing input cannot be null");
        this.result = Objects.requireNonNull(result, "Smithing result cannot be null");
        this.targetValue = targetValue;
    }

    @Override
    public boolean matches(Object target) {
        if (!(target instanceof SmithingResult smithingResult)) return false;
        return sameItem(input, smithingResult.input()) && sameItem(result, smithingResult.result());
    }

    private static boolean sameItem(ItemStack expected, ItemStack actual) {
        return actual != null && actual.getType() == expected.getType();
    }

    public ItemStack input() { return input; }
    public ItemStack result() { return result; }
    @Override public Object targetType() { return result; }
    @Override public long targetValue() { return targetValue; }
    @Override public String toString() {
        return "Smithing(" + input.getType().name() + "->" + result.getType().name() + ")";
    }

    public record SmithingResult(ItemStack input, ItemStack result) { }
}
