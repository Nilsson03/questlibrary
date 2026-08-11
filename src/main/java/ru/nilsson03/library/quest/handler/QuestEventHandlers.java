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
import ru.nilsson03.library.quest.core.progress.ProgressTargetResolver;
import ru.nilsson03.library.quest.user.data.QuestUserData;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

public class QuestEventHandlers {

    private static final NamespacedKey TRANSFORM_PLAYER_KEY = new NamespacedKey("questlibrary", "transform_player");
    private static final NamespacedKey TNT_IGNITER_KEY = new NamespacedKey("questlibrary", "tnt_igniter");

    private final QuestEventManager eventManager;
    private final QuestUsersStorage questUsersStorage;
    private final ObjectiveRegistry objectiveRegistry;
    private final ProgressTargetResolver progressTargetResolver;

    public QuestEventHandlers(
            QuestEventManager eventManager, QuestUsersStorage questUsersStorage,
            ObjectiveRegistry objectiveRegistry) {
        this(eventManager, questUsersStorage, objectiveRegistry, ProgressTargetResolver.identity(questUsersStorage));
    }

    public QuestEventHandlers(
            QuestEventManager eventManager, QuestUsersStorage questUsersStorage,
            ObjectiveRegistry objectiveRegistry, ProgressTargetResolver progressTargetResolver) {
        this.eventManager = eventManager;
        this.questUsersStorage = questUsersStorage;
        this.objectiveRegistry = objectiveRegistry;
        this.progressTargetResolver = progressTargetResolver != null
                ? progressTargetResolver
                : ProgressTargetResolver.identity(questUsersStorage);
    }

    private <T extends org.bukkit.event.Event> UniversalQuestEventHandler<T> universal(
            UniversalQuestEventHandler.QuestEventProgressLogic<T> logic) {
        return new UniversalQuestEventHandler<>(progressTargetResolver, logic);
    }

    protected void registerHandlers() {

        QuestEventHandler<PlayerExpChangeEvent> expChangeHandler = universal((event, actor, questUserData) -> {
                    int amount = event.getAmount();
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType(
                                    "EXP_CHANGE"), amount, actor);
                });

