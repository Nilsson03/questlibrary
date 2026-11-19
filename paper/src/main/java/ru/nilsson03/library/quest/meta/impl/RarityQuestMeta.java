package ru.nilsson03.library.quest.meta.impl;

import java.util.List;
import java.util.Objects;

import org.bukkit.Material;

import ru.nilsson03.library.quest.meta.QuestMeta;
import ru.nilsson03.library.quest.meta.QuestRarity;

public record RarityQuestMeta(
        QuestRarity rarity,
        String displayName,
        Material displayItem,
        List<String> description) implements QuestMeta {

    public RarityQuestMeta {
        Objects.requireNonNull(rarity, "rarity cannot be null");
        Objects.requireNonNull(displayName, "displayName cannot be null");
        Objects.requireNonNull(displayItem, "displayItem cannot be null");
        description = description == null ? List.of() : List.copyOf(description);
    }

    @Override
    public String displayName() {
        return displayName;
    }
}
