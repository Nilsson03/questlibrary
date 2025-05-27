package ru.nilsson03.library.quest.objective.progress.registry;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.progress.ProgressSaver;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.progress.parser.BaseProgressParser;
import ru.nilsson03.library.quest.objective.progress.parser.StagedProgressParser;
import ru.nilsson03.library.quest.objective.progress.saver.BaseProgressSaver;
import ru.nilsson03.library.quest.objective.progress.saver.StagedProgressSaver;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.parser.ParserRegistry;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;

public class QuestProgressRegistry extends ParserRegistry<Parser<QuestProgress>, QuestProgress> {

    private final QuestStorage questStorage;
    private final ObjectiveGoalFactoryRegistry objectiveGoalFactoryRegistry;
    private final QuestUsersStorage questUsersStorage;

    private static final Map<String, ProgressSaver> progressSavers = new HashMap<>();

    public QuestProgressRegistry(
            final QuestStorage questStorage, final ObjectiveGoalFactoryRegistry objectiveGoalFactoryRegistry,
            final QuestUsersStorage questUsersStorage) {
        this.questStorage = questStorage;
        this.objectiveGoalFactoryRegistry = objectiveGoalFactoryRegistry;
        this.questUsersStorage = questUsersStorage;
    }

    public void registerSaver(String key, ProgressSaver saver) {
        if (progressSavers.containsKey(key)) {
            throw new IllegalArgumentException("Progress saver already registered for key " + key);
        }
        progressSavers.putIfAbsent(key, saver);
    }

    public ProgressSaver getSaver(String key) throws IllegalArgumentException {
        if (!progressSavers.containsKey(key)) {
            throw new IllegalArgumentException("No progress saver found for key " + key);
        }

        return progressSavers.get(key);
    }

    /**
     * {@inheritDoc}
     */
    public void registerParser(String pluginName, String key, Parser<QuestProgress> parser) {
        super.registerParser("pluginName", key, parser);
    }

    @Override
    public void onRegistryInit() {
        registerParser("questlibrary", "base", new BaseProgressParser(questStorage, objectiveGoalFactoryRegistry, questUsersStorage));
        registerParser("questlibrary", "staged", new StagedProgressParser(questStorage, objectiveGoalFactoryRegistry, questUsersStorage));
        registerSaver("base", new BaseProgressSaver());
        registerSaver("staged", new StagedProgressSaver());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestProgress parse(ConfigurationSection section) {
        return super.parse(section);
    }
}
