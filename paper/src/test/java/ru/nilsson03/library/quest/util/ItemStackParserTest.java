package ru.nilsson03.library.quest.util;

import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import ru.nilsson03.library.quest.util.text.QuestText;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ItemStackParserTest {
    @BeforeEach void mockServer() { MockBukkit.mock(); }
    @AfterEach void stopServer() { MockBukkit.unmock(); }

    @Test void preservesConfiguredAmountNameLoreAndModelData() {
        var item = ItemStackParser.fromMap(Map.of("material", "AMETHYST_SHARD", "amount", 4,
                "name", "<aqua>Shard", "lore", List.of("&7Description"), "customModelData", 73));
        assertEquals(Material.AMETHYST_SHARD, item.getType());
        assertEquals(4, item.getAmount());
        var meta = item.getItemMeta();
        assertEquals(QuestText.component("<aqua>Shard"), meta.displayName());
        assertEquals(List.of(QuestText.component("&7Description")), meta.lore());
        assertEquals(73, meta.getCustomModelData());
    }

    @Test void mapsLegacyPotionFlagsToModernPotionVariants() {
        var item = ItemStackParser.fromMap(Map.of("material", "POTION",
                "potionData", Map.of("type", "SPEED", "extended", true)));
        assertEquals(PotionType.LONG_SWIFTNESS, ((PotionMeta) item.getItemMeta()).getBasePotionType());
    }
}
