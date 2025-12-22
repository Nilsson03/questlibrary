package ru.nilsson03.library.quest.condition.parser.impl;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.Plugin;
import ru.nilsson03.library.quest.QuestLibrary;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.QuestCompletedCondition;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.exception.QuestStorageDuplicateException;
import ru.nilsson03.library.quest.exception.QuestStorageException;
import ru.nilsson03.library.quest.exception.QuestStorageNotLoadedException;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.storage.QuestStorage;
import ru.nilsson03.library.quest.storage.QuestStorageManager;

public class QuestCompletedConditionParser implements Parser<QuestCondition> {

    private final Plugin plugin;
    private final QuestStorage questStorage;

    public QuestCompletedConditionParser(Plugin plugin) throws QuestStorageException {
        this.plugin = plugin;

        QuestStorageManager questStorageManager = QuestLibrary.getApi()
                                                              .getQuestStorageManager();

        try {
            this.questStorage = questStorageManager.getQuestStorageByPlugin(plugin);
        } catch (QuestStorageNotLoadedException exception) {
            String errorMessage = "It was not possible to get the quest repository for the " + plugin.getName() + " plugin because the repository for this plugin was not loaded!";
            plugin.getLogger()
                  .severe(errorMessage);
            throw new QuestStorageException(errorMessage, exception);
        } catch (QuestStorageDuplicateException exception) {
            String errorMessage = "It was not possible to get the quest repository for the " + plugin.getName() + " plugin, because more than one repository was found in the manager!";
            plugin.getLogger()
                  .severe(errorMessage);
            throw new QuestStorageException(errorMessage, exception);
        }
    }

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        String questId = section.getString("quest_completed");

        Quest requiredQuest;
        try {
            requiredQuest = questStorage.getQuestByUniqueKeyOrThrow(questId);
        } catch (IllegalArgumentException exception) {
            String errorMessage = "Couldn't get the required quest in the QuestCompletedCondition condition in plugin " + plugin.getName() + " with quest id " + questId;
            plugin.getLogger()
                  .warning(errorMessage);
            throw new NullPointerException(errorMessage);
        }
        return new QuestCompletedCondition(requiredQuest);
    }
}
