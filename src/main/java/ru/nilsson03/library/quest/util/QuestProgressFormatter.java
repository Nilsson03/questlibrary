package ru.nilsson03.library.quest.util;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.goal.impl.EntityTypeGoal;
import ru.nilsson03.library.quest.objective.goal.impl.MaterialGoal;
import ru.nilsson03.library.quest.objective.goal.impl.NumericGoal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        
        // Добавляем заголовок задачи
        String objectiveHeader = formatObjectiveHeader(objective, questProgress.isCompleted());
        result.add(objectiveHeader);
        
        // Добавляем цели с прогрессом
        for (Goal goal : objective.goals()) {
            long currentProgress = progress.getOrDefault(goal, 0L);
            long requiredProgress = goal.targetValue();
            boolean isCompleted = currentProgress >= requiredProgress;
            
            String goalLine = formatGoal(goal, currentProgress, requiredProgress, isCompleted);
            result.add("  " + goalLine);
        }
        
        return result;
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
            result.add((i + 1) + ". " + formatObjectiveName(objective));
            
            for (Goal goal : objective.goals()) {
                result.add("   - " + formatGoalName(goal));
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
            result.add((i + 1) + ". " + formatObjectiveHeader(questProgress.objective(), questProgress.isCompleted()));
            
            Map<Goal, Long> progress = questProgress.getProgress();
            for (Goal goal : questProgress.objective().goals()) {
                long currentProgress = progress.getOrDefault(goal, 0L);
                long requiredProgress = goal.targetValue();
                boolean isCompleted = currentProgress >= requiredProgress;
                
                result.add("   " + formatGoal(goal, currentProgress, requiredProgress, isCompleted));
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
    private static String formatGoal(Goal goal, long currentProgress, long requiredProgress, boolean isCompleted) {
        String goalName = formatGoalName(goal);
        
        if (isCompleted) {
            return COMPLETE_COLOR + "✓ " + STRIKETHROUGH + goalName + RESET;
        } else {
            String progressText = PROGRESS_COLOR + currentProgress + RESET + "/" + PROGRESS_COLOR + requiredProgress + RESET;
            return INCOMPLETE_COLOR + "○ " + goalName + " " + progressText + RESET;
        }
    }

    /**
     * Получает читаемое имя задачи.
     */
    private static String formatObjectiveName(Objective objective) {
        String key = objective.key();
        String typeName = objective.type().key();
        
        // Преобразуем ключ в читаемый формат
        String readableName = key.replace("_", " ");
        readableName = capitalizeWords(readableName);
        
        return readableName + " (" + typeName + ")";
    }

    /**
     * Получает читаемое имя цели.
     */
    private static String formatGoalName(Goal goal) {
        if (goal instanceof MaterialGoal materialGoal) {
            Material material = materialGoal.targetType();
            return formatMaterialName(material);
        } else if (goal instanceof EntityTypeGoal entityGoal) {
            EntityType entityType = entityGoal.targetType();
            return formatEntityName(entityType);
        } else if (goal instanceof NumericGoal) {
            return "Значение";
        } else {
            return goal.toString();
        }
    }

    /**
     * Форматирует имя материала в читаемый вид.
     */
    private static String formatMaterialName(Material material) {
        String name = material.name().toLowerCase().replace("_", " ");
        return capitalizeWords(name);
    }

    /**
     * Форматирует имя сущности в читаемый вид.
     */
    private static String formatEntityName(EntityType entityType) {
        String name = entityType.name().toLowerCase().replace("_", " ");
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
     * Создаёт компактное представление прогресса для одной строки.
     * 
     * @param questProgress прогресс квеста
     * @return строка с кратким прогрессом
     */
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
        
        if (questProgress.isCompleted()) {
            return COMPLETE_COLOR + "✓ " + STRIKETHROUGH + objective.key() + RESET + " " + 
                   COMPLETE_COLOR + "(100%)" + RESET;
        } else {
            return INCOMPLETE_COLOR + "○ " + objective.key() + RESET + " " + 
                   PROGRESS_COLOR + String.format("(%.0f%%)", percentage) + RESET;
        }
    }

    /**
     * Создаёт детальное представление прогресса с процентами для каждой цели.
     * 
     * @param questProgress прогресс квеста
     * @return список строк с детальным прогрессом
     */
    public static List<String> formatDetailedProgress(QuestProgress questProgress) {
        List<String> result = new ArrayList<>();
        
        Objective objective = questProgress.objective();
        Map<Goal, Long> progress = questProgress.getProgress();

        result.add(formatObjectiveHeader(objective, questProgress.isCompleted()));

        for (Goal goal : objective.goals()) {
            long currentProgress = progress.getOrDefault(goal, 0L);
            long requiredProgress = goal.targetValue();
            boolean isCompleted = currentProgress >= requiredProgress;
            double percentage = requiredProgress > 0 ? (double) currentProgress / requiredProgress * 100 : 0;
            
            String goalName = formatGoalName(goal);
            String progressBar = createProgressBar(percentage, 10);
            
            if (isCompleted) {
                result.add("  " + COMPLETE_COLOR + "✓ " + STRIKETHROUGH + goalName + RESET);
                result.add("    " + progressBar + " " + COMPLETE_COLOR + "100%" + RESET);
            } else {
                result.add("  " + INCOMPLETE_COLOR + "○ " + goalName + RESET);
                result.add("    " + progressBar + " " + PROGRESS_COLOR + 
                          String.format("%.0f%% (%d/%d)", percentage, currentProgress, requiredProgress) + RESET);
            }
        }
        
        return result;
    }

    /**
     * Создаёт визуальную полоску прогресса.
     * 
     * @param percentage процент выполнения (0-100)
     * @param length длина полоски в символах
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
}
