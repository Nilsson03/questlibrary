package ru.nilsson03.library.quest.quest.completer.registry;

import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.completer.impl.BaseQuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;

public class QuestCompleterRegistry {
    
    private final Map<Class<? extends BaseQuest>, QuestCompleter> completers = new HashMap<>();
    private final QuestCompleter defaultCompleter = new BaseQuestCompleter();

    public QuestCompleterRegistry(QuestUsersStorage questUsersStorage) {
    }

    public void registerCompleter(Class<? extends BaseQuest> questClass, QuestCompleter completer) {
        completers.put(questClass, completer);
    }

    public QuestCompleter getCompleter(BaseQuest quest) {
        Class<?> questClass = quest.getClass();
        
        while (questClass != null && BaseQuest.class.isAssignableFrom(questClass)) {
            QuestCompleter completer = completers.get(questClass);
            if (completer != null) {
                return completer;
            }
            questClass = questClass.getSuperclass();
        }
        
        return null;
    }

    public QuestCompleter getDefaultCompleter() {
        return defaultCompleter;
    }

    public void onRegisterInit() {
        registerCompleter(BaseQuest.class, new BaseQuestCompleter());
    }
}
