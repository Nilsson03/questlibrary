package ru.nilsson03.library.quest.handler;

import org.bukkit.Material;

public class QuestEventHandlersUtil {

    public static boolean isDrink(Material itemType) {
        return itemType == Material.POTION || itemType == Material.MILK_BUCKET || itemType == Material.HONEY_BOTTLE;
    }

    public static boolean isCompostable(Material material) {
        return switch (material) {
            case BEETROOT_SEEDS, DRIED_KELP, MELON_SEEDS, PUMPKIN_SEEDS, SWEET_BERRIES,
                 WHEAT_SEEDS, KELP, OAK_LEAVES, SPRUCE_LEAVES, BIRCH_LEAVES,
                 JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES, OAK_SAPLING, SPRUCE_SAPLING, BIRCH_SAPLING,
                 JUNGLE_SAPLING, ACACIA_SAPLING, DARK_OAK_SAPLING,
                 SEAGRASS, DRIED_KELP_BLOCK, NETHER_SPROUTS, SUGAR_CANE,
                 TALL_GRASS, VINE, WEEPING_VINES, TWISTING_VINES, CACTUS, MELON_SLICE, SEA_PICKLE,
                 LILY_PAD, PUMPKIN, CARVED_PUMPKIN, MELON, APPLE, BEETROOT, CARROT, COCOA_BEANS,
                 POTATO, WHEAT, BROWN_MUSHROOM, RED_MUSHROOM, MUSHROOM_STEM, CRIMSON_FUNGUS,
                 WARPED_FUNGUS, NETHER_WART, CRIMSON_ROOTS, WARPED_ROOTS, SHROOMLIGHT, DANDELION,
                 POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, ORANGE_TULIP, WHITE_TULIP,
                 PINK_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY, WITHER_ROSE, SUNFLOWER, LILAC, ROSE_BUSH,
                 PEONY, BREAD, COOKIE, BAKED_POTATO, HAY_BLOCK, NETHER_WART_BLOCK,
                 WARPED_WART_BLOCK, CAKE, PUMPKIN_PIE -> true;
            default -> false;
        };
    }
}
