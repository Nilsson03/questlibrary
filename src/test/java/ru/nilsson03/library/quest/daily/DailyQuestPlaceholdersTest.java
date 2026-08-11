package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.nilsson03.library.quest.daily.config.DailyQuestConfig;
import ru.nilsson03.library.quest.daily.placeholder.DailyQuestPlaceholders;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

@ExtendWith(MockitoExtension.class)
class DailyQuestPlaceholdersTest {

    @Test
    void buildsTimingAndPlayerPlaceholders() {
        DailyQuestSystem system = mock(DailyQuestSystem.class);
        DailyQuestConfig config = new DailyQuestConfig(5, "1d", DailyAssignmentMode.SHARED, Map.of("EASY", 50));
        when(system.getConfig()).thenReturn(config);
        when(system.getAssignmentMode()).thenReturn(DailyAssignmentMode.SHARED);
        when(system.millisUntilNextReset()).thenReturn(90_000L);
        when(system.getLastUpdateTime()).thenReturn(1_000_000L);

        BaseQuest quest = mock(BaseQuest.class);

        UUID player = UUID.randomUUID();
        when(system.getActiveDailyQuests(player)).thenReturn(List.of(quest));

        QuestUsersStorage usersStorage = mock(QuestUsersStorage.class);
        QuestUserData userData = mock(QuestUserData.class);
        when(userData.questIsComplete(quest)).thenReturn(true);
        when(usersStorage.getQuestUserData(player)).thenReturn(userData);
        when(system.getQuestUsersStorage()).thenReturn(usersStorage);

        DailyQuestPlaceholders placeholders = new DailyQuestPlaceholders(system);
        Map<String, String> map = placeholders.mapForPlayer(player);

        assertEquals("5", map.get("{daily_limit}"));
        assertEquals("SHARED", map.get("{daily_mode}"));
        assertEquals("90", map.get("{daily_time_left_seconds}"));
        assertEquals("1", map.get("{daily_active_count}"));
        assertEquals("1", map.get("{daily_completed_count}"));
        assertEquals("0", map.get("{daily_remaining_count}"));
        assertFalse(map.get("{daily_time_left}").isBlank());
        assertTrue(map.containsKey("{daily_next_reset}"));
    }
}
