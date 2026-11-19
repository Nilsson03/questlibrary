package ru.nilsson03.library.quest.util;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import ru.nilsson03.library.quest.util.text.QuestText;

public class ItemStackSerialize {

    private static final String DELIMITER = ":";
    private static final String EFFECT_DELIMITER = ";";
    private static final String LORE_DELIMITER = "|";
    private static final String META_DELIMITER = "~";

    public static String serialize(ItemStack item) {
        StringBuilder sb = new StringBuilder()
                .append(item.getType()).append(DELIMITER)
                .append(item.getAmount()).append(DELIMITER);

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();

            if (meta instanceof Damageable) {
                sb.append(((Damageable) meta).getDamage())
                        .append(DELIMITER);
            }

            if (meta.hasCustomModelData()) {
                sb.append(meta.getCustomModelData()).append(DELIMITER);
            }

            if (meta instanceof PotionMeta) {
                sb.append(META_DELIMITER).append("potion_effects").append(DELIMITER);
                serializePotionEffects((PotionMeta) meta, sb);
            }

            if (meta.hasDisplayName()) {
                sb.append(META_DELIMITER)
                        .append("name")
                        .append(DELIMITER)
                        .append(meta.getDisplayName()
                                .replace(META_DELIMITER, "")
                                .replace("§", "&")
                                .replace(LORE_DELIMITER, ""));
            }

            if (meta.hasLore() && meta.getLore() != null) {
                sb.append(META_DELIMITER)
                        .append("lore")
                        .append(DELIMITER)
                        .append(meta.getLore().stream()
                                .map(line -> line.replace(META_DELIMITER, "")
                                        .replace("§", "&")
                                        .replace(LORE_DELIMITER, ""))
                                .collect(Collectors.joining(LORE_DELIMITER)));
            }

            if (!item.getEnchantments().isEmpty()) {
                sb.append(META_DELIMITER).append("ench").append(DELIMITER);
                sb.append(item.getEnchantments().entrySet().stream()
                        .map(e -> e.getKey().getKey() + ":" + e.getValue())
                        .collect(Collectors.joining(DELIMITER)));
            }

            if (!meta.getItemFlags().isEmpty()) {
                sb.append(META_DELIMITER).append("flags").append(DELIMITER);
                sb.append(meta.getItemFlags().stream()
                        .map(Enum::name)
                        .collect(Collectors.joining(",")));
            }

            if (meta instanceof LeatherArmorMeta) {
                LeatherArmorMeta leather = (LeatherArmorMeta) meta;
                sb.append(META_DELIMITER).append("color").append(DELIMITER);
                sb.append(leather.getColor().asRGB());
            }
        }

