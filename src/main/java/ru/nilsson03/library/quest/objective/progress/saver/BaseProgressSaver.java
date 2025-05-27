package ru.nilsson03.library.quest.objective.progress.saver;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.goal.helper.GoalHelper;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.BaseQuestProgress;

public class BaseProgressSaver implements ProgressSaver {

    public void save(QuestProgress progress, ConfigurationSection section) {
        if (progress instanceof BaseQuestProgress) {
            section.set("quest_id", progress.quest().questUniqueKey().getKey());
            section.set("user_id", progress.userUuid().toString());
            section.set("objective_id", progress.objective().key());

            ConfigurationSection progressSection = section.createSection("progress");
            GoalHelper.saveGoalsFromProgress(progress.getProgress(), progressSection);
        }
    }
}
