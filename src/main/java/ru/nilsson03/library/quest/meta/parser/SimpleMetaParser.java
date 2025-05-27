package ru.nilsson03.library.quest.meta.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.impl.SimpleQuestMeta;
import ru.nilsson03.library.quest.parser.Parser;

import java.util.List;

public class SimpleMetaParser implements Parser<QuestMeta> {

    @Override
    public QuestMeta parse(ConfigurationSection section) {
        int weight = section.getInt("weight", 0);
        List<String> description = section.getStringList("description");
        String displayName = section.getString("displayName", "Unnamed Quest");
        boolean daily = section.getBoolean("daily", false);
        return new SimpleQuestMeta(weight, description, displayName, daily);
    }
}
