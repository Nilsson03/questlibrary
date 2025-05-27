package ru.nilsson03.library.quest.condition.parser.impl;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.AndCondition;
import ru.nilsson03.library.quest.condition.parser.registry.ConditionParserRegistry;
import ru.nilsson03.library.quest.parser.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AndConditionParser implements Parser<QuestCondition> {
    private final ConditionParserRegistry registry;

    public AndConditionParser(ConditionParserRegistry registry) {
        this.registry = registry;
    }

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        Objects.requireNonNull(section, "Configuration section is null");
        List<QuestCondition> conditions = new ArrayList<>();
        for (String key : section.getKeys(false)) {

            if (section.getConfigurationSection(key) == null) {
                continue;
            }

            conditions.add(registry.parse(section.getConfigurationSection(key)));
        }
        return new AndCondition(conditions);
    }
}