        QuestEventHandler<BlockBreakEvent> blockBreakHandler = universal((event, actor, questUserData) -> {
                    Material blockType = event.getBlock()
                            .getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType(
                                    "BREAK_BLOCK"), blockType, 1, actor);
                });

        QuestEventHandler<PlayerInteractEntityEvent> transformEntityTrackerHandler = universal((event, actor, questUserData) -> {
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
                        Player actor = org.bukkit.Bukkit.getPlayer(playerUuid);
                        if (actor == null) {
                            return;
                        }
                        QuestUserData questUserData = progressTargetResolver.resolve(actor);

                        if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                                objectiveRegistry.getObjectiveType("TRANSFORM_ENTITY"))) {
                            questUserData.incrementProgressQuestsWithObjectiveType(
                                    objectiveRegistry.getObjectiveType("TRANSFORM_ENTITY"),
                                    transformedType, 1, actor);
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
        };

        QuestEventHandler<EntityDamageByEntityEvent> blockSchieldEventHandler = universal((event, actor, questUserData) -> {
                    if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                            objectiveRegistry.getObjectiveType("BLOCK_DAMAGE_SHIELD"))) {
                        Entity entity = event.getEntity();
                        if (entity instanceof Player player) {
                            if (player.isBlocking()) {
                                int damage = (int) event.getDamage();
                                questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("BLOCK_DAMAGE_SHIELD"), damage, actor);
                            }
                        }
                    }
                });

        QuestEventHandler<CraftItemEvent> craftItemEventQuestEventHandler = universal((event, actor, questUserData) -> {
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

                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("CRAFT_ITEM"), material, amount, actor);
                });

        QuestEventHandler<BlockPlaceEvent> blockPlaceEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    Material type = event.getBlock()
                            .getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("BLOCK_PLACE"), type, 1, actor);
                });

        QuestEventHandler<EntityTameEvent> entityTameEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("TAME_ENTITY"), entityType, 1, actor);
                });

        QuestEventHandler<PlayerItemBreakEvent> playerItemBreakEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    Material material = event.getBrokenItem().getType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("ITEM_DESTROY"), material, 1, actor);
                });

        QuestEventHandler<InventoryClickEvent> inventoryClickEventQuestEventHandlerAnvil = universal((event, actor, questUserData) -> {
                    Inventory inventory = event.getInventory();
                    if (inventory.getType() == InventoryType.ANVIL
                            && event.getSlotType() == InventoryType.SlotType.RESULT) {
                        questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ANVIL"), 1, actor);
                    }
                });

        QuestEventHandler<FurnaceExtractEvent> furnaceExtractEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    Material material = event.getItemType();
                    int amount = event.getItemAmount();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("SMELT_ITEM"), material, amount, actor);
                });

        QuestEventHandler<EnchantItemEvent> enchantItemEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ENCHANT"), 1, actor);
                });

        QuestEventHandler<InventoryClickEvent> villagerTradeEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.MERCHANT
                            && event.getSlotType() == InventoryType.SlotType.RESULT
                            && event.getCurrentItem() != null
                            && !event.getCurrentItem().getType().isAir()) {
                        questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("TRADE_VILLAGER"), 1, actor);
                    }
                });

        QuestEventHandler<PlayerItemConsumeEvent> playerItemConsumeEventQuestEventHandlerDrinkPotion = universal((event, actor, questUserData) -> {
                    ItemStack itemStack = event.getItem().clone();

                    if (QuestEventHandlersUtil.isDrink(itemStack.getType())) {
                        questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("DRINK_POTION"), itemStack, 1, actor);
                    }
                });

        QuestEventHandler<PlayerItemConsumeEvent> playerItemConsumeEventQuestEventHandlerEat = universal((event, actor, questUserData) -> {
                    ItemStack itemStack = event.getItem();
                    Material material = itemStack.getType();

                    if (material.isEdible() && !QuestEventHandlersUtil.isDrink(material)) {
                        questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("EAT_ITEM"), material, 1, actor);
                    }
                });

        QuestEventHandler<PlayerFishEvent> playerFishEventQuestEventHandler = universal((event, actor, questUserData) -> {
                    if (event.getCaught() != null && event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
                        if (event.getCaught() instanceof Item item) {
                            ItemStack itemStack = item.getItemStack();
                            Material type = itemStack.getType();
                            if (type == Material.COD ||
                                    type == Material.SALMON ||
                                    type == Material.TROPICAL_FISH ||
                                    type == Material.PUFFERFISH) {
                                questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("CATCH_FISH"), itemStack.getAmount(), actor);
                            }
                        }
                    }
                });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerKillEntity = universal((event, actor, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("KILL_ENTITY"), entityType, 1, actor);
                });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerDeath = universal((event, actor, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("DEATH"), 1, actor);
                });

        QuestEventHandler<EntityDamageByEntityEvent> blockShieldHandler = universal((event, actor, questUserData) -> {
                    if (event.getEntity() instanceof Player defender) {
                        if (defender.isBlocking()) {
                            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("BLOCK_SHIELD"), 1, actor);
                        }
                    }
                });

        QuestEventHandler<EntityBreedEvent> breedEntityHandler = universal((event, actor, questUserData) -> {
                    EntityType entityType = event.getEntityType();
                    questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("BREED_ENTITY"), entityType, 1, actor);
                });

        QuestEventHandler<EntityTransformEvent> cureVillagerHandler = universal((event, actor, questUserData) -> {
                    if (event.getTransformReason() == EntityTransformEvent.TransformReason.CURED) {
                        if (event.getEntityType() == EntityType.ZOMBIE_VILLAGER) {
                            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("CURE_VILLAGER"), 1, actor);
                        }
                    }
                });

        QuestEventHandler<EntityResurrectEvent> useTotemHandler = universal((event, actor, questUserData) -> {
                    if (event.getEntity() instanceof Player) {
                        questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("USE_TOTEM"), 1, actor);
                    }
                });

        QuestEventHandler<PlayerShearEntityEvent> shearSheepHandler = universal((event, actor, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("SHEAR_SHEEP"), 1, actor);
                });

        QuestEventHandler<PlayerInteractEvent> fillComposterHandler = universal((event, actor, questUserData) -> {
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
                                            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("FILL_COMPOSTER"), 1, actor);
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

        QuestEventHandler<EnchantItemEvent> enchantWithLevelHandler = universal((event, actor, questUserData) -> {
                    ItemStack enchantedItem = event.getItem();
                    if (enchantedItem != null) {
                        questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("ENCHANT_WITH_LEVEL"), enchantedItem, 1, actor);
                    }
                });

        QuestEventHandler<InventoryClickEvent> anvilEnchantHandler = universal((event, actor, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.ANVIL) {
                        if (event.getSlotType() == InventoryType.SlotType.RESULT &&
                                event.getCurrentItem() != null &&
                                event.getCurrentItem().getType() != Material.AIR) {
                            ItemStack result = event.getCurrentItem();
                            if (!result.getEnchantments().isEmpty()) {
                                questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("ENCHANT_WITH_LEVEL"), result, 1, actor);
                            }
                        }
                    }
                });

        QuestEventHandler<PlayerInteractEvent> collectFromComposterHandler = universal((event, actor, questUserData) -> {
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
                            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("COLLECT_FROM_COMPOSTER"), 1, actor);
                        } else {
                            ConsoleLogger.info(eventManager.getPlugin().getName(), 
                                "Composter is not full (level %d), skipping COLLECT_FROM_COMPOSTER progress", level);
                        }
                    }
                });

        QuestEventHandler<BlockPlaceEvent> dragonResurrectHandler = universal((event, actor, questUserData) -> {
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
                        
                        questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("RESURRECT_DRAGON"), 1, actor);
                    } else {
                        ConsoleLogger.info(eventManager.getPlugin().getName(), 
                            "Not all 4 crystals placed yet");
                    }
                });

        QuestEventHandler<EntityExplodeEvent> tntBreakBlocksHandler = (event) -> {
            if (event.getEntity().getType() == EntityType.PRIMED_TNT) {
                Entity tnt = event.getEntity();
                int blockCount = event.blockList().size();

                if (blockCount > 0
                        && tnt.getPersistentDataContainer().has(TNT_IGNITER_KEY, PersistentDataType.STRING)) {
                    String playerUuidString = tnt.getPersistentDataContainer().get(TNT_IGNITER_KEY,
                            PersistentDataType.STRING);

                    if (playerUuidString != null) {
                        try {
                            UUID playerUuid = UUID.fromString(playerUuidString);
                            Player actor = org.bukkit.Bukkit.getPlayer(playerUuid);
                            if (actor == null) {
                                return;
                            }
                            QuestUserData questUserData = progressTargetResolver.resolve(actor);

                            if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                                    objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"))) {
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"), blockCount, actor);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        };


        QuestEventHandler<InventoryClickEvent> useGrindstoneHandler = universal((event, actor, questUserData) -> {
                    if (event.getInventory().getType() == InventoryType.GRINDSTONE) {
                        if (event.getSlotType() == InventoryType.SlotType.RESULT &&
                                event.getCurrentItem() != null &&
                                event.getCurrentItem().getType() != Material.AIR) {
                            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("USE_GRINDSTONE_ITEM"), 1, actor);
                        }
                    }
                });

        QuestEventHandler<FurnaceExtractEvent> useFurnaceHandler = universal((event, actor, questUserData) -> {
                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("USE_FURNACE"), 1, actor);
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
                eventManager, questUsersStorage, objectiveRegistry, eventManager.getPlugin(), progressTargetResolver);
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
