package ru.nightvision.quests.condition;

import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PrerequisiteQuestCondition implements QuestCondition {

    private final List<String> prerequisiteQuestIds;

    public PrerequisiteQuestCondition(List<String> prerequisiteQuestIds) {
        this.prerequisiteQuestIds = prerequisiteQuestIds;
    }

    @Override
    public boolean isMet(QuestUserData user) {
        if (prerequisiteQuestIds == null || prerequisiteQuestIds.isEmpty()) {
            return true;
        }

        Set<String> completed = user.completeQuests()
                            .stream()
                            .map(quest -> quest.questUniqueKey().getKey())
                            .collect(Collectors.toSet());

        return prerequisiteQuestIds.stream()
                                   .allMatch(completed::contains);
    }

    public List<String> getPrerequisiteQuestIds() {
        return prerequisiteQuestIds;
    }
}
