package ru.nilsson03.library.quest.objective.registry;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectiveRegistry {

    private final Map<String, ObjectiveType> objectiveTypes = new ConcurrentHashMap<>();

    private final ObjectiveParser objectiveParser;
    private final ObjectiveGoalFactoryRegistry objectiveGoalRegistry;

    public ObjectiveRegistry() {
        this.objectiveGoalRegistry = new ObjectiveGoalFactoryRegistry();
        this.objectiveGoalRegistry.onRegisterInit();
        this.objectiveParser = new ObjectiveParser(this, objectiveGoalRegistry);
    }

    /**
     * Регистрирует новый тип задачи.
     */
    public void registerObjectiveType(ObjectiveType type) {
        Objects.requireNonNull(type, "ObjectiveType cannot be null");
        objectiveTypes.putIfAbsent(type.key()
                                       .toLowerCase(), type);
    }

    /**
     * Возвращает тип задачи по ключу.
     */
    public ObjectiveType getObjectiveType(String key) {
        Objects.requireNonNull(key, "Key cannot be null");

        return objectiveTypes.computeIfAbsent(key.toLowerCase(), k -> {
            throw new IllegalArgumentException("No such objective type: " + key);
        });
    }

    public void onRegistryInit() {
        registerObjectiveType(ObjectiveType.create("EXP_CHANGE", PlayerExpChangeEvent.class));
        registerObjectiveType(ObjectiveType.create("TRANSFORM_ENTITY", EntityTransformEvent.class));
        registerObjectiveType(ObjectiveType.create("BREAK_BLOCK", BlockBreakEvent.class));
        //        registerObjectiveType(ObjectiveType.create("GAIN_EXP", PlayerPickupExperienceEvent.class));
        registerObjectiveType(ObjectiveType.create("CRAFT_ITEM", CraftItemEvent.class));
        registerObjectiveType(ObjectiveType.create("SMELT_ITEM", InventoryClickEvent.class));
        registerObjectiveType(ObjectiveType.create("BLOCK_PLACE", BlockPlaceEvent.class));
        registerObjectiveType(ObjectiveType.create("EAT_ITEM", PlayerItemConsumeEvent.class));
        registerObjectiveType(ObjectiveType.create("TAME_ENTITY", EntityTameEvent.class));
        registerObjectiveType(ObjectiveType.create("RIDE_HORSE", PlayerMoveEvent.class));
        registerObjectiveType(ObjectiveType.create("ITEM_DESTROY", PlayerItemBreakEvent.class));
        registerObjectiveType(ObjectiveType.create("ANVIL", InventoryClickEvent.class));
        registerObjectiveType(ObjectiveType.create("ENCHANT", EnchantItemEvent.class));
        registerObjectiveType(ObjectiveType.create("MOVE", PlayerMoveEvent.class));
        registerObjectiveType(ObjectiveType.create("TRADE_VILLAGER", PlayerInteractEntityEvent.class));
        registerObjectiveType(ObjectiveType.create("DRINK_POTION", PlayerItemConsumeEvent.class));
        registerObjectiveType(ObjectiveType.create("CATCH_FISH", PlayerFishEvent.class));
        registerObjectiveType(ObjectiveType.create("DEATH", EntityDeathEvent.class));
        registerObjectiveType(ObjectiveType.create("KILL_ENTITY", EntityDeathEvent.class));
    }

    public ObjectiveParser getObjectiveParser() {
        return objectiveParser;
    }

    public ObjectiveGoalFactoryRegistry getObjectiveGoalRegistry() {
        return objectiveGoalRegistry;
    }
}
