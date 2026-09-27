package ru.nilsson03.library.quest.objective.progress.saver;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;
import ru.nilsson03.library.quest.util.QuestLogContext;

public class BaseProgressSaver implements ProgressSaver {
    private final NPlugin plugin;

    public BaseProgressSaver() {
        this(null);
    }

    public BaseProgressSaver(NPlugin plugin) {
        this.plugin = plugin;
    }

    public void save(QuestProgress progress, ConfigurationSection section) {
        if (progress instanceof BaseQuestProgress) {
            section.set("quest_id", progress.quest().questUniqueKey().getKey());
            section.set("user_id", progress.userUuid().toString());
            section.set("objective_id", progress.objective().key());

            ConfigurationSection progressSection = section.createSection("progress");
            for (var entry : progress.getProgress().entrySet()) {
                try {
                    ConfigurationSection goalSection = progressSection.createSection(entry.getKey().toString());
                    goalSection.set("value", entry.getValue());
                } catch (RuntimeException exception) {
                    logSaveError(progress, entry.getKey(), exception);
                }
            }
        }
    }

    private void logSaveError(QuestProgress progress, Goal goal, RuntimeException exception) {
        String message = String.format(
                "Не удалось сохранить цель: quest=%s, objective=%s, player=%s, goal=%s: %s",
                progress.quest().questUniqueKey().getKey(), progress.objective().key(),
                QuestLogContext.player(progress.userUuid()),
                goal != null ? goal.getClass().getSimpleName() : "null", exception.getMessage());
        if (plugin != null) ConsoleLogger.error(plugin, message);
        else ConsoleLogger.error("questlibrary", message);
    }
}
