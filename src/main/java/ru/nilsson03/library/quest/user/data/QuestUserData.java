package ru.nilsson03.library.quest.user.data;

import ru.nilsson03.library.quest.core.Quest;
import ru.nilsson03.library.quest.exception.QuestAlreadyCompletedException;
import ru.nilsson03.library.quest.exception.UserAlreadyHasQuestProgressException;
import ru.nilsson03.library.quest.objective.progress.QuestProgress;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;
import ru.nilsson03.library.quest.user.data.impl.QuestUserReceiptsRewardsData;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface QuestUserData {

    void incrementProgressQuestsWithValueGoals(final ObjectiveType objectiveType, long value);

    void incrementProgressQuestsWithObjectiveType(final ObjectiveType objectiveType, Object object, long value);

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
    boolean questIsStarted(Quest quest);

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
    boolean questIsComplete(Quest quest);

    /**
     * Возвращает прогресс по указанному квесту.
     *
     * @param quest Квест.
     * @return Прогресс по квесту или null, если прогресс не найден.
     * @throws QuestAlreadyCompletedException если квест уже завершен.
     */
    QuestProgress getProgressByQuestOrThrow(Quest quest) throws QuestAlreadyCompletedException;

    /**
     * Хранится/есть ли информация о полученных игроком наградах за квесты
     * @return true or false
     */
    boolean hasActiveReceiptsRewardsData();

    /**
     * Метод для получения хранилища с информацией о полученных игроком наградах за выполнение квестов
     * Может быть null, если при инициализации класса был установлен флаг initializeReceiptsRewards == false
     *
     * @return хранилище с информацией о полученных игроком наградах за выполнение квестов
     */
    QuestUserReceiptsRewardsData getReceiptsRewardsData();

    /**
     * Получение завершённых у игрока квестов
     * @return список завершённых квестов
     */
    List<Quest> completeQuests();

    /**
     * Идентификатор пользоватля
     * @return UUID
     */
    UUID uuid();

    /**
     * Добавление игроку выполненного квеста, если он ещё не добавлен в коллекцию
     *
     * @param quest квест, который необходимо добавить в список выполненных
     */
    void addCompletedQuest(Quest quest);

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
    boolean isActiveQuest(Quest quest);
}
