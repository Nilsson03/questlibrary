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
        return new HasItemCondition(material, amount);
    }
}
