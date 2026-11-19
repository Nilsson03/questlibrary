package ru.nilsson03.library.quest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.MemorySection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import ru.nilsson03.library.quest.util.text.QuestText;

public class ItemStackParser {

    public static ItemStack fromMap(Map<String, Object> parameters) {
        if (parameters == null || !parameters.containsKey("material")) {
            return null;
        }

        Object itemObj = parameters.get("item");
        if (itemObj instanceof ItemStack) {
            return (ItemStack) itemObj;
        }

        Material material = parseMaterial(parameters);
        if (material == null)
            return null;

        int amount = parseAmount(parameters);
        ItemStack item = new ItemStack(material, amount);

        if (isPotion(material)) {
            applyPotionData(item, parameters);
        } else if (isTippedArrow(material)) {
            applyTippedArrowData(item, parameters);
        }
        applyItemMeta(item, parameters);

        return item;
    }

    private static Material parseMaterial(Map<String, Object> parameters) {
        String materialName = parameters.get("material").toString();
        try {
            return Material.valueOf(materialName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static int parseAmount(Map<String, Object> parameters) {
        if (parameters.containsKey("amount")) {
            return Integer.parseInt(parameters.get("amount").toString());
        }
        return 1;
    }

    private static void applyItemMeta(ItemStack item, Map<String, Object> parameters) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null)
            return;

        applyDisplayName(meta, parameters);
        applyLore(meta, parameters);
        applyEnchantments(meta, parameters);
        applyCustomModelData(meta, parameters);
        applyUnbreakable(meta, parameters);

        item.setItemMeta(meta);
    }

    private static void applyDisplayName(ItemMeta meta, Map<String, Object> parameters) {
        if (parameters.containsKey("name")) {
            meta.displayName(QuestText.component(parameters.get("name").toString()));
        }
    }

    private static void applyLore(ItemMeta meta, Map<String, Object> parameters) {
        if (!parameters.containsKey("lore"))
            return;

        Object loreObj = parameters.get("lore");
        if (loreObj instanceof List) {
            List<String> lore = new ArrayList<>();
            for (Object line : (List<?>) loreObj) {
                lore.add(line.toString());
            }
            meta.lore(QuestText.components(lore));
        }
    }

    private static void applyEnchantments(ItemMeta meta, Map<String, Object> parameters) {
        if (!parameters.containsKey("enchantments"))
            return;

        Object enchantsObj = parameters.get("enchantments");
        if (enchantsObj instanceof Map) {
            Map<String, Object> enchants = (Map<String, Object>) enchantsObj;
            for (Map.Entry<String, Object> entry : enchants.entrySet()) {
                Enchantment enchantment = getEnchantment(entry.getKey());
                if (enchantment != null) {
                    int level = Integer.parseInt(entry.getValue().toString());
                    meta.addEnchant(enchantment, level, true);
                }
            }
        }
    }

    private static void applyCustomModelData(ItemMeta meta, Map<String, Object> parameters) {
        if (parameters.containsKey("customModelData")) {
            meta.setCustomModelData(Integer.parseInt(parameters.get("customModelData").toString()));
        }
    }

    private static void applyUnbreakable(ItemMeta meta, Map<String, Object> parameters) {
        if (parameters.containsKey("unbreakable")) {
            meta.setUnbreakable(Boolean.parseBoolean(parameters.get("unbreakable").toString()));
        }
    }

    private static boolean isPotion(Material material) {
        return material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION;
    }

    private static boolean isTippedArrow(Material material) {
        return material == Material.TIPPED_ARROW;
    }

    private static boolean isFirework(Material material) {
        return material == Material.FIREWORK_ROCKET ||
                material == Material.FIREWORK_STAR;
    }

    private static boolean isEnchantedBook(Material material) {
        return material == Material.ENCHANTED_BOOK;
    }

    private static void applyPotionData(ItemStack item, Map<String, Object> parameters) {
        if (!parameters.containsKey("potionData"))
            return;

        Map<String, Object> potionDataMap = extractSection(parameters.get("potionData"));
        if (potionDataMap == null)
            return;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof PotionMeta potionMeta))
            return;

