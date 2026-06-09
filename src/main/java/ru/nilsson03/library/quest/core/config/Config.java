package ru.nilsson03.library.quest.core.config;

import ru.nilsson03.library.bukkit.file.configuration.BukkitConfig;
import ru.nilsson03.library.quest.QuestLibrary;

public class Config {

    private static BukkitConfig config;

    public static String messages_QuestProgress() {
        return getConfig().getString("settings.quest_progress");
    }

    public static String progressFormatter_CompactProgressFormat() {
        return getConfig().getString("settings.progress_formatter_compact_progress_format");
    }

    public static String progressFormatter_CompactCompleteFormat() {
        return getConfig().getString("settings.progress_formatter_compact_complete_format");
    }


    public static String progressFormatter_ProgressFormat() {
        return getConfig().getString("settings.progress_formatter_progress_format");
    }

    public static String progressFormatter_CompleteFormat() {
        return getConfig().getString("settings.progress_formatter_complete_format");
    }

    public static String progressFormatter_formatUndefinedGoal() {
        return getConfig().getString("settings.progress_formatter_goals_undefined");
    }

    public static BukkitConfig getConfig() {
        if (config == null) {
            config = QuestLibrary.getApi().getConfiguration();
        }
        return config;
    }
}
