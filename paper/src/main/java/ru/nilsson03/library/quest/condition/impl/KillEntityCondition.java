package ru.nilsson03.library.quest.condition.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class KillEntityCondition implements QuestCondition {

    private final Set<String> requiredWeapons;
    private final Set<String> requiredArmor;
    private final Map<PotionEffectType, Integer> requiredEffects;
    private final Double minDistance;
    private final Double maxDistance;
    private final boolean requireNoAggro;

    public KillEntityCondition(
            Set<String> requiredWeapons,
            Set<String> requiredArmor,
            Map<PotionEffectType, Integer> requiredEffects,
            Double minDistance,
            Double maxDistance,
            boolean requireNoAggro) {
        this.requiredWeapons = requiredWeapons;
        this.requiredArmor = requiredArmor;
        this.requiredEffects = requiredEffects;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.requireNoAggro = requireNoAggro;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        return true;
    }

    public boolean checkKillConditions(Player killer, Entity victim, double distance) {
        if (requiredWeapons != null && !requiredWeapons.isEmpty()) {
            ItemStack weapon = killer.getInventory().getItemInMainHand();
            if (weapon == null || !requiredWeapons.contains(weapon.getType().name())) {
                return false;
            }
        }

        if (requiredArmor != null && !requiredArmor.isEmpty()) {
            List<String> equippedArmor = Arrays.stream(killer.getInventory().getArmorContents())
                    .filter(Objects::nonNull)
                    .map(armor -> armor.getType().name())
                    .collect(Collectors.toList());

            if (!equippedArmor.containsAll(requiredArmor)) {
                return false;
            }
        }

        if (requiredEffects != null && !requiredEffects.isEmpty()) {
            for (Map.Entry<PotionEffectType, Integer> entry : requiredEffects.entrySet()) {
                PotionEffectType requiredType = entry.getKey();
                int requiredLevel = entry.getValue();

                PotionEffect currentEffect = killer.getPotionEffect(requiredType);

                if (currentEffect == null) {
                    return false;
                }

                if (currentEffect.getAmplifier() < requiredLevel) {
                    return false;
                }
            }
        }

        if (minDistance != null && distance < minDistance) {
            return false;
        }

        if (maxDistance != null && distance > maxDistance) {
            return false;
        }

        if (requireNoAggro && victim instanceof LivingEntity) {
            return false;
        }

        return true;
    }
}
