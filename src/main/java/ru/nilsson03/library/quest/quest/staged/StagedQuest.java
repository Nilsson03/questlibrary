package ru.nilsson03.library.quest.quest.staged;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.stage.QuestStage;

import java.util.Comparator;
import java.util.List;

public interface StagedQuest extends Quest {
    List<QuestStage> stages();

    default List<QuestStage> sortStages(List<QuestStage> stages) {
        return stages.stream()
                     .sorted(Comparator.comparing(QuestStage::weight))
                     .toList();
    }

    default QuestStage getStageByWeightOrThrow(int weight) throws IllegalArgumentException {
        return stages().stream()
                       .filter(stage -> stage.weight() == weight)
                       .findFirst()
                       .orElseThrow(() -> new IllegalArgumentException("Stage with weight " + weight + " not found"));
    }
}
