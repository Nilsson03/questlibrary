package ru.nilsson03.library.quest.objective.goal.impl;

import org.bukkit.block.Biome;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.quest.objective.goal.sub.ObjectiveGoal;

public class SurvivalConditionGoal implements ObjectiveGoal {

    private final PotionEffectType requiredEffect;
    private final String requiredWorld;
    private final Biome requiredBiome;
    private final long targetValue;

    public SurvivalConditionGoal(
            PotionEffectType requiredEffect,
            String requiredWorld,
            Biome requiredBiome,
            long targetValue) {
        this.requiredEffect = requiredEffect;
        this.requiredWorld = requiredWorld;
        this.requiredBiome = requiredBiome;
        this.targetValue = targetValue;
    }

    public record SurvivalData(
            PotionEffectType effect,
            String worldName,
            Biome biome) {
    }

    @Override
    public String targetType() {
        return new StringBuilder(requiredEffect.getName())
                .append(" ")
                .append(requiredWorld)
                .append(" ")
                .append(requiredBiome.name())
                .toString();
    }

    @Override
    public boolean matches(Object target) {
        if (!(target instanceof SurvivalData data)) {
            return false;
        }

        if (requiredEffect != null) {
            if (data.effect() == null || !requiredEffect.equals(data.effect())) {
                return false;
            }
        }

        if (requiredWorld != null && data.worldName() != null) {
            if (!requiredWorld.equalsIgnoreCase(data.worldName())) {
                return false;
            }
        }

        if (requiredBiome != null && !requiredBiome.equals(data.biome())) {
            return false;
        }

        return true;
    }

    @Override
    public long targetValue() {
        return targetValue;
    }

    public PotionEffectType getRequiredEffect() {
        return requiredEffect;
    }

    public String getRequiredWorld() {
        return requiredWorld;
    }

    public Biome getRequiredBiome() {
        return requiredBiome;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("SurvivalCondition(");
        if (requiredEffect != null) {
            sb.append("effect=").append(requiredEffect.getName()).append(", ");
        }
        if (requiredWorld != null) {
            sb.append("world=").append(requiredWorld).append(", ");
        }
        if (requiredBiome != null) {
            sb.append("biome=").append(requiredBiome.name()).append(", ");
        }
        sb.append("time=").append(targetValue).append("s)");
        return sb.toString();
    }
}
