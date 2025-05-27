package ru.nilsson03.library.quest.meta.impl;

import ru.nilsson03.library.quest.meta.QuestMeta;

import java.util.List;

public record SimpleQuestMeta(int weight, List<String> description, String displayName, boolean daily)
        implements QuestMeta {}
