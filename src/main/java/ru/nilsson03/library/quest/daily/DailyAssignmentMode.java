package ru.nilsson03.library.quest.daily;

import java.util.Locale;

public enum DailyAssignmentMode {
    SHARED,
    PERSONAL;

    public static DailyAssignmentMode fromString(String value) {
        if (value == null || value.isBlank()) {
            return SHARED;
        }
        try {
            return DailyAssignmentMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return SHARED;
        }
    }
}
