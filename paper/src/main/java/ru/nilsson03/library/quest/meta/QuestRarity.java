package ru.nilsson03.library.quest.meta;

import java.util.Locale;
import java.util.Objects;

public final class QuestRarity {

    private final String id;

    private QuestRarity(String id) {
        this.id = id;
    }

    public static QuestRarity of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Rarity id cannot be null or blank");
        }
        return new QuestRarity(value.trim().toUpperCase(Locale.ROOT));
    }

    public static QuestRarity fromString(String value) {
        if (value == null || value.isBlank()) {
            return of("DEFAULT");
        }
        return of(value);
    }

    public String id() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof QuestRarity that)) {
            return false;
        }
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return id;
    }
}
