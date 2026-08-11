package ru.nilsson03.library.quest.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.block.Biome;
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
import ru.nilsson03.library.quest.objective.ObjectiveFormatter;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.impl.EntityTypeGoal;
import ru.nilsson03.library.quest.objective.goal.impl.ItemStackGoal;
import ru.nilsson03.library.quest.objective.goal.impl.MaterialGoal;
import ru.nilsson03.library.quest.objective.goal.impl.MovementTypeGoal;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;
import ru.nilsson03.library.quest.objective.goal.impl.PrerequisiteQuestGoal;
import ru.nilsson03.library.quest.objective.goal.impl.SubmitItemGoal;
import ru.nilsson03.library.quest.objective.goal.impl.SurvivalConditionGoal;
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
    
    @FunctionalInterface
    public interface GoalFormatter {
        String format(Goal goal);
    }

    private static final Map<Class<? extends Goal>, GoalFormatter> customFormatters = new HashMap<>();
    
    static {
        // Регистрация встроенных форматтеров
        registerGoalFormatter(PrerequisiteQuestGoal.class, goal -> {
            PrerequisiteQuestGoal prerequisiteGoal = (PrerequisiteQuestGoal) goal;
            String format = ObjectiveFormatter.getFormatFromConfig("PREREQUISITE_QUEST");
            return format.replace("{quest}", prerequisiteGoal.getQuestName())
                         .replace("{target}", prerequisiteGoal.getVillagerName());
        });
        
        registerGoalFormatter(MaterialGoal.class, goal -> {
            MaterialGoal materialGoal = (MaterialGoal) goal;
            return formatMaterialName(materialGoal.targetType());
        });
        
        registerGoalFormatter(EntityTypeGoal.class, goal -> {
            EntityTypeGoal entityGoal = (EntityTypeGoal) goal;
            return formatEntityName(entityGoal.targetType());
        });
        
        registerGoalFormatter(ItemStackGoal.class, goal -> {
            ItemStackGoal itemStackGoal = (ItemStackGoal) goal;
            return formatItemStackName(itemStackGoal.targetType());
        });
        
        registerGoalFormatter(MovementTypeGoal.class, goal -> {
            MovementTypeGoal movementGoal = (MovementTypeGoal) goal;
            return formatMovementTypeGoal(movementGoal);
        });
        
        registerGoalFormatter(SurvivalConditionGoal.class, goal -> {
            SurvivalConditionGoal survivalGoal = (SurvivalConditionGoal) goal;
            return formatSurvivalConditionGoal(survivalGoal);
        });
        
        registerGoalFormatter(SubmitItemGoal.class, goal -> {
            SubmitItemGoal submitGoal = (SubmitItemGoal) goal;
            return formatItemStackName(submitGoal.targetType());
        });
    }
    
    /**
     * Регистрирует кастомный форматтер для определённого типа цели
     */
    public static void registerGoalFormatter(Class<? extends Goal> goalClass, GoalFormatter formatter) {
        customFormatters.put(goalClass, formatter);
    }

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
     * Поддерживает все типы целей, включая movement / survival и daily-квесты.
     */
    public static String formatObjectiveName(Objective objective) {
        if (objective == null) {
            return "";
        }
        if (objective.description() != null && !objective.description().isEmpty()) {
            return objective.description();
        }

        String key = objective.key();
        String typeName = objective.type() != null ? objective.type().key() : "objective";

        String readableName = key != null ? key.replace("_", " ") : typeName;
        readableName = capitalizeWords(readableName);

        return readableName + " (" + typeName + ")";
    }

    private static String formatGoalName(Goal goal, ObjectiveType type) {
        GoalFormatter customFormatter = customFormatters.get(goal.getClass());
        if (customFormatter != null) {
            return customFormatter.format(goal);
        }
        
        if (goal instanceof NumericGoal) {
            String format = ObjectiveFormatter.getFormatFromConfig(type.key());
            if (format != null) {
                return format;
            }
            return Config.progressFormatter_formatUndefinedGoal();
        }
        
        String goalStr = goal.toString();
        if (goalStr.startsWith("EnchantWithLevel(")) {
            return formatEnchantGoalName(goalStr);
        }
        
        return goalStr;
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

                String level = "";
                if (goalStr.contains("level=")) {
                    int levelStart = goalStr.indexOf("level=") + 6;
                    int levelEnd = goalStr.indexOf(",", levelStart);
                    if (levelEnd == -1)
                        levelEnd = goalStr.indexOf(")", levelStart);
                    level = goalStr.substring(levelStart, levelEnd);
                }

                String format = ObjectiveFormatter.getFormatFromConfig("ENCHANT_WITH_LEVEL");
                if (format != null) {
                    return format
                            .replace("{item}", capitalizeWords(enchantName.replace("_", " ")))
                            .replace("{level}", level);
                }
                
                return "Зачаровать " + capitalizeWords(enchantName.replace("_", " ")) + (level.isEmpty() ? "" : " " + level);
            }
        } catch (Exception e) {
            
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

    private static String formatMovementTypeGoal(MovementTypeGoal movementGoal) {
        MovementTypeGoal.MovementType type = movementGoal.getMovementType();
        
        return switch (type) {
            case WALK -> "Пешком";
            case FLY -> "На элитрах";
            case BOAT -> "На лодке";
            case HORSE -> "На лошади";
            case PIG -> "На свинье";
            case STRIDER -> "На страйдере";
            case VEHICLE -> "На транспорте";
            case ANY -> "Любым способом";
        };
    }

    private static String formatSurvivalConditionGoal(SurvivalConditionGoal survivalGoal) {
        StringBuilder conditions = new StringBuilder();
        
        PotionEffectType effect = survivalGoal.getRequiredEffect();
        String world = survivalGoal.getRequiredWorld();
        Biome biome = survivalGoal.getRequiredBiome();
        
        if (effect != null) {
            conditions.append("С эффектом ").append(formatPotionEffectType(effect));
        }
        
        if (world != null) {
            if (!conditions.isEmpty()) conditions.append(", ");
            conditions.append("в мире ").append(world);
        }
        
        if (biome != null) {
            if (!conditions.isEmpty()) conditions.append(", ");
            conditions.append("в биоме ").append(formatBiomeName(biome));
        }
        
        if (conditions.isEmpty()) {
            conditions.append("Выжить");
        }
        
        String format = ObjectiveFormatter.getFormatFromConfig("SURVIVAL_CONDITION");
        if (format != null) {
            return format.replace("{conditions}", conditions.toString());
        }
        
        return conditions.toString();
    }

    /**
     * Форматирует имя биома в читаемый вид.
     */
    private static String formatBiomeName(Biome biome) {
        return capitalizeWords(biome.name().replace("_", " ").toLowerCase());
    }
}
