package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.daily.config.DailyQuestConfig;
import ru.nilsson03.library.quest.daily.selector.WeightedRaritySelector;
import ru.nilsson03.library.quest.meta.QuestRarity;
import ru.nilsson03.library.quest.meta.impl.RarityQuestMeta;
import ru.nilsson03.library.quest.meta.impl.SimpleQuestMeta;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

class WeightedRaritySelectorTest {

    @Test
    void selectsUpToLimitUniqueQuests() {
        DailyQuestConfig config = new DailyQuestConfig(3, "1d", DailyAssignmentMode.SHARED, defaultWeights());
        WeightedRaritySelector selector = new WeightedRaritySelector(config, new Random(42));

        List<BaseQuest> pool = new ArrayList<>();
        pool.add(quest("q1", "EASY"));
        pool.add(quest("q2", "HARD"));
        pool.add(quest("q3", "EPIC"));
        pool.add(quest("q4", "MASTER"));
        pool.add(quest("q5", "EASY"));

        List<BaseQuest> selected = selector.select(pool);

        assertEquals(3, selected.size());
        assertEquals(3, selected.stream().distinct().count());
    }

    @Test
    void supportsCustomRarityIdsFromConfig() {
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("LEGENDARY", 100);
        DailyQuestConfig config = new DailyQuestConfig(1, "1d", DailyAssignmentMode.SHARED, weights);
        WeightedRaritySelector selector = new WeightedRaritySelector(config, new Random(1));

        BaseQuest legendary = quest("leg", "LEGENDARY");
        BaseQuest easy = quest("easy", "EASY");

        List<BaseQuest> selected = selector.select(List.of(legendary, easy));
        assertEquals(1, selected.size());
        assertEquals(legendary, selected.get(0));
    }

    @Test
    void skipsQuestsWithoutRarityMeta() {
        DailyQuestConfig config = new DailyQuestConfig(5, "1d", DailyAssignmentMode.SHARED, defaultWeights());
        WeightedRaritySelector selector = new WeightedRaritySelector(config, new Random(1));

        BaseQuest withRarity = quest("rare", "EASY");
        BaseQuest withoutRarity = mock(BaseQuest.class);
        when(withoutRarity.questMeta()).thenReturn(new SimpleQuestMeta(1, List.of(), "plain"));
        when(withoutRarity.questUniqueKey()).thenReturn(Namespace.of("Test", "plain"));

        List<BaseQuest> selected = selector.select(List.of(withRarity, withoutRarity));

        assertEquals(1, selected.size());
        assertEquals(withRarity, selected.get(0));
    }

    @Test
    void returnsEmptyWhenAllWeightsZero() {
        Map<String, Integer> zero = new LinkedHashMap<>();
        zero.put("EASY", 0);
        zero.put("HARD", 0);
        DailyQuestConfig config = new DailyQuestConfig(3, "1d", DailyAssignmentMode.SHARED, zero);
        WeightedRaritySelector selector = new WeightedRaritySelector(config, new Random(7));

        List<BaseQuest> selected = selector.select(List.of(
                quest("a", "EASY"),
                quest("b", "HARD")));

        assertTrue(selected.isEmpty());
    }

    private static Map<String, Integer> defaultWeights() {
        Map<String, Integer> weights = new LinkedHashMap<>();
        weights.put("EASY", 50);
        weights.put("HARD", 30);
        weights.put("EPIC", 15);
        weights.put("MASTER", 5);
        return weights;
    }

    private static BaseQuest quest(String key, String rarity) {
        BaseQuest quest = mock(BaseQuest.class);
        when(quest.questUniqueKey()).thenReturn(Namespace.of("Test", key));
        when(quest.questMeta()).thenReturn(new RarityQuestMeta(
                QuestRarity.of(rarity), key, Material.STONE, List.of()));
        return quest;
    }
}
