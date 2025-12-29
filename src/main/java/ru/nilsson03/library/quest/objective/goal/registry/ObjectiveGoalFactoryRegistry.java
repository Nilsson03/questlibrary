package ru.nilsson03.library.quest.objective.goal.registry;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import ru.nilsson03.library.bukkit.util.ItemStackSerialize;
import ru.nilsson03.library.quest.objective.goal.factory.ObjectiveGoalFactory;
import ru.nilsson03.library.quest.objective.goal.impl.EntityTypeGoal;
import ru.nilsson03.library.quest.objective.goal.impl.ItemStackGoal;
import ru.nilsson03.library.quest.objective.goal.impl.MaterialGoal;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ObjectiveGoalFactoryRegistry {

    private static final Map<String, ObjectiveGoalFactory> factories = new HashMap<>();

    /**
     * Регистрирует фабрику для указанного класса цели.
     *
     * @param type    тип цели.
     * @param factory фабрика для создания целей этого типа.
     */
    public void registerFactory(String type, ObjectiveGoalFactory factory) {
        factories.putIfAbsent(type.toLowerCase(), factory);
    }

    public Optional<ObjectiveGoalFactory> getFactory(String type) {
        return Optional.ofNullable(factories.get(type.toLowerCase()));
    }

    public void onRegisterInit() {

        registerFactory("value", parameters -> {
            long targetValue = Long.parseLong(parameters.get("value")
                                                        .toString());
            return new NumericGoal(targetValue);
        });

        registerFactory("entity", parameters -> {
            Object entityTypeObj = parameters.get("entityType");
            EntityType entityType;
            if (entityTypeObj instanceof String) {
                entityType = EntityType.valueOf((String) entityTypeObj);
            } else {
                entityType = (EntityType) entityTypeObj;
            }
            long targetValue = Long.parseLong(parameters.get("value")
                                                        .toString());
            return new EntityTypeGoal(entityType, targetValue);
        });

        registerFactory("itemStack", parameters -> {
            Object itemStackObj = parameters.get("itemStack");
            ItemStack itemStack;
            if (itemStackObj instanceof String) {
                itemStack = ItemStackSerialize.deserialize(itemStackObj.toString())
                                                .orElse(null);
            } else {
                itemStack = (ItemStack) itemStackObj;
            }
            long targetValue = Long.parseLong(parameters.get("value")
                                                        .toString());
            return new ItemStackGoal(itemStack, targetValue);
        });

        registerFactory("material", parameters -> {
            Object materialObj = parameters.get("material");
            Material material;
            if (materialObj instanceof String) {
                material = Material.valueOf((String) materialObj);
            } else {
                material = (Material) materialObj;
            }
            long targetValue = Long.parseLong(parameters.get("value")
                                                        .toString());
            return new MaterialGoal(material, targetValue);
        });
    }
}
