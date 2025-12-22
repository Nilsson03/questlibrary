package ru.nilsson03.library.quest.core.service;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.factory.QuestProgressFactory;
import ru.nilsson03.library.quest.objective.factory.registry.QuestProgressFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;

public class QuestProgressService {

    private final QuestProgressFactoryRegistry factoryRegistry;

    public QuestProgressService(QuestProgressFactoryRegistry factoryRegistry) {
        this.factoryRegistry = factoryRegistry;
    }

    public Set<QuestProgress> createEmptyProgressForQuest(final QuestUserData questUserData, final Quest quest) {
        QuestProgressFactory factory = factoryRegistry.getFactory(quest);
        if (factory == null) {
            throw new IllegalArgumentException("No factory found for quest type: " + quest.getClass()
                                                                                          .getSimpleName());
        }
        return factory.createProgress(questUserData, quest);
    }

    public QuestProgressFactoryRegistry getFactoryRegistry() {
        return factoryRegistry;
    }
}
