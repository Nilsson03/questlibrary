package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.bukkit.configuration.file.YamlConfiguration;
import org.junit.jupiter.api.Test;

import ru.nilsson03.library.quest.daily.config.DailyQuestConfig;
import ru.nilsson03.library.quest.daily.config.RarityDefinition;
import ru.nilsson03.library.quest.meta.QuestRarity;

class DailyQuestConfigTest {

    @Test
    void loadsDynamicRaritiesFromYamlLegacyIntForm() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("limit", 7);
        yaml.set("update-period", "2d");
        yaml.set("assignment-mode", "PERSONAL");
        yaml.set("rarities.EASY", 10);
        yaml.set("rarities.CUSTOM_TIER", 99);

        DailyQuestConfig config = DailyQuestConfig.fromYaml(yaml);

        assertEquals(7, config.limit());
        assertEquals("2d", config.updatePeriod());
        assertEquals(2L * 24 * 60 * 60 * 1000, config.updatePeriodMillis());
        assertEquals(DailyAssignmentMode.PERSONAL, config.assignmentMode());
        assertEquals(10, config.weightOf(QuestRarity.of("EASY")));
        assertEquals(99, config.weightOf("CUSTOM_TIER"));
        assertEquals("EASY", config.displayNameOf("EASY"));
        assertEquals("CUSTOM_TIER", config.displayNameOf(QuestRarity.of("CUSTOM_TIER")));
        assertTrue(config.hasRarity("CUSTOM_TIER"));
        assertFalse(config.hasRarity("MISSING"));
    }

    @Test
    void loadsRaritiesWithDisplayNameObjectForm() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("limit", 5);
        yaml.set("update-period", "1d");
        yaml.set("assignment-mode", "SHARED");
        yaml.set("rarities.EASY.weight", 50);
        yaml.set("rarities.EASY.display-name", "&#9DF8C0&lEASY");
        yaml.set("rarities.HARD.weight", 30);
        yaml.set("rarities.HARD.display-name", "&#FF996C&lHARD");

        DailyQuestConfig config = DailyQuestConfig.fromYaml(yaml);

        assertEquals(50, config.weightOf("EASY"));
        assertEquals("&#9DF8C0&lEASY", config.displayNameOf("EASY"));
        assertEquals(30, config.weightOf(QuestRarity.of("HARD")));
        assertEquals("&#FF996C&lHARD", config.displayNameOf(QuestRarity.of("HARD")));
    }

    @Test
    void objectFormWithoutDisplayNameFallsBackToId() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("rarities.EPIC.weight", 15);

        DailyQuestConfig config = DailyQuestConfig.fromYaml(yaml);

        assertEquals(15, config.weightOf("EPIC"));
        assertEquals("EPIC", config.displayNameOf("EPIC"));
    }

    @Test
    void constructorAcceptsIntegerWeightMap() {
        DailyQuestConfig config = new DailyQuestConfig(
                3,
                "1d",
                DailyAssignmentMode.SHARED,
                Map.of("EASY", 50, "HARD", 30));

        assertEquals(50, config.weightOf("EASY"));
        assertEquals("EASY", config.displayNameOf("EASY"));
        assertEquals(Map.of("EASY", 50, "HARD", 30), config.rarityWeights());
    }

    @Test
    void constructorAcceptsRarityDefinitions() {
        DailyQuestConfig config = new DailyQuestConfig(
                3,
                "1d",
                DailyAssignmentMode.SHARED,
                Map.of("EASY", new RarityDefinition(50, "&aEasy")));

        assertEquals(50, config.weightOf("EASY"));
        assertEquals("&aEasy", config.displayNameOf("EASY"));
    }

    @Test
    void withOverridesReplaceSelectedFields() {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("limit", 5);
        yaml.set("update-period", "1d");
        yaml.set("assignment-mode", "SHARED");
        yaml.set("rarities.EASY.weight", 50);
        yaml.set("rarities.EASY.display-name", "&aEasy");

        DailyQuestConfig config = DailyQuestConfig.fromYaml(yaml)
                .withOverrides(2, "12H", DailyAssignmentMode.PERSONAL);

        assertEquals(2, config.limit());
        assertEquals("12H", config.updatePeriod());
        assertEquals(DailyAssignmentMode.PERSONAL, config.assignmentMode());
        assertEquals(50, config.weightOf("EASY"));
        assertEquals("&aEasy", config.displayNameOf("EASY"));
    }
}
