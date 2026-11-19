package ru.nilsson03.library.quest.util;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.plugin.Plugin;

public final class QuestLog {
    private QuestLog() {}

    public static void debug(String name, String message, Object... args) {
        log(Logger.getLogger(name), Level.FINE, message, args);
    }

    public static void debug(Plugin plugin, String message, Object... args) {
        log(plugin.getLogger(), Level.FINE, message, args);
    }

    public static void info(String name, String message, Object... args) {
        log(Logger.getLogger(name), Level.INFO, message, args);
    }

    public static void info(Plugin plugin, String message, Object... args) {
        log(plugin.getLogger(), Level.INFO, message, args);
    }

    public static void warn(String name, String message, Object... args) {
        log(Logger.getLogger(name), Level.WARNING, message, args);
    }

    public static void warn(Plugin plugin, String message, Object... args) {
        log(plugin.getLogger(), Level.WARNING, message, args);
    }

    public static void error(String name, String message, Object... args) {
        log(Logger.getLogger(name), Level.SEVERE, message, args);
    }

    public static void error(Plugin plugin, String message, Object... args) {
        log(plugin.getLogger(), Level.SEVERE, message, args);
    }

    private static void log(Logger logger, Level level, String message, Object[] args) {
        if (logger.isLoggable(level)) logger.log(level, args.length == 0 ? message : message.formatted(args));
    }
}
