package ru.nilsson03.library.quest.condition.parser.impl;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.LevelCondition;
import ru.nilsson03.library.quest.parser.Parser;

public class LevelConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        int minLevel = section.getInt("min-level", 0);
        int maxLevel = section.getInt("max-level", Integer.MAX_VALUE);
        QuestCondition.ConditionType conditionType = parseConditionType(section);
        
        return new LevelCondition(minLevel, maxLevel, conditionType);
    }
    
    private QuestCondition.ConditionType parseConditionType(ConfigurationSection section) {
        String typeString = section.getString("condition-type", "START");
        try {
            return QuestCondition.ConditionType.valueOf(typeString.toUpperCase());
        } catch (IllegalArgumentException e) {
            return QuestCondition.ConditionType.START;
        }
    }
}
