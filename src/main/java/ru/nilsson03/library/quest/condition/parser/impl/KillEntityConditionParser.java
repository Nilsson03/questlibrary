package ru.nilsson03.library.quest.condition.parser.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.condition.impl.KillEntityCondition;
import ru.nilsson03.library.quest.parser.Parser;

public class KillEntityConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("KillEntityCondition section cannot be null");
        }

        Set<String> requiredWeapons = toUpperCaseSet(section.getStringList("required_weapons"));
        Set<String> requiredArmor = toUpperCaseSet(section.getStringList("required_armor"));
        Map<PotionEffectType, Integer> parsedEffects = parseRequiredEffects(section.getStringList("required_effects"));

        Double minDistance = section.isSet("min_distance") ? section.getDouble("min_distance") : null;
        Double maxDistance = section.isSet("max_distance") ? section.getDouble("max_distance") : null;
        boolean requireNoAggro = section.getBoolean("require_no_aggro", false);

        return new KillEntityCondition(
                requiredWeapons,
                requiredArmor,
                parsedEffects,
                minDistance,
                maxDistance,
                requireNoAggro);
    }

    private Set<String> toUpperCaseSet(List<String> values) {
        Set<String> result = new HashSet<>();
        if (values != null) {
            for (String value : values) {
                if (value != null && !value.isBlank()) {
                    result.add(value.toUpperCase());
                }
            }
        }
        return result;
    }

    public Map<PotionEffectType, Integer> parseRequiredEffects(List<String> requiredEffects) {
        Map<PotionEffectType, Integer> effects = new HashMap<>();

        if (requiredEffects == null || requiredEffects.isEmpty()) {
            return effects;
        }

        for (String effectString : requiredEffects) {
            String[] parts = effectString.split(":");

            if (parts.length == 2) {
                String effectName = parts[0].toUpperCase();
                int level = Integer.parseInt(parts[1]);

                PotionEffectType effectType = PotionEffectType.getByName(effectName);
                if (effectType != null) {
                    effects.put(effectType, level);
                } else {
                    ConsoleLogger.warn("questlibrary", "Unknown potion effect: %s", effectName);
                }
            } else {
                ConsoleLogger.warn("questlibrary", "Invalid effect format (should be EFFECT:LEVEL): %s", effectString);
            }
        }

        return effects;
    }
}