        if (potionDataMap.containsKey("duration")) {
            applyCustomPotionEffect(potionMeta, potionDataMap);
        } else {
            applyLegacyPotionData(potionMeta, potionDataMap);
        }

        applyPotionColor(potionMeta, potionDataMap);

        item.setItemMeta(potionMeta);
    }

    private static void applyCustomPotionEffect(PotionMeta potionMeta, Map<String, Object> data) {
        potionMeta.clearCustomEffects();

        PotionEffectType effectType = parseEffectType(data);
        int durationTicks = parseDuration(data);
        int amplifier = parseAmplifier(data);

        PotionEffect customEffect = new PotionEffect(
                effectType, durationTicks, amplifier, true, true, true);

        potionMeta.addCustomEffect(customEffect, true);
    }

    private static void applyLegacyPotionData(PotionMeta potionMeta, Map<String, Object> data) {
        PotionType potionType = parsePotionType(data);
        boolean extended = parseExtended(data);
        boolean upgraded = parseUpgraded(data);

        if (extended && upgraded) {
            throw new IllegalArgumentException("A potion cannot be both extended and upgraded");
        }
        if (extended || upgraded) {
            String variant = (extended ? "LONG_" : "STRONG_") + potionType.name();
            potionType = PotionType.valueOf(variant);
        }
        potionMeta.setBasePotionType(potionType);
    }

    private static void applyTippedArrowData(ItemStack item, Map<String, Object> parameters) {
        if (!parameters.containsKey("potionData"))
            return;

        Map<String, Object> potionDataMap = extractSection(parameters.get("potionData"));
        if (potionDataMap == null)
            return;

        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof PotionMeta potionMeta))
            return;

        if (potionDataMap.containsKey("effects")) {
            applyMultipleArrowEffects(potionMeta, potionDataMap);
        } else if (potionDataMap.containsKey("duration")) {
            applyCustomPotionEffect(potionMeta, potionDataMap);
        } else {
            applyLegacyPotionData(potionMeta, potionDataMap);
        }

        applyPotionColor(potionMeta, potionDataMap);

        item.setItemMeta(potionMeta);
    }

    private static void applyMultipleArrowEffects(PotionMeta potionMeta, Map<String, Object> data) {
        Object effectsObj = data.get("effects");
        List<?> effectsList = (effectsObj instanceof List) ? (List<?>) effectsObj : null;

        if (effectsList == null)
            return;

        for (Object effectObj : effectsList) {
            Map<String, Object> effectMap = extractSection(effectObj);
            if (effectMap == null)
                continue;

            PotionEffectType effectType = parseEffectType(effectMap);
            int durationTicks = parseDuration(effectMap);
            int amplifier = parseAmplifier(effectMap);

            PotionEffect effect = new PotionEffect(
                    effectType, durationTicks, amplifier, true, true, true);

            potionMeta.addCustomEffect(effect, true);
        }
    }

    private static void applyPotionColor(PotionMeta potionMeta, Map<String, Object> data) {
        if (!data.containsKey("color"))
            return;

        String colorStr = data.get("color").toString();
        Color color = parseColor(colorStr);
        if (color != null) {
            potionMeta.setColor(color);
        }
    }

    private static Color parseColor(String colorStr) {
        if (colorStr.startsWith("#")) {
            return parseHexColor(colorStr);
        }

        String[] rgb = colorStr.split(",");
        if (rgb.length == 3) {
            try {
                int r = Integer.parseInt(rgb[0].trim());
                int g = Integer.parseInt(rgb[1].trim());
                int b = Integer.parseInt(rgb[2].trim());
                return Color.fromRGB(r, g, b);
            } catch (IllegalArgumentException ignored) {
            }
        }

        try {
            return switch (colorStr.toUpperCase()) {
                case "RED" -> Color.RED;
                case "GREEN" -> Color.GREEN;
                case "BLUE" -> Color.BLUE;
                case "YELLOW" -> Color.YELLOW;
                case "PURPLE" -> Color.PURPLE;
                case "ORANGE" -> Color.ORANGE;
                case "LIME" -> Color.LIME;
                case "AQUA" -> Color.AQUA;
                case "WHITE" -> Color.WHITE;
                case "BLACK" -> Color.BLACK;
                case "GRAY", "GREY" -> Color.GRAY;
                case "MAROON" -> Color.MAROON;
                case "NAVY" -> Color.NAVY;
                case "OLIVE" -> Color.OLIVE;
                case "SILVER" -> Color.SILVER;
                case "TEAL" -> Color.TEAL;
                case "FUCHSIA" -> Color.FUCHSIA;
                default -> null;
            };
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Color parseHexColor(String hex) {
        try {
            int r = Integer.parseInt(hex.substring(1, 3), 16);
            int g = Integer.parseInt(hex.substring(3, 5), 16);
            int b = Integer.parseInt(hex.substring(5, 7), 16);
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException | IndexOutOfBoundsException e) {
            return null;
        }
    }

    private static Map<String, Object> extractSection(Object obj) {
        if (obj instanceof MemorySection memorySection) {
            return memorySection.getValues(false);
        } else if (obj instanceof Map) {
            return (Map<String, Object>) obj;
        }
        return null;
    }

    private static PotionEffectType parseEffectType(Map<String, Object> data) {
        if (data.containsKey("type")) {
            String typeName = data.get("type").toString();
            PotionEffectType effectType = PotionEffectType.getByName(typeName);
            if (effectType != null) {
                return effectType;
            }
        }
        return PotionEffectType.REGENERATION;
    }

    private static int parseDuration(Map<String, Object> data) {
        if (data.containsKey("duration")) {
            int seconds = Integer.parseInt(data.get("duration").toString());
            return seconds * 20;
        }
        return 3600;
    }

    private static int parseAmplifier(Map<String, Object> data) {
        if (data.containsKey("amplifier")) {
            return Integer.parseInt(data.get("amplifier").toString());
        }
        return 0;
    }

    private static PotionType parsePotionType(Map<String, Object> data) {
        Object typeObj = data.get("type");
        if (typeObj instanceof String name) {
            String legacyName = name.toUpperCase(java.util.Locale.ROOT).replace("MINECRAFT:", "");
            return PotionType.valueOf(switch (legacyName) {
                case "SPEED" -> "SWIFTNESS";
                case "JUMP" -> "LEAPING";
                case "INSTANT_HEAL" -> "HEALING";
                case "INSTANT_DAMAGE" -> "HARMING";
                case "REGEN" -> "REGENERATION";
                default -> legacyName;
            });
        } else if (typeObj instanceof PotionType) {
            return (PotionType) typeObj;
        }
        throw new IllegalArgumentException("Invalid potion type: " + typeObj);
    }

    private static boolean parseExtended(Map<String, Object> data) {
        if (data.containsKey("extended")) {
            Object extendedObj = data.get("extended");
            if (extendedObj instanceof Boolean) {
                return (Boolean) extendedObj;
            } else if (extendedObj instanceof String) {
                return Boolean.parseBoolean((String) extendedObj);
            }
        }
        return false;
    }

    private static boolean parseUpgraded(Map<String, Object> data) {
        if (data.containsKey("upgraded")) {
            Object upgradedObj = data.get("upgraded");
            if (upgradedObj instanceof Boolean) {
                return (Boolean) upgradedObj;
            } else if (upgradedObj instanceof String) {
                return Boolean.parseBoolean((String) upgradedObj);
            }
        }
        return false;
    }

    private static Enchantment getEnchantment(String name) {
        String normalized = name.toLowerCase().replace("_", "").replace(" ", "");

        Enchantment enchant = Enchantment.getByKey(NamespacedKey.minecraft(name.toLowerCase()));
        if (enchant != null) {
            return enchant;
        }

        for (Enchantment e : Enchantment.values()) {
            String enchantName = e.getKey().getKey().toLowerCase().replace("_", "");
            if (enchantName.equals(normalized)) {
                return e;
            }
        }

        return null;
    }
}
