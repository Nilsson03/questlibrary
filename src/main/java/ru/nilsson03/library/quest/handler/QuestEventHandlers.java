package ru.nilsson03.library.quest.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
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
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemBreakEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerShearEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import ru.nilsson03.library.bukkit.util.log.ConsoleLogger;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.handler.handlers.impl.UniversalQuestEventHandler;
import ru.nilsson03.library.quest.handler.wrapper.TntIgniteWrapper;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

public class QuestEventHandlers {

    private static final NamespacedKey TRANSFORM_PLAYER_KEY = new NamespacedKey("questlibrary", "transform_player");
    private static final NamespacedKey TNT_IGNITER_KEY = new NamespacedKey("questlibrary", "tnt_igniter");

    private final QuestEventManager eventManager;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveRegistry objectiveRegistry;

    public QuestEventHandlers(
            QuestEventManager eventManager, QuestUsersStorage questUsersStorage,
            ObjectiveRegistry objectiveRegistry) {
        this.eventManager = eventManager;
        this.questUsersStorage = questUsersStorage;
        this.objectiveRegistry = objectiveRegistry;
    }

    protected void registerHandlers() {

        QuestEventHandler<PlayerExpChangeEvent> expChangeHandler = new UniversalQuestEventHandler<>(questUsersStorage,
                (event, questUserData) -> {
                    int amount = event.getAmount();
                    questUserData.incrementProgressQuestsWithValueGoals(
                            objectiveRegistry.getObjectiveType(
                                    "EXP_CHANGE"),
                            amount);
                });

        QuestEventHandler<BlockBreakEvent> blockBreakHandler = new UniversalQuestEventHandler<>(questUsersStorage,
                (event, questUserData) -> {
                    Material blockType = event.getBlock()
                            .getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType(
                                    "BREAK_BLOCK"),
                            blockType,
                            1);
                });

        QuestEventHandler<PlayerInteractEntityEvent> transformEntityTrackerHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Entity entity = event.getRightClicked();
                    ItemStack item = event.getPlayer().getInventory().getItem(event.getHand());

                    if (item.getType().isAir())
                        return;

