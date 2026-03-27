package ru.nilsson03.library.quest.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
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

import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.handler.handlers.impl.UniversalQuestEventHandler;
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

        QuestEventHandler<EntityExplodeEvent> explodeBlocksHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    int blockCount = event.blockList().size();
                    if (blockCount > 0) {
                        questUserData.incrementProgressQuestsWithValueGoals(
                                objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"), blockCount);
                    }
                });

        QuestEventHandler<PlayerInteractEvent> composterHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND)
                        return;
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                    Block block = event.getClickedBlock();
                    if (block != null && block.getType() == Material.COMPOSTER) {
                        ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
                        if (item != null && item.getType() != Material.AIR) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("FILL_COMPOSTER"), 1);
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

        QuestEventHandler<PlayerInteractEvent> compostFullHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getHand() == null || event.getHand() != EquipmentSlot.HAND)
                        return;
                    if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
                        return;

                    Block block = event.getClickedBlock();
                    if (block != null && block.getType() == Material.COMPOSTER) {
                        Levelled levelled = (Levelled) block.getBlockData();
                        int level = levelled.getLevel();
                        if (level == 8) {
                            questUserData.incrementProgressQuestsWithObjectiveType(
                                    objectiveRegistry.getObjectiveType("COLLECT_FROM_COMPOSTER"), level, 1);
                        }
                    }
                });

        QuestEventHandler<CreatureSpawnEvent> dragonResurrectHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getEntity().getType() == EntityType.ENDER_DRAGON) {
                        if (event.getSpawnReason() == CreatureSpawnEvent.SpawnReason.CUSTOM) {
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("RESURRECT_DRAGON"), 1);
                        }
                    }
                });

        QuestEventHandler<PlayerInteractEvent> igniteTntHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
                    if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        Block block = event.getClickedBlock();
                        if (block != null && block.getType() == Material.TNT) {
                            ItemStack item = event.getItem();
                            if (item != null && (item.getType() == Material.FLINT_AND_STEEL
                                    || item.getType() == Material.FIRE_CHARGE)) {
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("IGNITE_TNT"), 1);

                                Location tntLocation = block.getLocation();
                                UUID playerId = questUserData.uuid();

                                Bukkit.getScheduler().runTaskLater(eventManager.getPlugin(), () -> {
                                    tntLocation.getWorld().getNearbyEntities(tntLocation, 1.5, 1.5, 1.5).stream()
                                            .filter(entity -> entity.getType() == EntityType.PRIMED_TNT)
                                            .findFirst()
                                            .ifPresent(tnt -> {
                                                tnt.getPersistentDataContainer().set(
                                                        TNT_IGNITER_KEY,
                                                        PersistentDataType.STRING,
                                                        playerId.toString());
                                            });
                                }, 1L);
                            }
                        }
                    }
                });

        QuestEventHandler<EntityExplodeEvent> tntBreakBlocksHandler = (event) -> {
            if (event.getEntity() != null && event.getEntity().getType() == EntityType.PRIMED_TNT) {
                Entity tnt = event.getEntity();
                int blockCount = event.blockList().size();

                if (blockCount > 0
                        && tnt.getPersistentDataContainer().has(TNT_IGNITER_KEY, PersistentDataType.STRING)) {
                    String playerUuidString = tnt.getPersistentDataContainer().get(TNT_IGNITER_KEY,
                            PersistentDataType.STRING);

                    if (playerUuidString != null) {
                        try {
                            UUID playerUuid = UUID.fromString(playerUuidString);
                            QuestUserData questUserData = questUsersStorage.getQuestUserData(playerUuid);

                            if (questUserData != null && questUserData.hasActiveQuestWithCurrentObjectiveType(
                                    objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"))) {
                                questUserData.incrementProgressQuestsWithValueGoals(
                                        objectiveRegistry.getObjectiveType("TNT_BREAK_BLOCKS"), blockCount);
                            }
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                }
            }
        };

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
                            questUserData.incrementProgressQuestsWithValueGoals(
                                    objectiveRegistry.getObjectiveType("FILL_COMPOSTER"), 1);
                        }
                    }
                });

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
                put("FILL_COMPOSTER", composterHandler);
                put("ENCHANT_WITH_LEVEL", enchantWithLevelHandler);
                put("ANVIL_ENCHANT_HANDLER", anvilEnchantHandler);
                put("RESURRECT_DRAGON", dragonResurrectHandler);
                put("IGNITE_TNT", igniteTntHandler);
                put("TNT_BREAK_BLOCKS", tntBreakBlocksHandler);
                put("COLLECT_FROM_COMPOSTER", fillComposterHandler);
                put("USE_GRINDSTONE_ITEM", useGrindstoneHandler);
                put("USE_FURNACE", useFurnaceHandler);
                put("BLOCK_DAMAGE_SHIELD", blockSchieldEventHandler);
            }
        });

        // Регистрируем вспомогательный обработчик для отслеживания игрока при
        // трансформации зомби-жителя
        eventManager.registerHandler(PlayerInteractEntityEvent.class, transformEntityTrackerHandler);

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
}
