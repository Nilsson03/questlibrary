package ru.nilsson03.library.quest.parser;

import org.bukkit.configuration.ConfigurationSection;

@FunctionalInterface
public interface Parser<O> {

    O parse(ConfigurationSection section);
}
