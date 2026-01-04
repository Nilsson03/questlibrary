package ru.nilsson03.library.quest.meta.parser;

import java.util.List;

import org.bukkit.configuration.ConfigurationSection;

import ru.nilsson03.library.quest.meta.DailyQuestMeta;
import ru.nilsson03.library.quest.meta.impl.SimpleDailyQuestMeta;
import ru.nilsson03.library.quest.parser.Parser;

public class DailyMetaParser implements Parser<DailyQuestMeta> {

    @Override
    public DailyQuestMeta parse(ConfigurationSection configurationSection) {
        int weight = configurationSection.getInt("weight", 1);
        List<String> description = configurationSection.getStringList("description");
        String displayName = configurationSection.getString("displayName", "Daily Quest");
        String updateTime = configurationSection.getString("updateTime", "1d");
        
        return new SimpleDailyQuestMeta(weight, description, displayName, updateTime);
    }
}
