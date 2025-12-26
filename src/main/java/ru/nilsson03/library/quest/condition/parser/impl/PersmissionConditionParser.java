package ru.nilsson03.library.quest.condition.parser.impl;

import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.PermissionCondition;
import ru.nilsson03.library.quest.parser.Parser;

public class PersmissionConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        Objects.requireNonNull(section, "Configuration section is null");
        String permission = section.getString("permission");
        return new PermissionCondition(permission);
    }
}
