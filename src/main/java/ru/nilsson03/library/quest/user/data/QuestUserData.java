package ru.nilsson03.library.quest.user.data;

import org.bukkit.entity.Player;

import ru.nilsson03.library.quest.exception.QuestAlreadyCompletedException;
import ru.nilsson03.library.quest.exception.UserAlreadyHasQuestProgressException;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QuestUserData {

    void incrementProgressQuestsWithValueGoals(final ObjectiveType objectiveType, long value);

    default void incrementProgressQuestsWithValueGoals(final ObjectiveType objectiveType, long value, Player actor) {
        incrementProgressQuestsWithValueGoals(objectiveType, value);
    }

    void incrementProgressQuestsWithObjectiveType(final ObjectiveType objectiveType, Object object, long value);

    default void incrementProgressQuestsWithObjectiveType(
            final ObjectiveType objectiveType, Object object, long value, Player actor) {
        incrementProgressQuestsWithObjectiveType(objectiveType, object, value);
    }

    /**
     * Добавление игроку новых прогрессов прохождения квестов 
     * @param questProgresses список квестов с прогрессом, которые необходимо добавить
     */
    void addNewProgressFromSet(Set<QuestProgress> questProgresses);

    /**
     * Проверяет, находится ли квест на стадии выполнения
     *
     * @param quest квест
     * @return true, если квест находится на стадии выполнения, иначе false
     */
    boolean questIsStarted(BaseQuest quest);

    /**
     * Возвращает список прогрессов по указанному типу цели.
     *
     * @param objectiveType Тип цели.
     * @return Список прогрессов или пустой список, если нет активных квестов с указанным типом цели.
     */
    List<QuestProgress> getProgressByObjectiveType(ObjectiveType objectiveType);

    /**
     * Проверяет, есть ли активные квесты с указанным типом цели.
     *
     * @param objectiveType Тип цели.
     * @return true, если есть активные квесты с указанным типом цели, иначе false.
     */
    boolean hasActiveQuestWithCurrentObjectiveType(ObjectiveType objectiveType);

    /**
     * Добавляет новый прогресс по квесту.
     *
     * @param progress Прогресс по квесту.
     * @throws QuestAlreadyCompletedException       если квест уже завершен.
     * @throws UserAlreadyHasQuestProgressException если прогресс по этому квесту уже существует.
     */
    void addNewProgress(QuestProgress progress) throws QuestAlreadyCompletedException,
                                                             UserAlreadyHasQuestProgressException;

    /**
     * Проверяет, завершен ли указанный квест.
     *
     * @param quest Квест.
     * @return true, если квест завершен, иначе false.
     */
    boolean questIsComplete(BaseQuest quest);

    /**
     * Возвращает прогресс по указанному квесту.
     *
     * @param quest Квест.
     * @return Прогресс по квесту или null, если прогресс не найден.
     * @throws QuestAlreadyCompletedException если квест уже завершен.
     */
    QuestProgress getProgressByQuestOrThrow(BaseQuest quest) throws QuestAlreadyCompletedException;

    /**
     * Получает все прогрессы игрока по квесту (для всех objectives).
     *
     * @param quest Квест.
     * @return Список прогрессов по всем objectives квеста.
     * @throws QuestAlreadyCompletedException если квест уже завершен.
     */
    List<QuestProgress> getAllProgressForQuest(BaseQuest quest) throws QuestAlreadyCompletedException;

    /**
     * Получение завершённых у игрока квестов
     * @return список завершённых квестов
     */
    List<BaseQuest> completeQuests();

    /**
     * Идентификатор пользоватля
     * @return UUID
     */
    UUID uuid();

    default QuestSubjectKind subjectKind() {
        return QuestSubjectKind.PLAYER;
    }

    /**
     * Добавление игроку выполненного квеста, если он ещё не добавлен в коллекцию
     *
     * @param quest квест, который необходимо добавить в список выполненных
     */
    void addCompletedQuest(BaseQuest quest);

    /**
     * Удаляет квест из списка завершённых у игрока (если присутствует).
     *
     * @param quest квест
     */
    void removeCompletedQuest(BaseQuest quest);

    /**
     * Снимает прогресс и отметку о завершении для указанного квеста.
     *
     * @param quest квест
     */
    default void clearQuestState(BaseQuest quest) {
        removeQuestProgress(quest);
        removeCompletedQuest(quest);
    }

    /**
     * Получение активных квестов игрока
     * @return прогрессы активных квестов
     */
    List<QuestProgress> getActiveQuests();

    /**
     * Добавить игроку список активных квестов
     * @param questProgresses список прогрессов, которые будут добавлены в коллекцию
     */
    void addActiveQuests(List<QuestProgress> questProgresses);

    /**
     * Является ли квест выполняемым игроком в данный момент
     * Поиск ведётся по уникальному ключу квеста
     * @param quest Quest
     * @return true or false
     */
    boolean isActiveQuest(BaseQuest quest);
    
    /**
     * Удаляет все прогрессы указанного квеста из списка активных квестов
     * @param quest квест, прогрессы которого нужно удалить
     */
    void removeQuestProgress(BaseQuest quest);

    /**
     * Имеет ли игрок хоть один активный квест
     */
    boolean hasActiveQuests();

    /**
     * Количество активных у игрока квестов
     */
    int countActiveQuests();
}

