package ru.nilsson03.library.quest.meta.parser;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.QuestRarity;
import ru.nilsson03.library.quest.meta.impl.RarityQuestMeta;
import ru.nilsson03.library.quest.parser.Parser;

public class RarityMetaParser implements Parser<QuestMeta> {

    @Override
    public QuestMeta parse(ConfigurationSection section) {
        String rarityString = section.getString("rarity");
        if (rarityString == null || rarityString.isBlank()) {
            rarityString = section.getString("rare", "DEFAULT");
        }
        QuestRarity rarity = QuestRarity.fromString(rarityString);
        String displayName = section.getString("displayName", "Quest");
        Material displayItem = Material.matchMaterial(section.getString("displayItem", "STONE"));
        if (displayItem == null) {
            displayItem = Material.STONE;
        }
        List<String> description = section.getStringList("description");
        return new RarityQuestMeta(rarity, displayName, displayItem, description);
    }
}
