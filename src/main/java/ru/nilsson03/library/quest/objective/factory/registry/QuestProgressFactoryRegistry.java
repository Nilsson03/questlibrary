package ru.nilsson03.library.quest.objective.factory.registry;

import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

import java.util.HashMap;
import java.util.Map;

public class QuestProgressFactoryRegistry {
    private final Map<Class<? extends BaseQuest>, QuestProgressFactory> factories = new HashMap<>();

    public void registerFactory(Class<? extends BaseQuest> questClass, QuestProgressFactory factory) {
        factories.put(questClass, factory);
    }

    public QuestProgressFactory getFactory(BaseQuest quest) {
        return factories.get(quest.getClass());
    }
}
