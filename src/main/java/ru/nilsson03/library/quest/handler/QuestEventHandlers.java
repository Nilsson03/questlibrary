package ru.nilsson03.library.quest.handler;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.*;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import ru.nilsson03.library.quest.handler.handlers.QuestEventHandler;
import ru.nilsson03.library.quest.handler.handlers.impl.UniversalQuestEventHandler;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.user.storage.QuestUsersStorage;

import java.util.HashMap;
import java.util.Map;

public class QuestEventHandlers {

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
                                                                                                            blockType.name(),
                                                                                                            1);
                                                                                                });

        QuestEventHandler<EntityTransformEvent> entityTransformHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            EntityType entityType = event.getEntityType();
            questUserData.incrementProgressQuestsWithObjectiveType(
                    objectiveRegistry.getObjectiveType("TRANSFORM_ENTITY"), entityType, 1);
        });

        //        QuestEventHandler<PlayerPickupExperienceEvent> playerPickupExperienceEventQuestEventHandler = new UniversalQuestEventHandler<>(
        //                questUsersStorage,
        //                (event, questUserData) -> {
        //                    questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("GAIN_EXP"), event.getExperienceOrb().getExperience());
        //                }
        //        );

        QuestEventHandler<CraftItemEvent> craftItemEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            ItemStack itemStack = event.getCurrentItem();
            int amount = itemStack.getAmount();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("CRAFT_ITEM"),
                                                                   itemStack, amount);
        });

        QuestEventHandler<BlockPlaceEvent> blockPlaceEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            Material type = event.getBlock()
                                 .getType();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("BLOCK_PLACE"),
                                                                   type, 1);
        });

        QuestEventHandler<EntityTameEvent> entityTameEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            EntityType entityType = event.getEntityType();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("TAME_ENTITY"),
                                                                   entityType, 1);
        });

        QuestEventHandler<PlayerItemBreakEvent> playerItemBreakEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            ItemStack itemStack = event.getBrokenItem();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("ITEM_DESTROY"),
                                                                   itemStack, 1);
        });

        QuestEventHandler<InventoryClickEvent> inventoryClickEventQuestEventHandlerAnvil = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            Inventory inventory = event.getInventory();
            if (inventory.getType() == InventoryType.ANVIL && event.getSlotType() == InventoryType.SlotType.RESULT) {
                questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ANVIL"), 1);
            }
        });

        QuestEventHandler<InventoryClickEvent> inventoryClickEventQuestEventHandlerSmelt = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            if (event.getInventory()
                     .getType() == InventoryType.FURNACE && event.getSlotType() == InventoryType.SlotType.RESULT) {
                FurnaceInventory furnaceInventory = (FurnaceInventory) event.getInventory();
                ItemStack result = furnaceInventory.getResult();
                questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("SMELT_ITEM"),
                                                                       result, result.getAmount());
            }
        });

        QuestEventHandler<EnchantItemEvent> enchantItemEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("ENCHANT"), 1);
        });

        QuestEventHandler<PlayerMoveEvent> playerMoveEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("MOVE"), 1);
        });

        QuestEventHandler<PlayerMoveEvent> playerRideHorseEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("RIDE_HORSE"), 1);
        });

        QuestEventHandler<PlayerInteractEntityEvent> playerInteractEntityEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("TRADE_VILLAGER"),
                                                                1);
        });

        QuestEventHandler<PlayerItemConsumeEvent> playerItemConsumeEventQuestEventHandlerDrinkPotion = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            ItemStack itemStack = event.getItem();
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
                questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("EAT_ITEM"),
                                                                       itemStack, 1);
            }
        });

        QuestEventHandler<PlayerFishEvent> playerFishEventQuestEventHandler = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            EntityType entityType = event.getCaught()
                                         .getType();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("CATCH_FISH"),
                                                                   entityType, 1);
        });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerKillEntity = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            EntityType entityType = event.getEntityType();
            questUserData.incrementProgressQuestsWithObjectiveType(objectiveRegistry.getObjectiveType("KILL_ENTITY"),
                                                                   entityType, 1);
        });

        QuestEventHandler<EntityDeathEvent> entityDeathEventQuestEventHandlerDeath = new UniversalQuestEventHandler<>(
                questUsersStorage, (event, questUserData) -> {
            questUserData.incrementProgressQuestsWithValueGoals(objectiveRegistry.getObjectiveType("DEATH"), 1);
        });

        Map<String, QuestEventHandler<?>> handlers = Map.copyOf(new HashMap<>() {{
            put("EXP_CHANGE", expChangeHandler);
            put("BREAK_BLOCK", blockBreakHandler);
            put("TRANSFORM_ENTITY", entityTransformHandler);
            //            put("GAIN_EXP", playerPickupExperienceEventQuestEventHandler);
            put("CRAFT_ITEM", craftItemEventQuestEventHandler);
            put("BLOCK_PLACE", blockPlaceEventQuestEventHandler);
            put("TAME_ENTITY", entityTameEventQuestEventHandler);
            put("ITEM_DESTROY", playerItemBreakEventQuestEventHandler);
            put("ANVIL", inventoryClickEventQuestEventHandlerAnvil);
            put("SMELT_ITEM", inventoryClickEventQuestEventHandlerSmelt);
            put("ENCHANT", enchantItemEventQuestEventHandler);
            put("MOVE", playerMoveEventQuestEventHandler);
            put("RIDE_HORSE", playerRideHorseEventQuestEventHandler);
            put("TRADE_VILLAGER", playerInteractEntityEventQuestEventHandler);
            put("DRINK_POTION", playerItemConsumeEventQuestEventHandlerDrinkPotion);
            put("EAT_ITEM", playerItemConsumeEventQuestEventHandlerEat);
            put("CATCH_FISH", playerFishEventQuestEventHandler);
            put("KILL_ENTITY", entityDeathEventQuestEventHandlerKillEntity);
            put("DEATH", entityDeathEventQuestEventHandlerDeath);
        }});

        for (Map.Entry<String, QuestEventHandler<?>> entry : handlers.entrySet()) {
            eventManager.registerHandler(objectiveRegistry.getObjectiveType(entry.getKey()), entry.getValue());
        }
    }
}
