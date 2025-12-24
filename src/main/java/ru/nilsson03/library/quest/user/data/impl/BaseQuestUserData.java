package ru.nilsson03.library.quest.user.data.impl;

import ru.nilsson03.library.quest.QuestLibrary;
import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.exception.QuestAlreadyCompletedException;
import ru.nilsson03.library.quest.exception.UserAlreadyHasQuestProgressException;
import ru.nilsson03.library.quest.objective.Objective;
import ru.nilsson03.library.quest.objective.goal.Goal;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.user.data.QuestUserData;

import java.util.*;
import java.util.stream.Collectors;

public class BaseQuestUserData implements QuestUserData {

    private final UUID uuid;
    private final List<Quest> completeQuests;
    private final List<QuestProgress> questsProgress;
    private final QuestUserReceiptsRewardsData receiptsRewardsData;

    /**
     * Конструктор для создания объекта QuestUserData с указанным UUID,
     * списком завершенных квестов и списком прогрессов.
     *
     * @param uuid                UUID пользователя.
     * @param completeQuests      Список завершенных квестов.
     * @param objectiveProgresses Список прогрессов по целям квестов.
     * @throws NullPointerException если любой из параметров равен null.
     * @see QuestUserReceiptsRewardsData
     */
    public BaseQuestUserData(
            final UUID uuid, List<Quest> completeQuests,
            final List<QuestProgress> objectiveProgresses, QuestUserReceiptsRewardsData receiptsRewardsData)
            throws NullPointerException {
        this.uuid = Objects.requireNonNull(uuid, "User uuid cant be null");
        this.completeQuests = Objects.requireNonNull(completeQuests, "Complete quests cant be null");
        this.questsProgress = Objects.requireNonNull(objectiveProgresses, "Objective progresses cant be null");
        this.receiptsRewardsData = receiptsRewardsData;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void incrementProgressQuestsWithValueGoals(ObjectiveType objectiveType, long value) {
        if (!hasActiveQuestWithCurrentObjectiveType(objectiveType)) {
            return;
        }

        List<QuestProgress> objectivesProgress = getProgressByObjectiveType(objectiveType);
        objectivesProgress.forEach(progress -> {
            Objective objective = progress.objective();
            for (Goal goal : objective.goals()) {
                progress.incrementProgress(goal, value, false);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void incrementProgressQuestsWithObjectiveType(final ObjectiveType objectiveType, Object object,
            long value) {
        if (!hasActiveQuestWithCurrentObjectiveType(objectiveType)) {
            return;
        }

        List<QuestProgress> objectivesProgress = getProgressByObjectiveType(objectiveType);
        objectivesProgress.forEach(progress -> {
            Objective objective = progress.objective();
            Optional<Goal> optionalGoal = objective.getGoal(object);
            optionalGoal.ifPresent(goal -> progress.incrementProgress(goal, value, false));
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addNewProgressFromSet(Set<QuestProgress> objectiveProgresses) {

        QuestLibrary questLibrary = QuestLibrary.getApi();

        for (QuestProgress objectiveProgress : objectiveProgresses) {
            try {
                addNewProgress(objectiveProgress);
            } catch (QuestAlreadyCompletedException exception) {
                questLibrary.getLogger()
                        .warning(
                                "An error occurred when adding a set of new progressions for the user " + uuid
                                        + " because this quest has already been completed by the player.");
            } catch (UserAlreadyHasQuestProgressException exception) {
                questLibrary.getLogger()
                        .warning(
                                "An error occurred when adding a set of new progressions for the user " + uuid
                                        + " because the player already has progress for this quest with the same task type and set of tasks.");
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean questIsStarted(Quest quest) {
        return questsProgress.stream()
                .anyMatch(objectiveProgresses -> objectiveProgresses.quest()
                        .questUniqueKey()
                        .equals(quest.questUniqueKey()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized List<QuestProgress> getProgressByObjectiveType(final ObjectiveType objectiveType) {
        if (!hasActiveQuestWithCurrentObjectiveType(objectiveType)) {
            return Collections.emptyList();
        }

        return questsProgress.stream()
                .filter(progress -> progress.objective()
                        .type() == objectiveType)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized boolean hasActiveQuestWithCurrentObjectiveType(final ObjectiveType objectiveType) {
        return questsProgress.stream()
                .anyMatch(progress -> progress.objective()
                        .type() == objectiveType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addNewProgress(QuestProgress progress) throws QuestAlreadyCompletedException,
            UserAlreadyHasQuestProgressException {
        if (questIsComplete(progress.quest())) {
            throw new QuestAlreadyCompletedException("Quest already completed");
        }

        if (progressQuestAlreadyFoundInMap(progress)) {
            throw new UserAlreadyHasQuestProgressException(
                    "Collection with quest progress already has progress for this quest: " + progress.quest()
                            .questUniqueKey());
        }

        // if (ObjectivesUtil.isProgressForQuestWithCurrentTypeAlreadyAdded(progress,
        // objectiveProgresses)) {
        // return;
        // }

        questsProgress.add(progress);
    }

    /**
     * Проверяет, есть ли уже прогресс по указанному квесту.
     *
     * @param objectiveProgress Прогресс по квесту.
     * @return true, если прогресс уже существует, иначе false.
     */
    private boolean progressQuestAlreadyFoundInMap(QuestProgress objectiveProgress) {
        return this.questsProgress.stream()
                .anyMatch(progress -> progress.quest()
                        .equals(objectiveProgress.quest()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean questIsComplete(final Quest quest) {
        return completeQuests.contains(quest);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestProgress getProgressByQuestOrThrow(final Quest quest) throws QuestAlreadyCompletedException {

        if (questIsComplete(quest)) {
            throw new QuestAlreadyCompletedException("User already complete this quest, progress not available.");
        }

        return questsProgress.stream()
                .filter(objectiveProgress -> objectiveProgress.quest()
                        .equals(quest))
                .findFirst()
                .orElse(null);
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasActiveReceiptsRewardsData() {
        return receiptsRewardsData != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QuestUserReceiptsRewardsData getReceiptsRewardsData() {
        return receiptsRewardsData;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Quest> completeQuests() {
        return new ArrayList<>(completeQuests);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public UUID uuid() {
        return uuid;
    }

     /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addCompletedQuest(Quest quest) {
        Objects.requireNonNull(quest, "Quest cannot be null");

        if (completeQuests.stream()
                .anyMatch(completeQuest -> completeQuest.questUniqueKey()
                        .equals(quest.questUniqueKey()))) {
            return;
        }

        completeQuests.add(quest);
    }

    @Override
    public List<QuestProgress> getActiveQuests() {
        return new ArrayList<>(questsProgress);
    }

    public synchronized void addActiveQuests(List<QuestProgress> objectiveProgresses) {
        this.questsProgress.addAll(objectiveProgresses);
    }

    @Override
    public synchronized boolean isActiveQuest(Quest quest) {
        return questsProgress.stream()
                .anyMatch(progress -> progress.quest().questUniqueKey().equals(quest.questUniqueKey()));
    }
}
