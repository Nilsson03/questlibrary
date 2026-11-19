package ru.nilsson03.library.quest.condition.parser.impl;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.HasItemCondition;
import ru.nilsson03.library.quest.parser.Parser;

public class HasItemConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        Material material = Material.valueOf(section.getString("material"));
        int amount = section.getInt("amount");
        QuestCondition.ConditionType conditionType = parseConditionType(section);
        return new HasItemCondition(material, amount, conditionType);
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
