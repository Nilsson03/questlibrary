package ru.nilsson03.library.quest.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import ru.nilsson03.library.bukkit.util.TimeUtil;
import ru.nilsson03.library.bukkit.util.TranslationUtil;
import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.impl.EntityTypeGoal;
import ru.nilsson03.library.quest.objective.goal.impl.ItemStackGoal;
import ru.nilsson03.library.quest.objective.goal.impl.MaterialGoal;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.text.api.UniversalTextApi;
import ru.nilsson03.library.text.util.ReplaceData;

/**
 * Утилита для форматирования прогресса квестов в текстовый список.
 * Показывает прогресс для незавершённых целей и перечёркивает завершённые.
 */
public class QuestProgressFormatter {

    private static final String STRIKETHROUGH = "§m";
    private static final String RESET = "§r";
    private static final String COMPLETE_COLOR = "§a";
    private static final String INCOMPLETE_COLOR = "§7";
    private static final String PROGRESS_COLOR = "§e";

    /**
     * Преобразует список прогрессов квеста в форматированный текстовый список.
     * 
     * @param questProgress прогресс квеста
     * @return список строк с форматированными целями и прогрессом
     */
    public static List<String> formatQuestProgress(QuestProgress questProgress) {
        List<String> result = new ArrayList<>();

        Objective objective = questProgress.objective();
        Map<Goal, Long> progress = questProgress.getProgress();
        ObjectiveType type  = objective.type();

        String objectiveHeader = formatObjectiveHeader(objective, questProgress.isCompleted());
        result.add(objectiveHeader);

        for (Goal goal : objective.goals()) {
            long currentProgress = progress.getOrDefault(goal, 0L);
            long requiredProgress = goal.targetValue();
            boolean isCompleted = currentProgress >= requiredProgress;

            String goalLine = formatGoal(goal, type, currentProgress, requiredProgress, isCompleted);
            result.add("  " + goalLine);
        }

        return result;
    }

    public static String formatCompactProgress(QuestProgress questProgress) {
        Objective objective = questProgress.objective();
        Map<Goal, Long> progress = questProgress.getProgress();

        long totalCurrent = 0;
        long totalRequired = 0;

        for (Goal goal : objective.goals()) {
            totalCurrent += progress.getOrDefault(goal, 0L);
            totalRequired += goal.targetValue();
        }

        double percentage = totalRequired > 0 ? (double) totalCurrent / totalRequired * 100 : 0;

        String objectiveName = formatObjectiveName(objective);

        if (questProgress.isCompleted()) {
            return UniversalTextApi.replacePlaceholders(Config.progressFormatter_CompactCompleteFormat(),
                    new ReplaceData("{objective}", objectiveName));
        } else {
            return UniversalTextApi.replacePlaceholders(Config.progressFormatter_CompactProgressFormat(),
                    new ReplaceData("{objective}", objectiveName),
                    new ReplaceData("{percentage}", String.format("(%.0f%%)", percentage)));
        }
    }


