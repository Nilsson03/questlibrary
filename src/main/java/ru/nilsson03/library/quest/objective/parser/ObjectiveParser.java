package ru.nilsson03.library.quest.objective.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.helper.GoalHelper;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.parser.Parser;

public class ObjectiveParser implements Parser<Objective> {

    private final ObjectiveRegistry objectiveRegistry;
    private final ObjectiveGoalFactoryRegistry objectiveGoalRegistry;
    private final Set<String> usedObjectiveKeys = new HashSet<>();

    public ObjectiveParser(
            final ObjectiveRegistry objectiveRegistry,
            final ObjectiveGoalFactoryRegistry objectiveGoalRegistry) {
        this.objectiveRegistry = objectiveRegistry;
        this.objectiveGoalRegistry = objectiveGoalRegistry;
    }

    @Override
    public Objective parse(ConfigurationSection section) {
        String key = section.getString("key");

        if (key == null || key.isEmpty()) {
            key = UUID.randomUUID().toString();
            ConsoleLogger.warn("questlibrary", "Objective key is missing in config, generated random key: %s", key);
        }

        if (usedObjectiveKeys.contains(key)) {
            throw new IllegalArgumentException(
                    "Duplicate objective key detected: " + key + ". Each objective must have a unique key!");
        }

        usedObjectiveKeys.add(key);

        ConfigurationSection goalsSection = section.getConfigurationSection("goals");

        List<Goal> goals = GoalHelper.loadGoalsToObjective(objectiveGoalRegistry, goalsSection);

        List<PotionEffect> potionEffects = section.getMapList("potionEffects")
                .stream()
                .map(this::parsePotionEffect)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        String typeString = section.getString("type");
        ObjectiveType objectiveType = objectiveRegistry.getObjectiveType(typeString);

        String description = section.getString("description", "");

        return new Objective(key, objectiveType, potionEffects, goals, description);
    }

    private PotionEffect parsePotionEffect(Map<?, ?> effectMap) {
        try {
            String typeString = (String) effectMap.get("type");
            PotionEffectType type = PotionEffectType.getByName(typeString);
            if (type == null) {
                throw new IllegalArgumentException("Invalid potion effect type: " + typeString);
            }

            int duration = (int) effectMap.get("duration");
            int amplifier = (int) effectMap.get("amplifier");

            return new PotionEffect(type, duration, amplifier);
        } catch (Exception e) {
            System.err.println("Failed to parse potion effect: " + e.getMessage());
            return null;
        }
    }
}
