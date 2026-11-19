package ru.nilsson03.library.quest.core.config;

import java.util.List;

import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import ru.nilsson03.library.quest.QuestLibrary;

public class Config {

    private static FileConfiguration config;

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

    public static List<String> getDisabledWorlds() {
        return getConfig().getStringList("settings.disabled_worlds");
    }

    public static boolean isWorldEnabled(World world) {
        if (world == null) {
            return false;
        }
        List<String> disabledWorlds = getDisabledWorlds();
        return !disabledWorlds.contains(world.getName());
    }

    public static FileConfiguration getConfig() {
        if (config == null) {
            config = QuestLibrary.getApi().getConfiguration();
        }
        return config;
    }
}
