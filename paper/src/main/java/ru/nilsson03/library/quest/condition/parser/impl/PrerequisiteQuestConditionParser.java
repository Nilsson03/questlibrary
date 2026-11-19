package ru.nilsson03.library.quest.condition.parser.impl;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.PrerequisiteQuestCondition;
import ru.nilsson03.library.quest.parser.Parser;

import java.util.List;

public class PrerequisiteQuestConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("PrerequisiteQuestCondition section cannot be null");
        }

        List<String> questIds = section.getStringList("prerequisite-quests");
        if (questIds == null || questIds.isEmpty()) {
            throw new IllegalArgumentException("PrerequisiteQuestCondition requires non-empty 'quests' list");
        }

        return new PrerequisiteQuestCondition(questIds);
    }
}
