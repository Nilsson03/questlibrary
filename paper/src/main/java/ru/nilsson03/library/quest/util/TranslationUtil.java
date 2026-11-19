package ru.nilsson03.library.quest.util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.quest.util.text.QuestText;

/**
 * @author Nicholas Alexandrov
 */
public class TranslationUtil {

    private static final Map<String, YamlConfiguration> translationConfigs = new HashMap<>();
    private static String defaultLanguage = "ru";
    private static JavaPlugin libraryPlugin = null;

    /**
     * Инициализирует систему переводов с указанием плагина библиотеки.
     * Должен быть вызван один раз при загрузке библиотеки.
     *
     * @param plugin экземпляр плагина библиотеки
     */
    public static void initialize(JavaPlugin plugin) {
        libraryPlugin = plugin;
    }

    /**
     * Загружает файл переводов для указанного языка из ресурсов библиотеки.
     *
     * @param language код языка (например, "ru", "en")
     */
    public static void loadTranslations(String language) {
        Objects.requireNonNull(language, "Language cannot be null");

        String fileName = "translations-" + language.toLowerCase() + ".yml";
        String resourcePath = "translation/" + fileName;

        try {
            InputStream stream = libraryPlugin != null
                    ? libraryPlugin.getResource(resourcePath)
                    : TranslationUtil.class.getClassLoader().getResourceAsStream(resourcePath);

            if (stream == null) {
                if (libraryPlugin != null) {
                    QuestLog.warn(libraryPlugin, "Translation file not found in resources: %s", resourcePath);
                }
                return;
            }

            InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            YamlConfiguration config = YamlConfiguration.loadConfiguration(reader);
            translationConfigs.put(language.toLowerCase(), config);

            if (libraryPlugin != null) {
                QuestLog.info(libraryPlugin, "Loaded translation file from resources: %s", fileName);
            }

            stream.close();
        } catch (Exception e) {
            if (libraryPlugin != null) {
                QuestLog.warn(libraryPlugin, "Failed to load translation file %s: %s", fileName, e.getMessage());
            }
        }
    }

    /**
     * Устанавливает язык по умолчанию.
     *
     * @param language код языка
     */
    public static void setDefaultLanguage(String language) {
        defaultLanguage = language.toLowerCase();
    }

    /**
     * Получает язык по умолчанию.
     *
     * @return код языка по умолчанию
     */
    public static String getDefaultLanguage() {
        return defaultLanguage;
    }

    public static String translateItem(ItemStack itemStack) {
        return translateItem(itemStack, defaultLanguage);
    }

