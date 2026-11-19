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
            for (String goalKey : goalsSection.getKeys(false)) {
                ConfigurationSection goalSection = goalsSection.getConfigurationSection(goalKey);
                if (goalSection == null) {
                    throw new IllegalArgumentException("Некорректный формат цели: " + goalKey);
                }

                String goalType = goalSection.getString("type");
                if (goalType == null || goalType.trim().isEmpty()) {
                    throw new IllegalArgumentException("Тип цели не указан для: " + goalKey);
                }

                Optional<ObjectiveGoalFactory> factoryOptional = objectiveGoalRegistry.getFactory(goalType);
                if (factoryOptional.isEmpty()) {
                    throw new IllegalArgumentException("Неизвестный тип цели: " + goalType);
                }

                var factory = factoryOptional.get();

                Map<String, Object> parameters = new HashMap<>();
                for (String key : goalSection.getKeys(false)) {
                    if (!key.equals("type")) {
                        parameters.put(key, goalSection.get(key));
                    }
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
                if (parts.length < 2) {
                    continue; // некорректный формат
                }
                String goalType = parts[0].toLowerCase(); // "material" или "value"

                Optional<ObjectiveGoalFactory> factoryOptional = objectiveGoalRegistry.getFactory(goalType);
                if (factoryOptional.isEmpty()) continue;

                Map<String, Object> parameters = new HashMap<>();
                String payload = parts[1];
                String[] args = goalType.equals("value")
                        ? new String[]{payload}
                        : payload.split("-");

                try {
                    if (goalType.equals("value")) {
                        parameters.put("value", Long.parseLong(args[0]));
                    } else if (args.length >= 2) {
                        parameters.put(goalType, args[0]);
                        parameters.put("value", Long.parseLong(args[1]));
                    } else {
                        continue;
                    }
                } catch (NumberFormatException exception) {
                    continue;
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
