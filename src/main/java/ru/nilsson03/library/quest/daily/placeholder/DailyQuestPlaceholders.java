package ru.nilsson03.library.quest.daily.placeholder;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import ru.nilsson03.library.bukkit.util.TimeUtil;
import ru.nilsson03.library.quest.daily.DailyQuestSystem;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

/**
 * Заполнители для UI ежедневных заданий (меню, сообщения, описание).
 *
 * <pre>
 * {daily_time_left}          — время до сброса (читаемый формат, например "1 час, 30 минут")
 * {daily_time_left_short}    — краткая форма (например "1Ч 30М")
 * {daily_time_left_seconds}  — количество секунд до сброса
 * {daily_time_left_ms}       — количество миллисекунд до сброса
 * {daily_next_reset}         — дата и время следующего сброса (дд.ММ.гггг ЧЧ:мм)
 * {daily_last_update}        — дата и время последней ротации
 * {daily_limit}              — установленный лимит выбора
 * {daily_mode}               — ОБЩИЙ / ЛИЧНЫЙ
 * {daily_active_count}       — количество активных ежедневных заданий у игрока
 * {daily_completed_count}    — сколько из них игрок выполнил
 * {daily_remaining_count}    — активные минус выполненные
 * </pre>
 */
public final class DailyQuestPlaceholders {

    public static final String DATE_FORMAT = "dd.MM.yyyy HH:mm";

    private final DailyQuestSystem system;

    public DailyQuestPlaceholders(DailyQuestSystem system) {
        this.system = Objects.requireNonNull(system, "system");
    }

    public ReplaceData[] global() {
        return toArray(globalMap());
    }

    public ReplaceData[] forPlayer(UUID playerId) {
        return toArray(mapForPlayer(playerId));
    }

    public Map<String, String> globalMap() {
        Map<String, String> map = new LinkedHashMap<>();
        putTiming(map);
        map.put("{daily_limit}", String.valueOf(system.getConfig().limit()));
        map.put("{daily_mode}", system.getAssignmentMode().name());
        return map;
    }

    public Map<String, String> mapForPlayer(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        Map<String, String> map = globalMap();

        List<BaseQuest> active = system.getActiveDailyQuests(playerId);
        int activeCount = active.size();
        int completedCount = countCompleted(playerId, active);

        map.put("{daily_active_count}", String.valueOf(activeCount));
        map.put("{daily_completed_count}", String.valueOf(completedCount));
        map.put("{daily_remaining_count}", String.valueOf(Math.max(0, activeCount - completedCount)));
        return map;
    }

    public String apply(String text) {
        return UniversalTextApi.replacePlaceholders(text, global());
    }

    public String apply(String text, UUID playerId) {
        return UniversalTextApi.replacePlaceholders(text, forPlayer(playerId));
    }

    public List<String> applyAll(List<String> lines) {
        List<String> result = new ArrayList<>();
        if (lines == null) {
            return result;
        }
        for (String line : lines) {
            result.add(apply(line));
        }
        return result;
    }

    public List<String> applyAll(List<String> lines, UUID playerId) {
        List<String> result = new ArrayList<>();
        if (lines == null) {
            return result;
        }
        for (String line : lines) {
            result.add(apply(line, playerId));
        }
        return result;
    }

    private void putTiming(Map<String, String> map) {
        long millisLeft = system.millisUntilNextReset();
        long secondsLeft = millisLeft / 1000L;
        long lastUpdate = system.getLastUpdateTime();
        long nextReset = lastUpdate > 0
                ? lastUpdate + system.getConfig().updatePeriodMillis()
                : System.currentTimeMillis() + millisLeft;

        map.put("{daily_time_left}", TimeUtil.getTime(secondsLeft));
        map.put("{daily_time_left_short}", TimeUtil.parseTimeToString(secondsLeft));
        map.put("{daily_time_left_seconds}", String.valueOf(secondsLeft));
        map.put("{daily_time_left_ms}", String.valueOf(millisLeft));
        map.put("{daily_next_reset}", formatMillis(nextReset));
        map.put("{daily_last_update}", lastUpdate > 0 ? formatMillis(lastUpdate) : "-");
    }

    private int countCompleted(UUID playerId, List<BaseQuest> active) {
        QuestUsersStorage usersStorage = system.getQuestUsersStorage();
        if (usersStorage == null || active.isEmpty()) {
            return 0;
        }
        QuestUserData userData = usersStorage.getQuestUserData(playerId);
        if (userData == null) {
            return 0;
        }
        int completed = 0;
        for (BaseQuest quest : active) {
            if (userData.questIsComplete(quest)) {
                completed++;
            }
        }
        return completed;
    }

    private static String formatMillis(long epochMillis) {
        return TimeUtil.formatDate(new Date(epochMillis), DATE_FORMAT);
    }

    private static ReplaceData[] toArray(Map<String, String> map) {
        return map.entrySet().stream()
                .map(entry -> new ReplaceData(entry.getKey(), entry.getValue()))
                .toArray(ReplaceData[]::new);
    }
}
