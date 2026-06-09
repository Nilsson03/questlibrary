package ru.nilsson03.library.quest.core;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

@Getter
@RequiredArgsConstructor
public class QuestSystemContext {

    private final QuestService questService;
    private final QuestStorage questStorage;
    private final QuestUsersStorage questUsersStorage;
    private final QuestManager questManager;
    private final UserDataPersistent dataPersistent;

    public ObjectiveRegistry getObjectiveRegistry() {
        return questService.getObjectiveRegistry();
    }

    public void start() {
        questManager.registerEventHandlers();
        questStorage.loadQuests();
    }
}
