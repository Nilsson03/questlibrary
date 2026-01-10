package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.Set;

public class KillEntityCondition implements QuestCondition {

    private final Set<String> requiredWeapons;
    private final Set<String> requiredArmor;
    private final Set<String> requiredEffects;
    private final Double minDistance;
    private final Double maxDistance;
    private final boolean requireNoAggro;

    public KillEntityCondition(
            Set<String> requiredWeapons,
            Set<String> requiredArmor,
            Set<String> requiredEffects,
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
            boolean hasRequiredArmor = false;
            for (ItemStack armor : killer.getInventory().getArmorContents()) {
                if (armor != null && requiredArmor.contains(armor.getType().name())) {
                    hasRequiredArmor = true;
                    break;
                }
            }
            if (!hasRequiredArmor) {
                return false;
            }
        }

        if (requiredEffects != null && !requiredEffects.isEmpty()) {
            boolean hasRequiredEffect = false;
            for (PotionEffect effect : killer.getActivePotionEffects()) {
                if (requiredEffects.contains(effect.getType().getName())) {
                    hasRequiredEffect = true;
                    break;
                }
            }
            if (!hasRequiredEffect) {
                return false;
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
