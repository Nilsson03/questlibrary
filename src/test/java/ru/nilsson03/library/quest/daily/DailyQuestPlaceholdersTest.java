package ru.nilsson03.library.quest.daily;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.bukkit.configuration.file.YamlConfiguration;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.nilsson03.library.BaseLibrary;
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

        YamlConfiguration timeConfig = new YamlConfiguration();
        timeConfig.set("time.minutes_first_form", "минута");
        timeConfig.set("time.minutes_second_form", "минуты");
        timeConfig.set("time.minutes_third_form", "минут");
        timeConfig.set("time.seconds_first_form", "секунда");
        timeConfig.set("time.seconds_second_form", "секунды");
        timeConfig.set("time.seconds_third_form", "секунд");
        timeConfig.set("time.hours_first_form", "час");
        timeConfig.set("time.hours_second_form", "часа");
        timeConfig.set("time.hours_third_form", "часов");
        timeConfig.set("time.days_first_form", "день");
        timeConfig.set("time.days_second_form", "дня");
        timeConfig.set("time.days_third_form", "дней");
        timeConfig.set("time.weeks_first_form", "неделя");
        timeConfig.set("time.weeks_second_form", "недели");
        timeConfig.set("time.weeks_third_form", "недель");
        timeConfig.set("time.months_first_form", "месяц");
        timeConfig.set("time.months_second_form", "месяца");
        timeConfig.set("time.months_third_form", "месяцев");
        timeConfig.set("time.years_first_form", "год");
        timeConfig.set("time.years_second_form", "года");
        timeConfig.set("time.years_third_form", "лет");
        BaseLibrary baseLibrary = mock(BaseLibrary.class);
        when(baseLibrary.getConfig()).thenReturn(timeConfig);

        DailyQuestPlaceholders placeholders = new DailyQuestPlaceholders(system);
        Map<String, String> map;
        try (MockedStatic<BaseLibrary> baseLibraryMock = mockStatic(BaseLibrary.class)) {
            baseLibraryMock.when(BaseLibrary::getInstance).thenReturn(baseLibrary);
            map = placeholders.mapForPlayer(player);
        }

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