                    if (entity instanceof ZombieVillager && item.getType() == Material.GOLDEN_APPLE) {
                        entity.getPersistentDataContainer().set(
                                TRANSFORM_PLAYER_KEY,
                                PersistentDataType.STRING,
                                event.getPlayer().getUniqueId().toString());
                    }
                });

        QuestEventHandler<EntityTransformEvent> entityTransformHandler = (event) -> {
            Entity originalEntity = event.getEntity();
            EntityType transformedType = event.getTransformedEntity().getType();

            if (originalEntity.getPersistentDataContainer().has(TRANSFORM_PLAYER_KEY, PersistentDataType.STRING)) {
                String playerUuidString = originalEntity.getPersistentDataContainer()
                        .get(TRANSFORM_PLAYER_KEY, PersistentDataType.STRING);

                if (playerUuidString != null) {
                    try {
                        java.util.UUID playerUuid = java.util.UUID.fromString(playerUuidString);
                        QuestUserData questUserData = questUsersStorage.getQuestUserData(playerUuid);

                        if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                                objectiveRegistry.getObjectiveType("TRANSFORM_ENTITY"))) {
                            questUserData.incrementProgressQuestsWithObjectiveType(
                                    objectiveRegistry.getObjectiveType("TRANSFORM_ENTITY"),
                                    transformedType, 1);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        };

        QuestEventHandler<EntityDamageByEntityEvent> blockSchieldEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                            objectiveRegistry.getObjectiveType("BLOCK_DAMAGE_SHIELD"))) {
                        Entity entity = event.getEntity();
                        if (entity instanceof Player player) {
                            if (player.isBlocking()) {
                                int damage = (int) event.getDamage();
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("BLOCK_DAMAGE_SHIELD"), damage);
                            }
                        }
                    }
                });

        QuestEventHandler<CraftItemEvent> craftItemEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    ItemStack itemStack = event.getCurrentItem();
                    if (itemStack == null)
                        return;

                    Material material = itemStack.getType();
                    int amount;

                    if (event.isShiftClick()) {
                        amount = getShiftClickAmount(event);
                    } else {
                        amount = itemStack.getAmount();
                    }

                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("CRAFT_ITEM"),
                            material, amount);
                });

        QuestEventHandler<BlockPlaceEvent> blockPlaceEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Material type = event.getBlock()
                            .getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("BLOCK_PLACE"),
                            type, 1);
                });

        QuestEventHandler<EntityTameEvent> entityTameEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("TAME_ENTITY"),
                            entityType, 1);
                });

        QuestEventHandler<PlayerItemBreakEvent> playerItemBreakEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Material material = event.getBrokenItem().getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("ITEM_DESTROY"),
                            material, 1);
                });

        QuestEventHandler<InventoryClickEvent> inventoryClickEventQuestEventHandlerAnvil = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Inventory inventory = event.getInventory();
                    if (inventory.getType() == InventoryType.ANVIL
                            && event.getSlotType() == InventoryType.SlotType.RESULT) {
                        questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ANVIL"),
                                1);
                    }
                });

        QuestEventHandler<FurnaceExtractEvent> furnaceExtractEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Material material = event.getItemType();
                    int amount = event.getItemAmount();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("SMELT_ITEM"),
                            material, amount);
                });

        QuestEventHandler<EnchantItemEvent> enchantItemEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ENCHANT"),
                            1);
                });

        QuestEventHandler<InventoryClickEvent> villagerTradeEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.MERCHANT
                            && event.getSlotType() == InventoryType.SlotType.RESULT
                            && event.getCurrentItem() != null
                            && !event.getCurrentItem().getType().isAir()) {
                        questUserData.incrementProgressQuestsWithValueGoals(
                                objectiveRegistry.getObjectiveType("TRADE_VILLAGER"),
                                1);
                    }
                });

        QuestEventHandler<PlayerItemConsumeEvent> playerItemConsumeEventQuestEventHandlerDrinkPotion = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    ItemStack itemStack = event.getItem().clone();

                    if (QuestEventHandlersUtil.isDrink(itemStack.getType())) {
                        questUserData.incrementProgressQuestsWithObjectiveType(
                                objectiveRegistry.getObjectiveType("DRINK_POTION"), itemStack, 1);
                    }
                });

        QuestEventHandler<PlayerItemConsumeEvent> playerItemConsumeEventQuestEventHandlerEat = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    ItemStack itemStack = event.getItem();
                    Material material = itemStack.getType();

                    if (material.isEdible() && !QuestEventHandlersUtil.isDrink(material)) {
                        questUserData.incrementProgressQuestsWithObjectiveType(
                                objectiveRegistry.getObjectiveType("EAT_ITEM"),
                                material, 1);
                    }
                });

        QuestEventHandler<PlayerFishEvent> playerFishEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getCaught() != null && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                        if (event.getCaught() instanceof Item item) {
                            ItemStack itemStack = item.getItemStack();
                            Material type = itemStack.getType();
                            if (type == Material.COD ||
                                    type == Material.SALMON ||
                                    type == Material.TROPICAL_FISH ||
                                    type == Material.PUFFERFISH) {
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("CATCH_FISH"),
                                        itemStack.getAmount());
                            }
                        }
                    }
                });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerKillEntity = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("KILL_ENTITY"),
                            entityType, 1);
                });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerDeath = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("DEATH"), 1);
                });

        QuestEventHandler<EntityDamageByEntityEvent> blockShieldHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getEntity() instanceof Player defender) {
                        if (defender.isBlocking()) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("BLOCK_SHIELD"), 1);
                        }
                    }
                });

        QuestEventHandler<EntityBreedEvent> breedEntityHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(
                            objectiveRegistry.getObjectiveType("BREED_ENTITY"), entityType, 1);
                });

        QuestEventHandler<EntityTransformEvent> cureVillagerHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
                        if (event.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("CURE_VILLAGER"), 1);
                        }
                    }
                });

        QuestEventHandler<EntityResurrectEvent> useTotemHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getEntity() instanceof Player) {
                        questUserData.incrementProgressQuestsWithValueGoals(
                                objectiveRegistry.getObjectiveType("USE_TOTEM"), 1);
                    }
                });

        QuestEventHandler<PlayerShearEntityEvent> shearSheepHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(
                            objectiveRegistry.getObjectiveType("SHEAR_SHEEP"), 1);
                });

        QuestEventHandler<PlayerInteractEvent> fillComposterHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND)
                        return;
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                    Block block = event.getClickedBlock();
                    if (block != null && block.getType() == Material.COMPOSTER) {
                        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                        if (item != null && item.getType() != Material.AIR) {
                            Levelled levelledBefore = (Levelled) block.getBlockData();
                            int levelBefore = levelledBefore.getLevel();
                            
                            ConsoleLogger.info(eventManager.getPlugin().getName(), 
                                "Player %s attempting to fill composter with %s, current level: %d",
                                event.getPlayer().getName(), item.getType(), levelBefore);
                            
                            if (QuestEventHandlersUtil.isCompostable(item.getType())) {
                                org.bukkit.Bukkit.getScheduler().runTaskLater(eventManager.getPlugin(), () -> {
                                    if (block.getType() == Material.COMPOSTER) {
                                        Levelled levelledAfter = (Levelled) block.getBlockData();
                                        int levelAfter = levelledAfter.getLevel();
                                        
                                        if (levelAfter > levelBefore) {
                                            ConsoleLogger.info(eventManager.getPlugin().getName(), 
                                                "Composter level increased from %d to %d, incrementing FILL_COMPOSTER progress", 
                                                levelBefore, levelAfter);
                                            questUserData.incrementProgressQuestsWithValueGoals(
                                                    objectiveRegistry.getObjectiveType("FILL_COMPOSTER"), 1);
                                        } else {
                                            ConsoleLogger.warn(eventManager.getPlugin().getName(), 
                                                "Composter level did not increase (still %d), item was not accepted", levelAfter);
                                        }
                                    }
                                }, 1L);
                            } else {
                                ConsoleLogger.warn(eventManager.getPlugin().getName(), 
                                    "Item %s is NOT compostable, skipping FILL_COMPOSTER progress", item.getType());
                            }
                        }
                    }
                });

        QuestEventHandler<EnchantItemEvent> enchantWithLevelHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    ItemStack enchantedItem = event.getItem();
                    if (enchantedItem != null) {
                        questUserData.incrementProgressQuestsWithObjectiveType(
                                objectiveRegistry.getObjectiveType("ENCHANT_WITH_LEVEL"), enchantedItem, 1);
                    }
                });

        QuestEventHandler<InventoryClickEvent> anvilEnchantHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.ANVIL) {
                        if (event.getSlotType() == InventoryType.SlotType.RESULT &&
                                event.getCurrentItem() != null &&
                                event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack result = event.getCurrentItem();
                            if (!result.getEnchantments().isEmpty()) {
                                questUserData.incrementProgressQuestsWithObjectiveType(
                                        objectiveRegistry.getObjectiveType("ENCHANT_WITH_LEVEL"), result, 1);
                            }
                        }
                    }
                });

        QuestEventHandler<PlayerInteractEvent> collectFromComposterHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND)
                        return;
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                    Block block = event.getClickedBlock();
                    if (block != null && block.getType() == Material.COMPOSTER) {
                        Levelled levelled = (Levelled) block.getBlockData();
                        int level = levelled.getLevel();
                        
                        ConsoleLogger.info(eventManager.getPlugin().getName(), 
                            "Player %s interacting with composter, level: %d",
                            event.getPlayer().getName(), level);
                        
                        if (level == 8) {
                            ConsoleLogger.info(eventManager.getPlugin().getName(), 
                                "Composter is full (level 8), incrementing COLLECT_FROM_COMPOSTER progress");
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("COLLECT_FROM_COMPOSTER"), 1);
                        } else {
                            ConsoleLogger.info(eventManager.getPlugin().getName(), 
                                "Composter is not full (level %d), skipping COLLECT_FROM_COMPOSTER progress", level);
                        }
                    }
                });

        QuestEventHandler<BlockPlaceEvent> dragonResurrectHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    Block placedBlock = event.getBlockPlaced();

                    if (placedBlock.getType() != Material.END_CRYSTAL) {
                        return;
                    }
                    
                    Block blockBelow = placedBlock.getRelative(0, -1, 0);
                    if (blockBelow.getType() != Material.END_PORTAL_FRAME) {
                        return;
                    }
                    
                    World world = placedBlock.getWorld();
                    if (world.getEnvironment() != World.Environment.THE_END) {
                        return;
                    }
                    
                    ConsoleLogger.info(eventManager.getPlugin().getName(), 
                        "Player %s placed End Crystal on portal frame at %s", 
                        event.getPlayer().getName(), placedBlock.getLocation());
                    
                    // Получаем центр портала (0, Y, 0 в мире Края)
                    Location portalCenter = new Location(world, 0, blockBelow.getY(), 0);
                    
                    // Проверяем, установлены ли все 4 кристалла
                    if (areFourCrystalsPlaced(portalCenter)) {
                        ConsoleLogger.info(eventManager.getPlugin().getName(), 
                            "All 4 End Crystals placed! Player %s resurrected the dragon", 
                            event.getPlayer().getName());
                        
                        questUserData.incrementProgressQuestsWithValueGoals(
                                objectiveRegistry.getObjectiveType("RESURRECT_DRAGON"), 1);
                    } else {
                        ConsoleLogger.info(eventManager.getPlugin().getName(), 
                            "Not all 4 crystals placed yet");
                    }
                });

        QuestEventHandler<EntityExplodeEvent> tntBreakBlocksHandler = (event) -> {
            if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
                Entity tnt = event.getEntity();
                int blockCount = event.blockList().size();
                ConsoleLogger.info(eventManager.getPlugin().getName(), "TNT exploded! Blocks destroyed: %d", blockCount);

                if (blockCount > 0
                        && tnt.getPersistentDataContainer().has(TNT_IGNITER_KEY, PersistentDataType.STRING)) {
                    String playerUuidString = tnt.getPersistentDataContainer().get(TNT_IGNITER_KEY,
                            PersistentDataType.STRING);
                    ConsoleLogger.info(eventManager.getPlugin().getName(), "Found igniter UUID: %s", playerUuidString);

                    if (playerUuidString != null) {
                        try {
                            UUID playerUuid = UUID.fromString(playerUuidString);
                            QuestUserData questUserData = questUsersStorage.getQuestUserData(playerUuid);

                            if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                                    objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"))) {
                                ConsoleLogger.info(eventManager.getPlugin().getName(), "Incrementing TNT_BREAK_BLOCKS progress by %d", blockCount);
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"), blockCount);
                            } else {
                                ConsoleLogger.warn(eventManager.getPlugin().getName(), "Player has no active TNT_BREAK_BLOCKS quest");
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                } else {
                    ConsoleLogger.warn(eventManager.getPlugin().getName(), "TNT has no igniter UUID or destroyed 0 blocks");
                }
            }
        };


        QuestEventHandler<InventoryClickEvent> useGrindstoneHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.GRINDSTONE) {
                        if (event.getSlotType() == InventoryType.SlotType.RESULT &&
                                event.getCurrentItem() != null &&
                                event.getCurrentItem().getType() != Material.AIR) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("USE_GRINDSTONE_ITEM"), 1);
                        }
                    }
                });

        QuestEventHandler<FurnaceExtractEvent> useFurnaceHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(
                            objectiveRegistry.getObjectiveType("USE_FURNACE"), 1);
                });

        Map<String, QuestEventHandler<?>> handlers = Map.copyOf(new HashMap<>() {
            {
                put("EXP_CHANGE", expChangeHandler);
                put("BREAK_BLOCK", blockBreakHandler);
                put("TRANSFORM_ENTITY", entityTransformHandler);
                put("CRAFT_ITEM", craftItemEventQuestEventHandler);
                put("BLOCK_PLACE", blockPlaceEventQuestEventHandler);
                put("TAME_ENTITY", entityTameEventQuestEventHandler);
                put("ITEM_DESTROY", playerItemBreakEventQuestEventHandler);
                put("ANVIL", inventoryClickEventQuestEventHandlerAnvil);
                put("SMELT_ITEM", furnaceExtractEventQuestEventHandler);
                put("ENCHANT", enchantItemEventQuestEventHandler);
                put("TRADE_VILLAGER", villagerTradeEventQuestEventHandler);
                put("DRINK_POTION", playerItemConsumeEventQuestEventHandlerDrinkPotion);
                put("EAT_ITEM", playerItemConsumeEventQuestEventHandlerEat);
                put("CATCH_FISH", playerFishEventQuestEventHandler);
                put("KILL_ENTITY", entityDeathEventQuestEventHandlerKillEntity);
                put("DEATH", entityDeathEventQuestEventHandlerDeath);
                put("BLOCK_SHIELD", blockShieldHandler);
                put("BREED_ENTITY", breedEntityHandler);
                put("CURE_VILLAGER", cureVillagerHandler);
                put("USE_TOTEM", useTotemHandler);
                put("SHEAR_SHEEP", shearSheepHandler);
                put("FILL_COMPOSTER", fillComposterHandler);
                put("ENCHANT_WITH_LEVEL", enchantWithLevelHandler);
                put("ANVIL_ENCHANT_HANDLER", anvilEnchantHandler);
                put("RESURRECT_DRAGON", dragonResurrectHandler);
                put("TNT_BREAK_BLOCKS", tntBreakBlocksHandler);
                put("COLLECT_FROM_COMPOSTER", collectFromComposterHandler);
                put("USE_GRINDSTONE_ITEM", useGrindstoneHandler);
                put("USE_FURNACE", useFurnaceHandler);
                put("BLOCK_DAMAGE_SHIELD", blockSchieldEventHandler);
            }
        });

        eventManager.registerHandler(PlayerInteractEntityEvent.class, transformEntityTrackerHandler);

        TntIgniteWrapper tntIgniteWrapper = new TntIgniteWrapper(
                eventManager, questUsersStorage, objectiveRegistry, eventManager.getPlugin());
        tntIgniteWrapper.registerHandlers();

        for (Map.Entry<String, QuestEventHandler<?>> entry : handlers.entrySet()) {
            if (entry.getKey().equals("ANVIL_ENCHANT_HANDLER")) {
                eventManager.registerHandler(InventoryClickEvent.class, entry.getValue());
            } else {
                eventManager.registerHandler(objectiveRegistry.getObjectiveType(entry.getKey()), entry.getValue());
            }
        }
    }

    private int getShiftClickAmount(CraftItemEvent event) {
        int maxCraftable = 64;

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && !ingredient.getType().isAir()) {
                maxCraftable = Math.min(maxCraftable, ingredient.getAmount());
            }
        }

        return maxCraftable * event.getRecipe().getResult().getAmount();
    }

    private boolean areFourCrystalsPlaced(Location portalCenter) {
        int[][] offsets = {
            {3, 0, 0},   // Восток
            {-3, 0, 0},  // Запад
            {0, 0, 3},   // Юг
            {0, 0, -3}   // Север
        };

        int crystalsFound = 0;

        for (int[] offset : offsets) {
            Location crystalLoc = portalCenter.clone().add(offset[0], offset[1], offset[2]);
            Block block = crystalLoc.getBlock();

            if (block.getType() == Material.END_CRYSTAL) {
                crystalsFound++;
                ConsoleLogger.info(eventManager.getPlugin().getName(),
                    "Found End Crystal at offset [%d, %d, %d]", offset[0], offset[1], offset[2]);
            }
        }

        ConsoleLogger.info(eventManager.getPlugin().getName(),
            "Total End Crystals found: %d/4", crystalsFound);

        return crystalsFound == 4;
    }
}
