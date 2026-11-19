package ru.nilsson03.library.quest.daily.config;

import java.util.Objects;

public final class RarityDefinition {

    private final int weight;
    private final String displayName;

    public RarityDefinition(int weight, String displayName) {
        this.weight = Math.max(0, weight);
        this.displayName = Objects.requireNonNull(displayName, "displayName");
    }

    public int weight() {
        return weight;
    }

    public String displayName() {
        return displayName;
    }
}
