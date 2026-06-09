package ru.nilsson03.library.quest.tracker;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.nilsson03.library.quest.core.event.UserCompleteQuestEvent;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.quest.completer.CompleteStatus;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class PrerequisiteQuestTracker implements Listener {
    
    private final ObjectiveRegistry objectiveRegistry;
    
    public PrerequisiteQuestTracker(ObjectiveRegistry objectiveRegistry) {
        this.objectiveRegistry = objectiveRegistry;
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onQuestComplete(UserCompleteQuestEvent event) {
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
    }
}
