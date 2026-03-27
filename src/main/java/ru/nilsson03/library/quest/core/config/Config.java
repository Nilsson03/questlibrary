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

    public static String progressFormatter_formatPlayTimeGoal() {
        return getConfig().getString("settings.progress_formatter_goals_playtime");
    }

    public static String progressFormatter_formatSurvivalGoal() {
        return getConfig().getString("settings.progress_formatter_goals_survival");
    }

    public static String progressFormatter_formatBlockShieldGoal() {
        return getConfig().getString("settings.progress_formatter_goals_block_shield");
    }

    public static String progressFormatter_formatCureVillagerGoal() {
        return getConfig().getString("settings.progress_formatter_goals_cure_villager");
    }

    public static String progressFormatter_formatUseTotemGoal() {
        return getConfig().getString("settings.progress_formatter_goals_use_totem");
    }

    public static String progressFormatter_formatShearSheepGoal() {
        return getConfig().getString("settings.progress_formatter_goals_shear_sheep");
    }
    public static String progressFormatter_formatTntBlockBreaksGoal() {
        return getConfig().getString("settings.progress_formatter_goals_tnt_block_breaks");
    }

    public static String progressFormatter_formatCollectComposterGoal() {
        return getConfig().getString("settings.progress_formatter_goals_collect_from_composter");
    }

    public static String progressFormatter_formatFillComposterGoal() {
        return getConfig().getString("settings.progress_formatter_goals_fill_composter");
    }

    public static String progressFormatter_formatResurrectDragonGoal() {
        return getConfig().getString("settings.progress_formatter_goals_resurrect_dragon");
    }

    public static String progressFormatter_formatIgniteTntGoal() {
        return getConfig().getString("settings.progress_formatter_goals_ignite_tnt");
    }

    public static String progressFormatter_formatUseGrindstoneItemGoal() {
        return getConfig().getString("settings.progress_formatter_goals_use_grindstone_item");
    }

    public static String progressFormatter_formatDeathItemGoal() {
        return getConfig().getString("settings.progress_formatter_goals_death");
    }

    public static String progressFormatter_formatMoveGoal() {
        return getConfig().getString("settings.progress_formatter_goals_move");
    }

    public static String progressFormatter_formatExpChangeGoal() {
        return getConfig().getString("settings.progress_formatter_goals_exp_change");
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
