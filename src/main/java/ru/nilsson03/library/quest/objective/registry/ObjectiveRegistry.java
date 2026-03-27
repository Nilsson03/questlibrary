package ru.nilsson03.library.quest.objective.registry;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;

import ru.nilsson03.library.quest.objective.goal.registry.ObjectiveGoalFactoryRegistry;
import ru.nilsson03.library.quest.objective.parser.ObjectiveParser;

public class ObjectiveRegistry {

    private final Map<String, ObjectiveType> objectiveTypes = new ConcurrentHashMap<>();

    private final ObjectiveParser objectiveParser;
    private final ObjectiveGoalFactoryRegistry objectiveGoalRegistry;

    public ObjectiveRegistry() {
        this.objectiveGoalRegistry = new ObjectiveGoalFactoryRegistry();
        this.objectiveGoalRegistry.onRegisterInit();
        this.objectiveParser = new ObjectiveParser(this, objectiveGoalRegistry);
    }

    public void registerObjectiveType(ObjectiveType type) {
        Objects.requireNonNull(type, "ObjectiveType cannot be null");
        objectiveTypes.putIfAbsent(type.key()
                .toLowerCase(), type);
    }

    public ObjectiveType getObjectiveType(String key) {
        Objects.requireNonNull(key, "Key cannot be null");

        return objectiveTypes.computeIfAbsent(key.toLowerCase(), k -> {
            throw new IllegalArgumentException("No such objective type: " + key);
        });
    }

    public void onRegistryInit() {
        registerObjectiveType(ObjectiveType.create("TRADE_VILLAGER", InventoryClickEvent.class)); // Удалить
        registerObjectiveType(ObjectiveType.create("EXP_CHANGE", PlayerExpChangeEvent.class));
        registerObjectiveType(ObjectiveType.create("TRANSFORM_ENTITY", EntityTransformEvent.class));
        registerObjectiveType(ObjectiveType.create("BREAK_BLOCK", BlockBreakEvent.class));
        registerObjectiveType(ObjectiveType.create("CRAFT_ITEM", CraftItemEvent.class));
        registerObjectiveType(ObjectiveType.create("SMELT_ITEM", FurnaceExtractEvent.class));
        registerObjectiveType(ObjectiveType.create("BLOCK_PLACE", BlockPlaceEvent.class));
        registerObjectiveType(ObjectiveType.create("EAT_ITEM", PlayerItemConsumeEvent.class));
        registerObjectiveType(ObjectiveType.create("TAME_ENTITY", EntityTameEvent.class));
        registerObjectiveType(ObjectiveType.create("RIDE_HORSE", PlayerMoveEvent.class));
        registerObjectiveType(ObjectiveType.create("ITEM_DESTROY", PlayerItemBreakEvent.class));
        registerObjectiveType(ObjectiveType.create("ANVIL", InventoryClickEvent.class));
        registerObjectiveType(ObjectiveType.create("ENCHANT", EnchantItemEvent.class));
        registerObjectiveType(ObjectiveType.create("MOVE", PlayerMoveEvent.class));
        registerObjectiveType(ObjectiveType.create("DRINK_POTION", PlayerItemConsumeEvent.class));
        registerObjectiveType(ObjectiveType.create("CATCH_FISH", PlayerFishEvent.class));
        registerObjectiveType(ObjectiveType.create("DEATH", EntityDeathEvent.class));
        registerObjectiveType(ObjectiveType.create("KILL_ENTITY", EntityDeathEvent.class));
        registerObjectiveType(ObjectiveType.create("BLOCK_SHIELD", EntityDamageByEntityEvent.class));
        registerObjectiveType(ObjectiveType.create("BREED_ENTITY", EntityBreedEvent.class));
        registerObjectiveType(ObjectiveType.create("CURE_VILLAGER", EntityTransformEvent.class));
        registerObjectiveType(ObjectiveType.create("USE_TOTEM", EntityResurrectEvent.class));
        registerObjectiveType(ObjectiveType.create("SHEAR_SHEEP", PlayerShearEntityEvent.class));
        registerObjectiveType(ObjectiveType.create("PLAYTIME", PlayerJoinEvent.class));
        registerObjectiveType(ObjectiveType.create("ENCHANT_WITH_LEVEL", EnchantItemEvent.class));
        registerObjectiveType(ObjectiveType.create("SURVIVAL_CONDITION", PlayerJoinEvent.class));
        registerObjectiveType(ObjectiveType.create("RESURRECT_DRAGON", CreatureSpawnEvent.class));
        registerObjectiveType(ObjectiveType.create("IGNITE_TNT", PlayerInteractEvent.class));
        registerObjectiveType(ObjectiveType.create("TNT_BREAK_BLOCKS", EntityExplodeEvent.class));
        registerObjectiveType(ObjectiveType.create("COLLECT_FROM_COMPOSTER", PlayerInteractEvent.class));
        registerObjectiveType(ObjectiveType.create("USE_GRINDSTONE_ITEM", InventoryClickEvent.class));
        registerObjectiveType(ObjectiveType.create("USE_FURNACE", FurnaceExtractEvent.class));
        registerObjectiveType(ObjectiveType.create("SUBMIT_ITEM", PlayerInteractEvent.class));
        registerObjectiveType(ObjectiveType.create("BLOCK_DAMAGE_SHIELD", EntityDamageByEntityEvent.class));
        registerObjectiveType(ObjectiveType.create("FILL_COMPOSTER", PlayerInteractEvent.class));
    }

    public ObjectiveParser getObjectiveParser() {
        return objectiveParser;
    }

    public ObjectiveGoalFactoryRegistry getObjectiveGoalRegistry() {
        return objectiveGoalRegistry;
    }
}