        return sb.toString();
    }

    public static void deserializePotionEffects(PotionMeta meta, String effectsData) {
        if (effectsData == null || effectsData.isEmpty()) {
            return;
        }

        try {
            String[] effects = effectsData.split(EFFECT_DELIMITER);
            for (String effectPart : effects) {
                if (effectPart.isEmpty())
                    continue;

                String[] effectParts = effectPart.split(DELIMITER);
                if (effectParts.length < 3) {
                    QuestLog.warn("QuestLibrary",
                            "Invalid potion effect data: %s (expected at least 3 parts)", effectPart);
                    continue;
                }

                try {
                    PotionEffectType type = PotionEffectType.getByName(effectParts[0]);
                    if (type == null) {
                        QuestLog.warn("QuestLibrary", "Unknown potion effect type: %s", effectParts[0]);
                        continue;
                    }

                    int duration = Integer.parseInt(effectParts[1]);
                    int amplifier = Integer.parseInt(effectParts[2]);

                    meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
                } catch (NumberFormatException e) {
                    QuestLog.error("QuestLibrary",
                            "Failed to parse potion effect numbers: %s", e.getMessage());
                }
            }
        } catch (Exception e) {
            QuestLog.error("QuestLibrary",
                    "An error occurred when deserializing potion effects: %s", e.getMessage());
        }
    }

    public static void serializePotionEffects(PotionMeta meta, StringBuilder sb) {
        try {
            PotionType baseType = meta.getBasePotionType();
            PotionEffectType effectType = baseType == null ? null : baseType.getEffectType();
            if (effectType != null) {
                int duration = baseType.name().startsWith("LONG_") ? 9600 : 3600;
                int amplifier = baseType.name().startsWith("STRONG_") ? 1 : 0;
                sb.append(effectType.getName())
                        .append(DELIMITER)
                        .append(duration / 20)
                        .append(DELIMITER)
                        .append(amplifier)
                        .append(EFFECT_DELIMITER);
            }

            if (meta.hasCustomEffects()) {
                meta.getCustomEffects()
                        .forEach(effect -> sb.append(effect.getType()
                                .getName())
                                .append(DELIMITER)
                                .append(effect.getDuration())
                                .append(DELIMITER)
                                .append(effect.getAmplifier())
                                .append(EFFECT_DELIMITER));
            }
        } catch (Exception e) {
            QuestLog.error("QuestLibrary",
                    "An error occurred when applying the result of parsing the %s string to PotionMeta, the reason is %s.",
                    sb.toString(), e.getMessage());
        }
    }

    public static Optional<ItemStack> deserialize(String data) {
        if (data == null || data.isEmpty()) {
            QuestLog.warn("QuestLibrary", "Cannot deserialize item: data is null or empty");
            return Optional.empty();
        }

        String[] parts = data.split(DELIMITER, -1);
        if (parts.length < 2) {
            return Optional.empty();
        }

        try {
            Material type = Material.valueOf(parts[0]);
            int amount = Integer.parseInt(parts[1]);
            ItemStack item = new ItemStack(type, amount);

            if (parts.length > 2) {
                ItemMeta meta = item.getItemMeta();
                int index = 2;

                // Пытаемся распарсить damage (если это число)
                if (meta instanceof Damageable && parts.length > index && !parts[index].isEmpty()) {
                    try {
                        int damage = Integer.parseInt(parts[index]);
                        ((Damageable) meta).setDamage(damage);
                        index++;
                    } catch (NumberFormatException e) {
                        // Не число - пропускаем damage
                    }
                } else if (!(meta instanceof Damageable) && parts.length > index && !parts[index].isEmpty()) {
                    // Для не-Damageable предметов пропускаем damage только если это число
                    try {
                        Integer.parseInt(parts[index]);
                        index++;
                    } catch (NumberFormatException e) {
                        // Не число - не пропускаем
                    }
                }

                // Пытаемся распарсить customModelData (если это число)
                if (parts.length > index
                        && !parts[index].isEmpty()) {
                    try {
                        int cmd = Integer.parseInt(parts[index]);
                        if (cmd > 0) {
                            meta.setCustomModelData(cmd);
                        }
                        index++;
                    } catch (NumberFormatException e) {
                        // Не число - пропускаем customModelData
                    }
                }

                if (parts.length > index) {
                    String remaining = String.join(DELIMITER,
                            Arrays.copyOfRange(parts, index, parts.length));

                    String[] metaSections = remaining.split(META_DELIMITER);
                    for (String section : metaSections) {
                        if (section.isEmpty())
                            continue;

                        if (section.startsWith("name" + DELIMITER)) {
                            String name = section.substring(5);
                            name = name.replace("\\:", ":").replace("\\~", "~").replace("\\|", "|");
                            meta.displayName(QuestText.component(name));
                        } else if (section.startsWith("lore" + DELIMITER)) {
                            String loreStr = section.substring(5);
                            if (!loreStr.isEmpty()) {
                                List<String> lore = Arrays.stream(loreStr.split("\\" + LORE_DELIMITER))
                                        .map(line -> line.replace("\\:", ":").replace("\\~", "~").replace("\\|", "|"))
                                        .collect(Collectors.toList());
                                meta.lore(QuestText.components(lore));
                            }
                        } else if (section.startsWith("ench" + DELIMITER)) {
                            String enchStr = section.substring(5);
                            if (!enchStr.isEmpty()) {
                                String[] enchParts = enchStr.split(DELIMITER);
                                for (String ench : enchParts) {
                                    String[] e = ench.split(":");
                                    if (e.length == 2) {
                                        Enchantment enc = Enchantment.getByKey(NamespacedKey.minecraft(e[0]));
                                        if (enc != null) {
                                            item.addUnsafeEnchantment(enc, Integer.parseInt(e[1]));
                                        }
                                    }
                                }
                            }
                        } else if (section.startsWith("flags" + DELIMITER)) {
                            String flagsStr = section.substring(6);
                            if (!flagsStr.isEmpty()) {
                                String[] flags = flagsStr.split(",");
                                for (String flag : flags) {
                                    try {
                                        meta.addItemFlags(ItemFlag.valueOf(flag));
                                    } catch (IllegalArgumentException ignored) {
                                    }
                                }
                            }
                        } else if (section.startsWith("unbreakable" + DELIMITER)) {
                            meta.setUnbreakable(true);
                        } else if (section.startsWith("color" + DELIMITER) && meta instanceof LeatherArmorMeta) {
                            int rgb = Integer.parseInt(section.substring(6));
                            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(rgb));
                        } else if (section.startsWith("potion_effects" + DELIMITER) && meta instanceof PotionMeta) {
                            String effectsData = section.substring(14);
                            deserializePotionEffects((PotionMeta) meta, effectsData);
                        }
                    }
                }

                item.setItemMeta(meta);
            }

            return Optional.of(item);
        } catch (Exception e) {
            QuestLog.error("QuestLibrary", "Deserialize error: %s", e.getMessage());
            return Optional.empty();
        }
    }

    public static void deserializePotionEffects(PotionMeta meta, String[] effectData) {
        for (String effectPart : String.join(DELIMITER, effectData).split(EFFECT_DELIMITER)) {
            if (effectPart.isEmpty()) {
                QuestLog.warn("QuestLibrary",
                        "Couldn't apply result of string parsing to PotionMeta (effectPart is empty)");
                continue;
            }

            String[] effectParts = effectPart.split(DELIMITER);
            if (effectParts.length < 3) {
                QuestLog.warn("QuestLibrary",
                        "Couldn't apply result of %s string parsing to PotionMeta (effectPart.split(%s) < 3)",
                        effectPart, DELIMITER);
                continue;
            }

            try {
                PotionEffectType type = PotionEffectType.getByName(effectParts[0]);
                int duration = Integer.parseInt(effectParts[1]);
                int amplifier = Integer.parseInt(effectParts[2]);
                meta.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
            } catch (Exception e) {
                QuestLog.error("QuestLibrary",
                        "An error occurred when applying the result of parsing the %s string to PotionMeta, the reason is %s.",
                        effectParts.toString(), e.getMessage());
            }
        }
    }
}
