package ru.nilsson03.library.quest.meta.impl;

import ru.nilsson03.library.quest.meta.DailyQuestMeta;

import java.util.List;

public record SimpleDailyQuestMeta(int weight, List<String> description, String displayName, String updateTime)
        implements DailyQuestMeta {}
