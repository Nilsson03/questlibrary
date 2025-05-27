package ru.nilsson03.library.quest.storage.loader;

import ru.nilsson03.library.quest.core.service.QuestService;
import ru.nilsson03.library.quest.storage.loader.impl.BaseQuestLoader;
import ru.nilsson03.library.quest.storage.loader.impl.StagedQuestLoader;

public class QuestLoaderFactory {

    public static QuestLoader createLoader(QuestService questService, String type) {
        return switch (type.toLowerCase()) {
            case "simple" -> new BaseQuestLoader(questService);
            case "staged" -> new StagedQuestLoader(questService);
            default -> throw new IllegalArgumentException("Unknown quest type: " + type);
        };
    }
}
