package ru.nightvision.quests.condition.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nightvision.quests.condition.KillEntityCondition;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class KillEntityConditionParser implements Parser<QuestCondition> {

    @Override
    public QuestCondition parse(ConfigurationSection section) {
        if (section == null) {
            throw new IllegalArgumentException("KillEntityCondition section cannot be null");
        }

        Set<String> requiredWeapons = toUpperCaseSet(section.getStringList("required_weapons"));
        Set<String> requiredArmor = toUpperCaseSet(section.getStringList("required_armor"));
        Set<String> requiredEffects = toUpperCaseSet(section.getStringList("required_effects"));

        Double minDistance = section.isSet("min_distance") ? section.getDouble("min_distance") : null;
        Double maxDistance = section.isSet("max_distance") ? section.getDouble("max_distance") : null;
        boolean requireNoAggro = section.getBoolean("require_no_aggro", false);

        return new KillEntityCondition(
                requiredWeapons,
                requiredArmor,
                requiredEffects,
                minDistance,
                maxDistance,
                requireNoAggro
        );
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
}
