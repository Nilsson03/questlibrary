package ru.nilsson03.library.quest.quest.simple;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.objective.Objective;

import java.util.List;

public interface BaseQuest extends Quest {
    List<Objective> objectives();
}