    public static String translateItem(ItemStack itemStack, String language) {
        Material type = itemStack.getType();
        if (type == Material.POTION ||
                type == Material.LINGERING_POTION ||
                type == Material.SPLASH_POTION ||
                type == Material.TIPPED_ARROW) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            var base = potionMeta.getBasePotionType();
            PotionEffectType effectType = base == null ? null : base.getEffectType();
            return translatePotion(effectType, type, language);
        } else if (type == Material.SHIELD) {
            return translateShield(itemStack, language);
        } else {
            return translateMaterial(type, language);
        }
    }

    private static String translateColor(Material material, DyeColor color, String language) {
        return getTranslation(language, "items." + material + "." + color.name(),
                formatName(material.name() + " " + color.name()));
    }

    private static String translateShield(ItemStack itemStack, String language) {
        ItemMeta meta = itemStack.getItemMeta();
        Material type = itemStack.getType();
        if (meta instanceof BlockStateMeta blockStateMeta) {
            if (blockStateMeta.getBlockState() instanceof Banner banner) {
                DyeColor baseColor = banner.getBaseColor();
                return translateColor(type, baseColor, language);
            }
        }
        return translateMaterial(Material.SHIELD, language);
    }

    /**
     * Переводит название материала на указанный язык.
     *
     * @param material материал для перевода
     * @param language код языка
     * @return переведенное название или оригинальное имя материала
     */
    public static String translateMaterial(Material material, String language) {
        if (material == null)
            return "";
        return getTranslation(language, "items." + material.name().toUpperCase(), formatName(material.name()));
    }

    public static String translatePotion(PotionEffectType type, Material material, String language) {
        return getTranslation(language, "effects." + type.getName(), formatName(type.getName()));
    }

    public static String translatePotion(PotionEffectType type) {
        return getTranslation(getDefaultLanguage(), "effects." + type.getName(),
                formatName(type.getName()));
    }

    /**
     * Переводит название материала на язык по умолчанию.
     *
     * @param material материал для перевода
     * @return переведенное название или оригинальное имя материала
     */
    public static String translateMaterial(Material material) {
        return translateMaterial(material, defaultLanguage);
    }

    /**
     * Переводит название моба на указанный язык.
     *
     * @param entityType тип сущности для перевода
     * @param language   код языка
     * @return переведенное название или оригинальное имя моба
     */
    public static String translateMob(EntityType entityType, String language) {
        if (entityType == null)
            return "";
        return getTranslation(language, "mobs." + entityType.name().toUpperCase(), formatName(entityType.name()));
    }

    /**
     * Переводит название моба на язык по умолчанию.
     *
     * @param entityType тип сущности для перевода
     * @return переведенное название или оригинальное имя моба
     */
    public static String translateMob(EntityType entityType) {
        return translateMob(entityType, defaultLanguage);
    }

    /**
     * Переводит название эффекта на указанный язык.
     *
     * @param effectType тип эффекта для перевода
     * @param language   код языка
     * @return переведенное название или оригинальное имя эффекта
     */
    public static String translateEffect(PotionEffectType effectType, String language) {
        if (effectType == null)
            return "";
        String effectName = effectType.getName().toUpperCase();
        return getTranslation(language, "effects." + effectName, formatName(effectName));
    }

    /**
     * Переводит название эффекта на язык по умолчанию.
     *
     * @param effectType тип эффекта для перевода
     * @return переведенное название или оригинальное имя эффекта
     */
    public static String translateEffect(PotionEffectType effectType) {
        return translateEffect(effectType, defaultLanguage);
    }

    /**
     * Переводит название зачарования на указанный язык.
     *
     * @param enchantment зачарование для перевода
     * @param language    код языка
     * @return переведенное название или оригинальное имя зачарования
     */
    public static String translateEnchantment(Enchantment enchantment, String language) {
        if (enchantment == null)
            return "";
        String enchantName = enchantment.getKey().getKey().toUpperCase();
        return getTranslation(language, "items." + enchantName, formatName(enchantName));
    }

    /**
     * Переводит название зачарования на язык по умолчанию.
     *
     * @param enchantment зачарование для перевода
     * @return переведенное название или оригинальное имя зачарования
     */
    public static String translateEnchantment(Enchantment enchantment) {
        return translateEnchantment(enchantment, defaultLanguage);
    }

    /**
     * Переводит название спавнера на указанный язык.
     *
     * @param entityType тип моба в спавнере
     * @param language   код языка
     * @return переведенное название спавнера
     */
    public static String translateSpawner(EntityType entityType, String language) {
        if (entityType == null)
            return "";
        return getTranslation(language, "spawners-name." + entityType.name(),
                "Spawner " + formatName(entityType.name()));
    }

    /**
     * Переводит название спавнера на язык по умолчанию.
     *
     * @param entityType тип моба в спавнере
     * @return переведенное название спавнера
     */
    public static String translateSpawner(EntityType entityType) {
        return translateSpawner(entityType, defaultLanguage);
    }

    /**
     * Получает перевод по указанному пути.
     *
     * @param language     код языка
     * @param path         путь к переводу в YAML файле
     * @param defaultValue значение по умолчанию, если перевод не найден
     * @return переведенная строка или значение по умолчанию
     */
    public static String getTranslation(String language, String path, String defaultValue) {
        YamlConfiguration config = translationConfigs.get(language.toLowerCase());

        if (config == null) {
            return QuestText.colorize(defaultValue);
        }

        // getString() may stringify a YAML section as
        // "MemorySection[path=..., root=YamlConfiguration]". This is not a
        // translation and must never leak into item names/lore.
        Object raw = config.get(path);
        if (raw instanceof ConfigurationSection || !(raw instanceof String)) {
            return QuestText.colorize(defaultValue);
        }

        String translation = (String) raw;

        if (translation == null || translation.isEmpty()) {
            return QuestText.colorize(defaultValue);
        }

        return QuestText.colorize(translation);
    }

    /**
     * Получает перевод по указанному пути с языком по умолчанию.
     *
     * @param path         путь к переводу в YAML файле
     * @param defaultValue значение по умолчанию, если перевод не найден
     * @return переведенная строка или значение по умолчанию
     */
    public static String getTranslation(String path, String defaultValue) {
        return getTranslation(defaultLanguage, path, defaultValue);
    }

    /**
     * Проверяет, загружен ли файл переводов для указанного языка.
     *
     * @param language код языка
     * @return true, если файл переводов загружен
     */
    public static boolean isLanguageLoaded(String language) {
        return translationConfigs.containsKey(language.toLowerCase());
    }

    /**
     * Перезагружает файл переводов для указанного языка.
     *
     * @param language код языка
     */
    public static void reloadTranslations(String language) {
        translationConfigs.remove(language.toLowerCase());
        loadTranslations(language);
    }

    /**
     * Перезагружает все загруженные файлы переводов.
     */
    public static void reloadAllTranslations() {
        Set<String> languages = new HashSet<>(translationConfigs.keySet());
        translationConfigs.clear();
        languages.forEach(TranslationUtil::loadTranslations);
    }

    /**
     * Очищает все загруженные переводы.
     */
    public static void clearTranslations() {
        translationConfigs.clear();
    }

    /**
     * Форматирует имя, заменяя подчеркивания на пробелы и делая первую букву
     * заглавной.
     *
     * @param name исходное имя
     * @return отформатированное имя
     */
    private static String formatName(String name) {
        if (name == null || name.isEmpty())
            return "";

        String[] words = name.toLowerCase().split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1))
                        .append(" ");
            }
        }

        return result.toString().trim();
    }

    /**
     * Получает все доступные языки.
     *
     * @return массив кодов загруженных языков
     */
    public static String[] getAvailableLanguages() {
        return translationConfigs.keySet().toArray(new String[0]);
    }
}