    /**
     * Преобразует список задач квеста в форматированный текстовый список.
     * 
     * @param objectives список задач квеста
     * @return список строк с форматированными задачами
     */
    public static List<String> formatObjectives(List<Objective> objectives) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < objectives.size(); i++) {
            Objective objective = objectives.get(i);
            ObjectiveType type = objective.type();
            result.add((i + 1) + ". " + formatObjectiveName(objective));

            for (Goal goal : objective.goals()) {
                result.add("   - " + formatGoalName(goal, type));
            }
        }

        return result;
    }

    /**
     * Преобразует список задач с прогрессом в форматированный текстовый список.
     * 
     * @param progressList список прогрессов по задачам
     * @return список строк с форматированными задачами и прогрессом
     */
    public static List<String> formatProgressList(List<QuestProgress> progressList) {
        List<String> result = new ArrayList<>();

        for (int i = 0; i < progressList.size(); i++) {
            QuestProgress questProgress = progressList.get(i);
            result.add(INCOMPLETE_COLOR + (i + 1) + ". " + RESET
                    + formatObjectiveHeader(questProgress.objective(), questProgress.isCompleted()));

            Map<Goal, Long> progress = questProgress.getProgress();
            ObjectiveType type = questProgress.objective().type();
            for (Goal goal : questProgress.objective().goals()) {
                long currentProgress = progress.getOrDefault(goal, 0L);
                long requiredProgress = goal.targetValue();
                boolean isCompleted = currentProgress >= requiredProgress;

                result.add("   " + formatGoal(goal, type, currentProgress, requiredProgress, isCompleted));
            }
        }

        return result;
    }

    /**
     * Форматирует заголовок задачи с учётом завершённости.
     */
    private static String formatObjectiveHeader(Objective objective, boolean isCompleted) {
        String objectiveName = formatObjectiveName(objective);

        if (isCompleted) {
            return COMPLETE_COLOR + STRIKETHROUGH + objectiveName + RESET;
        } else {
            return INCOMPLETE_COLOR + objectiveName + RESET;
        }
    }

    /**
     * Форматирует цель с прогрессом.
     */
    private static String formatGoal(Goal goal, ObjectiveType type, long currentProgress, long requiredProgress, boolean isCompleted) {
        String goalName = formatGoalName(goal, type);

        double percentage = requiredProgress > 0 ? (double) currentProgress / requiredProgress * 100 : 0;

        if (isCompleted) {
            return UniversalTextApi.replacePlaceholders(Config.progressFormatter_CompleteFormat(),
                    new ReplaceData("{goal}", goalName));
        } else {
            return UniversalTextApi.replacePlaceholders(Config.progressFormatter_ProgressFormat(),
                    new ReplaceData("{current}", currentProgress),
                    new ReplaceData("{required}", requiredProgress),
                    new ReplaceData("{goal}", goalName),
                    new ReplaceData("{percentage}", String.format("(%.0f%%)", percentage)));
        }
    }

    /**
     * Получает читаемое имя задачи.
     * Использует description если доступно, иначе форматирует ключ.
     */
    private static String formatObjectiveName(Objective objective) {
        if (objective.description() != null && !objective.description().isEmpty()) {
            return objective.description();
        }

        String key = objective.key();
        String typeName = objective.type().key();

        String readableName = key.replace("_", " ");
        readableName = capitalizeWords(readableName);

        return readableName + " (" + typeName + ")";
    }

    /**
     * Получает читаемое имя цели.
     */
    private static String formatGoalName(Goal goal, ObjectiveType type) {
        if (goal instanceof MaterialGoal materialGoal) {
            Material material = materialGoal.targetType();
            return formatMaterialName(material);
        } else if (goal instanceof EntityTypeGoal entityGoal) {
            EntityType entityType = entityGoal.targetType();
            return formatEntityName(entityType);
        } else if (goal instanceof ItemStackGoal itemStackGoal) {
            return formatItemStackName(itemStackGoal.targetType());
        } else if (goal instanceof NumericGoal) {
            return "Значение";
        } else {
            String goalStr = goal.toString();
            if (goalStr.startsWith("EnchantWithLevel(")) {
                return formatEnchantGoalName(goalStr);
            }
            return goalStr;
        }
    }

    private static String formatNumericGoal(ObjectiveType type) {
        switch (type.key()) {
            case "PLAYTIME" -> {
                return Config.progressFormatter_formatPlayTimeGoal();
            }
            case "SURVIVAL_CONDITION" -> {
                return Config.progressFormatter_formatSurvivalGoal();
            }
            case "BLOCK_SHIELD" -> {
                return Config.progressFormatter_formatBlockShieldGoal();
            }
            case "CURE_VILLAGER" -> {
                return Config.progressFormatter_formatCureVillagerGoal();
            }
            case "USE_TOTEM" -> {
                return Config.progressFormatter_formatUseTotemGoal();
            }
            case "SHEAR_SHEEP" -> {
                return Config.progressFormatter_formatShearSheepGoal();
            }
            case "TNT_BREAK_BLOCKS" -> {
                return Config.progressFormatter_formatTntBlockBreaksGoal();
            }
            case "COLLECT_FROM_COMPOSTER" -> {
                return Config.progressFormatter_formatCollectComposterGoal();
            }
            case "FILL_COMPOSTER" -> {
                return  Config.progressFormatter_formatFillComposterGoal();
            }
            case "RESURRECT_DRAGON" -> {
                return Config.progressFormatter_formatResurrectDragonGoal();
            }
            case "IGNITE_TNT" -> {
                return Config.progressFormatter_formatIgniteTntGoal();
            }
            case "USE_GRINDSTONE_ITEM" -> {
                return Config.progressFormatter_formatUseGrindstoneItemGoal();
            }
            case "DEATH" -> {
                return Config.progressFormatter_formatDeathItemGoal();
            }
            case "MOVE" -> {
                return Config.progressFormatter_formatMoveGoal();
            }
            case "EXP_CHANGE" -> {
                return Config.progressFormatter_formatExpChangeGoal();
            }
            default -> {
                return Config.progressFormatter_formatUndefinedGoal();
            }
        }
    }

    /**
     * Форматирует имя цели зачарования в читаемый вид.
     */
    private static String formatEnchantGoalName(String goalStr) {
        try {
            if (goalStr.contains("enchant=")) {
                int enchantStart = goalStr.indexOf("enchant=") + 8;
                int enchantEnd = goalStr.indexOf(",", enchantStart);
                if (enchantEnd == -1)
                    enchantEnd = goalStr.indexOf(")", enchantStart);
                String enchantName = goalStr.substring(enchantStart, enchantEnd);

                String levelInfo = "";
                if (goalStr.contains("level=")) {
                    int levelStart = goalStr.indexOf("level=") + 6;
                    int levelEnd = goalStr.indexOf(",", levelStart);
                    if (levelEnd == -1)
                        levelEnd = goalStr.indexOf(")", levelStart);
                    levelInfo = " " + goalStr.substring(levelStart, levelEnd);
                }

                return "Зачаровать " + capitalizeWords(enchantName.replace("_", " ")) + levelInfo;
            }
        } catch (Exception e) {
            // Fallback to original string
        }
        return goalStr;
    }

    /**
     * Форматирует имя материала в читаемый вид.
     */
    private static String formatMaterialName(ItemStack itemStack) {
        String name = TranslationUtil.translateItem(itemStack);
        return capitalizeWords(name);
    }

    private static String formatMaterialName(Material material) {
        String name = TranslationUtil.translateMaterial(material);
        return capitalizeWords(name);
    }

    /**
     * Форматирует имя сущности в читаемый вид.
     */
    private static String formatEntityName(EntityType entityType) {
        if (entityType == null) {
            return "Unknown Entity";
        }
        String name = TranslationUtil.translateMob(entityType);
        return capitalizeWords(name);
    }

    /**
     * Делает первую букву каждого слова заглавной.
     */
    private static String capitalizeWords(String str) {
        String[] words = str.split(" ");
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
     * Создаёт визуальную полоску прогресса.
     * 
     * @param percentage процент выполнения (0-100)
     * @param length     длина полоски в символах
     * @return строка с полоской прогресса
     */
    private static String createProgressBar(double percentage, int length) {
        int filled = (int) Math.round(percentage / 100.0 * length);
        filled = Math.min(filled, length);

        StringBuilder bar = new StringBuilder();
        bar.append("§8[");

        for (int i = 0; i < length; i++) {
            if (i < filled) {
                bar.append("§a█");
            } else {
                bar.append("§7░");
            }
        }

        bar.append("§8]§r");
        return bar.toString();
    }

    /**
     * Форматирует имя предмета (ItemStack) в читаемый вид.
     * Поддерживает зелья и зачарования.
     */
    private static String formatItemStackName(ItemStack itemStack) {
        StringBuilder nameBuilder = new StringBuilder();
        ItemMeta meta = itemStack.getItemMeta();

        if (meta != null && meta.hasDisplayName()) {
            nameBuilder.append(meta.getDisplayName());
        } else {
            nameBuilder.append(formatMaterialName(itemStack));
        }

        if (meta instanceof PotionMeta potionMeta) {
            if (potionMeta.hasCustomEffects()) {
                for (PotionEffect effect : potionMeta.getCustomEffects()) {
                    nameBuilder.append(", ")
                            .append(formatPotionEffectType(effect.getType()))
                            .append(" ")
                            .append(effect.getAmplifier() + 1)
                            .append(" (")
                            .append(TimeUtil.getTime(effect.getDuration() / 20))
                            .append(")");
                }
            }
        }

        Map<Enchantment, Integer> enchantments = itemStack.getEnchantments();
        if (!enchantments.isEmpty()) {
            for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
                nameBuilder.append(", ")
                        .append(formatEnchantmentName(entry.getKey()))
                        .append(" ")
                        .append(entry.getValue());
            }
        }

        return nameBuilder.toString();
    }

    private static String formatPotionEffectType(PotionEffectType type) {
        return capitalizeWords(type.getName().replace("_", " ").toLowerCase());
    }

    private static String formatEnchantmentName(Enchantment enchantment) {
        return capitalizeWords(TranslationUtil.translateEnchantment(enchantment));
    }
}
