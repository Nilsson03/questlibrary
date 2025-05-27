package ru.nilsson03.library.quest.objective.progress;

import org.bukkit.configuration.ConfigurationSection;

public interface ProgressSaver {

    void save(QuestProgress progress, ConfigurationSection section);
}
