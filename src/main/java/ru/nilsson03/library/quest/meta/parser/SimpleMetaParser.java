package ru.nilsson03.library.quest.meta.parser;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.impl.SimpleQuestMeta;
import ru.nilsson03.library.quest.parser.Parser;

public class SimpleMetaParser implements Parser<QuestMeta> {

    @Override
    public QuestMeta parse(ConfigurationSection configurationSection) {
        int weight = configurationSection.getInt("weight");
        List<String> description = configurationSection.getStringList("description");
        String displayName = configurationSection.getString("displayName");
        boolean daily = configurationSection.getBoolean("daily");
        return new SimpleQuestMeta(weight, description, displayName, daily);
    }
    
}
