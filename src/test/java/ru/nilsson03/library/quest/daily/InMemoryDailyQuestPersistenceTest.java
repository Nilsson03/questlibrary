package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import ru.nilsson03.library.bukkit.util.Namespace;
import ru.nilsson03.library.quest.daily.persistence.InMemoryDailyQuestPersistence;
import ru.nilsson03.library.quest.meta.QuestRarity;
import ru.nilsson03.library.quest.meta.impl.RarityQuestMeta;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

class InMemoryDailyQuestPersistenceTest {

    @Test
    void storesSharedAndPlayerSelections() {
        InMemoryDailyQuestPersistence persistence = new InMemoryDailyQuestPersistence();
        BaseQuest quest = quest("q1");
        long now = 123456L;

        persistence.saveSharedQuests(List.of(quest), now);
        assertEquals(now, persistence.getLastUpdateTime().orElseThrow());
        assertEquals(List.of(quest), persistence.loadSharedQuests());

        UUID player = UUID.randomUUID();
        persistence.savePlayerQuests(player, List.of(quest), now);
        assertEquals(List.of(quest), persistence.loadPlayerQuests(player));
        assertTrue(persistence.loadAllPlayerQuests().containsKey(player));

        persistence.clearAllPlayerQuests();
        assertTrue(persistence.loadPlayerQuests(player).isEmpty());
    }

    private static BaseQuest quest(String key) {
        BaseQuest quest = Mockito.mock(BaseQuest.class);
        Mockito.when(quest.questUniqueKey()).thenReturn(Namespace.of("Test", key));
        Mockito.when(quest.questMeta())
                .thenReturn(new RarityQuestMeta(QuestRarity.of("EASY"), key, Material.STONE, List.of()));
        return quest;
    }
}
