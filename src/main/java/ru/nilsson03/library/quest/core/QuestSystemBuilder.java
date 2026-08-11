package ru.nilsson03.library.quest.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.bukkit.event.Event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.nilsson03.library.NPlugin;
import ru.nilsson03.library.quest.QuestLibrary;
import ru.nilsson03.library.quest.core.manager.QuestManager;
import ru.nilsson03.library.quest.core.progress.ProgressTargetResolver;
import ru.nilsson03.library.quest.core.service.QuestLifecycleService;
import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.quest.completer.QuestCompleter;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.QuestStorageManager;
import ru.nilsson03.library.quest.storage.loader.QuestLoader;
import ru.nilsson03.library.quest.user.data.UserDataPersistent;
import ru.nilsson03.library.quest.user.data.impl.SqlUserPersistent;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class QuestSystemBuilder {

    private final NPlugin plugin;
    private QuestLoader questLoader;
    private Function<QuestService, QuestLoader> questLoaderFactory;
    private UserDataPersistent persistent;
    private ProgressTargetResolver progressTargetResolver;
    private Function<QuestUsersStorage, ProgressTargetResolver> progressTargetResolverFactory;
    private final Map<String, Class<? extends Event>> objectiveTypes = new HashMap<>();
    private final Map<String, ObjectiveGoalFactory> goalFactories = new HashMap<>();
    private final List<CompleterRegistration> completerRegistrations = new ArrayList<>();
    private final List<MetaParserRegistration> metaParserRegistrations = new ArrayList<>();

    public static QuestSystemBuilder create(NPlugin plugin) {
        return new QuestSystemBuilder(plugin);
    }

    public QuestSystemBuilder withQuestLoader(QuestLoader questLoader) {
        this.questLoader = questLoader;
        return this;
    }

    public QuestSystemBuilder withProgressTargetResolver(ProgressTargetResolver progressTargetResolver) {
        this.progressTargetResolver = progressTargetResolver;
        this.progressTargetResolverFactory = null;
        return this;
    }

    public QuestSystemBuilder withProgressTargetResolverFactory(
            Function<QuestUsersStorage, ProgressTargetResolver> progressTargetResolverFactory) {
        this.progressTargetResolverFactory = progressTargetResolverFactory;
        this.progressTargetResolver = null;
        return this;
    }

    public QuestSystemBuilder registerMetaParser(String key, Parser<QuestMeta> parser) {
        metaParserRegistrations.add(new MetaParserRegistration(plugin.getName(), key, parser));
        return this;
    }

    public QuestSystemBuilder withQuestLoaderFactory(Function<QuestService, QuestLoader> questLoaderFactory) {
        this.questLoaderFactory = questLoaderFactory;
        return this;
    }

    public QuestSystemBuilder registerObjectiveType(String key, Class<? extends Event> eventClass) {
        objectiveTypes.put(key, eventClass);
        return this;
    }

    public QuestSystemBuilder withUserPersistent(UserDataPersistent persistent) {
        this.persistent = persistent;
        return this;
    }

    public QuestSystemBuilder registerGoalFactory(String key, ObjectiveGoalFactory factory) {
        goalFactories.put(key, factory);
        return this;
    }

    public <T extends BaseQuest> QuestSystemBuilder registerCompleter(
            Class<T> questClass,
            BiFunction<QuestStorage, QuestLifecycleService, QuestCompleter> completerFactory) {
        completerRegistrations.add(new CompleterRegistration(questClass, completerFactory));
        return this;
    }

    public QuestSystemContext build() {
        if (questLoader == null && questLoaderFactory == null) {
            throw new IllegalStateException("QuestLoader must be set before building");
        }

        QuestLibrary questLibrary = QuestLibrary.getApi();
        QuestStorageManager storageManager = questLibrary.getQuestStorageManager();

        QuestService questService = new QuestService(plugin, null);

        for (MetaParserRegistration registration : metaParserRegistrations) {
            questService.getMetaParserRegistry()
                    .registerParser(registration.pluginName, registration.key, registration.parser);
        }

        if (questLoader == null) {
            questLoader = questLoaderFactory.apply(questService);
        }

        QuestStorage questStorage = storageManager.loadQuestStorage(plugin, questLoader);
        UserDataPersistent userDataPersistent;
        if (persistent == null) {
            userDataPersistent = new SqlUserPersistent(plugin, questStorage);
        } else {
            userDataPersistent = persistent;
        }

        QuestUsersStorage questUsersStorage = new QuestUsersStorage(plugin, userDataPersistent);

        ProgressTargetResolver resolver;
        if (progressTargetResolverFactory != null) {
            resolver = progressTargetResolverFactory.apply(questUsersStorage);
        } else if (progressTargetResolver != null) {
            resolver = progressTargetResolver;
        } else {
            resolver = ProgressTargetResolver.identity(questUsersStorage);
        }

        questService.initializeQuestManager(questUsersStorage, resolver);
        QuestManager questManager = questService.getQuestManager();

        ObjectiveRegistry objectiveRegistry = questService.getObjectiveRegistry();

        objectiveTypes.forEach(
                (key, eventClass) -> objectiveRegistry.registerObjectiveType(ObjectiveType.create(key, eventClass)));

        goalFactories
                .forEach((key, factory) -> objectiveRegistry.getObjectiveGoalRegistry().registerFactory(key, factory));

        QuestLifecycleService lifecycleService = questManager.getQuestLifecycleService();
        completerRegistrations.forEach(registration -> questManager.getQuestCompleterRegistry().registerCompleter(
                registration.questClass,
                registration.completerFactory.apply(questStorage, lifecycleService)));

        return new QuestSystemContext(
                questService, questStorage, questUsersStorage, questManager, userDataPersistent, resolver);
    }

    private static class CompleterRegistration {
        private final Class<? extends BaseQuest> questClass;
        private final BiFunction<QuestStorage, QuestLifecycleService, QuestCompleter> completerFactory;

        CompleterRegistration(Class<? extends BaseQuest> questClass,
                BiFunction<QuestStorage, QuestLifecycleService, QuestCompleter> completerFactory) {
            this.questClass = questClass;
            this.completerFactory = completerFactory;
        }
    }

    private static class MetaParserRegistration {
        private final String pluginName;
        private final String key;
        private final Parser<QuestMeta> parser;

        MetaParserRegistration(String pluginName, String key, Parser<QuestMeta> parser) {
            this.pluginName = pluginName;
            this.key = key;
            this.parser = parser;
        }
    }
}
