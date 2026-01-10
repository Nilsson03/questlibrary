package ru.nightvision.quests.goal.factory;

import java.util.Map;

import org.bukkit.block.Biome;
import org.bukkit.potion.PotionEffectType;

import ru.nightvision.quests.goal.SurvivalConditionGoal;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;

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
