package ru.nilsson03.library.quest.tracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.nilsson03.library.quest.core.event.UserCompleteQuestEvent;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;

public class PrerequisiteQuestTracker implements Listener {
    
    private final ObjectiveRegistry objectiveRegistry;
    
    public PrerequisiteQuestTracker(ObjectiveRegistry objectiveRegistry) {
        this.objectiveRegistry = objectiveRegistry;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestComplete(UserCompleteQuestEvent event) {
        try {
            if (event.getStatus() != CompleteStatus.SUCCESS) {
                return;
            }

            QuestUserData questUserData = event.getQuestUserData();
            if (questUserData == null) {
                return;
            }

            String completedQuestId = event.getQuest().questUniqueKey().getKey();

            if (!questUserData.hasActiveQuestWithCurrentObjectiveType(
                    objectiveRegistry.getObjectiveType("PREREQUISITE_QUEST"))) {
                return;
            }

            questUserData.incrementProgressQuestsWithObjectiveType(
                objectiveRegistry.getObjectiveType("PREREQUISITE_QUEST"),
                completedQuestId,
                1L
            );
        } catch (RuntimeException exception) {
            ConsoleLogger.error(event.getPlugin(),
                    "Ошибка обработки PREREQUISITE_QUEST: completedQuest=%s, player=%s, error=%s",
                    event.getQuest() != null && event.getQuest().questUniqueKey() != null
                            ? event.getQuest().questUniqueKey().getKey() : "null",
                    event.getQuestUserData() != null ? event.getQuestUserData().uuid() : "null",
                    exception.getMessage());
            throw exception;
        }
    }
}
