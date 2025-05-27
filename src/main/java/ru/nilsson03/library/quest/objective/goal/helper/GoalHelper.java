package ru.nilsson03.library.quest.objective.goal.helper;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;

import java.util.*;

public class GoalHelper {

    public static List<Goal> loadGoalsToObjective(
            ObjectiveGoalFactoryRegistry objectiveGoalRegistry, ConfigurationSection goalsSection) {
        List<Goal> goals = new ArrayList<>();

        if (goalsSection != null) {
            for (String goalType : goalsSection.getKeys(false)) {
                ConfigurationSection goalSection = goalsSection.getConfigurationSection(goalType);
                if (goalSection == null) {
                    throw new IllegalArgumentException("Некорректный формат цели: " + goalType);
                }

                Optional<ObjectiveGoalFactory> factoryOptional = objectiveGoalRegistry.getFactory(goalType);
                if (factoryOptional.isEmpty()) {
                    throw new IllegalArgumentException("Неизвестный тип цели: " + goalType);
                }

                var factory = factoryOptional.get();

                Map<String, Object> parameters = new HashMap<>();
                for (String key : goalSection.getKeys(false)) {
                    parameters.put(key, goalSection.get(key));
                }

                Goal goal = factory.create(parameters);
                goals.add(goal);
            }
        }

        return goals;
    }

    public static Map<Goal, Long> loadGoalsToProgress(
            ObjectiveGoalFactoryRegistry objectiveGoalRegistry, ConfigurationSection goalsSection) {
        Map<Goal, Long> goals = new HashMap<>();

        if (goalsSection != null) {
            for (String goalString : goalsSection.getKeys(false)) {
                ConfigurationSection goalSection = goalsSection.getConfigurationSection(goalString);
                if (goalSection == null) continue;

                String[] parts = goalString.split("[()]");
                String goalType = parts[0].toLowerCase(); // "material"

                Optional<ObjectiveGoalFactory> factoryOptional = objectiveGoalRegistry.getFactory(goalType);
                if (factoryOptional.isEmpty()) continue;

                Map<String, Object> parameters = new HashMap<>();
                String[] args = parts[1].split("-"); // ["DIAMOND", "10"]

                if (goalType.equalsIgnoreCase("Value")) {
                    parameters.put("value", Long.parseLong(args[1]));
                } else {
                    parameters.put(goalType, args[0]);
                    parameters.put("value", Long.parseLong(args[1]));
                }

                Goal goal = factoryOptional.get().create(parameters);
                goals.put(goal, goalSection.getLong("value"));
            }
        }

        return goals;
    }

    public static void saveGoalsFromProgress(Map<Goal, Long> progress, ConfigurationSection section) {
        for (Map.Entry<Goal, Long> entry : progress.entrySet()) {
            Goal goal = entry.getKey();
            Long value = entry.getValue();
            ConfigurationSection goalSection = section.createSection(goal.toString());
            goalSection.set("value", value);
        }
    }
}
