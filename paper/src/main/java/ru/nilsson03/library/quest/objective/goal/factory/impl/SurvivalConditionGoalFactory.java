package ru.nilsson03.library.quest.objective.goal.factory.impl;

import org.bukkit.block.Biome;
import org.bukkit.potion.PotionEffectType;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.SurvivalConditionGoal;

import java.util.Map;

public class SurvivalConditionGoalFactory implements ObjectiveGoalFactory {
    
    @Override
    public Goal create(Map<String, Object> parameters) {
        long targetValue = Long.parseLong(parameters.get("value").toString());
        
        PotionEffectType requiredEffect = null;
        if (parameters.containsKey("effect")) {
            String effectName = parameters.get("effect").toString().toUpperCase();
            requiredEffect = PotionEffectType.getByName(effectName);
        }
        
        String requiredWorld = null;
        if (parameters.containsKey("world")) {
            requiredWorld = parameters.get("world").toString();
        }
        
        Biome requiredBiome = null;
        if (parameters.containsKey("biome")) {
            try {
                String biomeName = parameters.get("biome").toString().toUpperCase();
                requiredBiome = Biome.valueOf(biomeName);
            } catch (IllegalArgumentException e) {
            }
        }
        
        return new SurvivalConditionGoal(requiredEffect, requiredWorld, requiredBiome, targetValue);
    }
}
