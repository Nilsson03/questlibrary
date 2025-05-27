package ru.nilsson03.library.quest.objective.progress.saver;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.goal.helper.GoalHelper;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.impl.StagedQuestProgress;

public class StagedProgressSaver implements ProgressSaver {

    public void save(QuestProgress progress, ConfigurationSection section) {
        if (progress instanceof StagedQuestProgress stagedQuestProgress) {
            section.set("quest_id", progress.quest().questUniqueKey().getKey());
            section.set("user_id", progress.userUuid().toString());
            section.set("current_stage", stagedQuestProgress.currentStage().weight());

            ConfigurationSection progressSection = section.createSection("progress");
            GoalHelper.saveGoalsFromProgress(progress.getProgress(), progressSection);
        }
    }
}
