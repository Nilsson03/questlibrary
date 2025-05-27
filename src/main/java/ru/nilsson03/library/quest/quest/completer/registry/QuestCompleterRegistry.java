package ru.nilsson03.library.quest.quest.completer.registry;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.impl.BaseQuestCompleter;
import ru.nilsson03.library.quest.quest.completer.impl.StagedQuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.quest.staged.StagedQuest;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;

public class QuestCompleterRegistry {

    private static final Map<Class<? extends Quest>, QuestCompleter> completers = new HashMap<>();

    private final QuestUsersStorage questUsersStorage;

    public QuestCompleterRegistry(QuestUsersStorage questUsersStorage) {
        this.questUsersStorage = questUsersStorage;
    }

    public void registerCompleter(Class<? extends Quest> questClass, QuestCompleter completer) {
        completers.put(questClass, completer);
    }

    public void onRegisterInit() {
        registerCompleter(BaseQuest.class, new BaseQuestCompleter(questUsersStorage));
        registerCompleter(StagedQuest.class, new StagedQuestCompleter(questUsersStorage));
    }

    public QuestCompleter getCompleter(Quest quest) {
        return completers.get(quest.getClass());
    }
}
