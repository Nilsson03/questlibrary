package ru.nilsson03.library.quest.objective.factory.registry;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;

import java.util.HashMap;
import java.util.Map;

public class QuestProgressFactoryRegistry {
    private final Map<Class<? extends Quest>, QuestProgressFactory> factories = new HashMap<>();

    public void registerFactory(Class<? extends Quest> questClass, QuestProgressFactory factory) {
        factories.put(questClass, factory);
    }

    public QuestProgressFactory getFactory(Quest quest) {
        return factories.get(quest.getClass());
    }
}
