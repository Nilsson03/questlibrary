package ru.nilsson03.library.quest.handler;

import org.bukkit.Material;

public class QuestEventHandlersUtil {

    public static boolean isDrink(Material itemType) {
        return itemType == Material.POTION || itemType == Material.MILK_BUCKET || itemType == Material.HONEY_BOTTLE;
    }
}
