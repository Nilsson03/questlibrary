package ru.nilsson03.library.quest.user.data.impl;

import ru.nilsson03.library.quest.reward.QuestReward;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class QuestUserReceiptsRewardsData {

    private final Map<UUID, Integer> takenRewardsAndCount;

    public QuestUserReceiptsRewardsData() {
        this(new HashMap<>());
    }

    public QuestUserReceiptsRewardsData(Map<UUID, Integer> takenRewardsAndCount) {
        this.takenRewardsAndCount = takenRewardsAndCount;
    }

    /**
     * Добавление награды в список полученных
     * Если награда уже была получена, то будет увеличено количество получений
     * В противном случае, будет добавлено значение уникального ключа reward и установлено количество получений в 1
     *
     * @param reward награда, которую игрок получил
     */
    public void takeReward(QuestReward reward) {
        if (rewardAlreadyTaken(reward.uniqueIdentificationKey())) {
            takenRewardsAndCount.computeIfPresent(reward.uniqueIdentificationKey(), (k, v) -> v + 1);
            return;
        }

        takenRewardsAndCount.put(reward.uniqueIdentificationKey(), 1);
    }

    /**
     * Проверяет, превышает или равно значение количество раз получения награды
     *
     * @param uuid  идентификатор награды, которую необходимо проверить
     * @param value значение, с которым сравнивается количество получений
     * @return true, если количество получений превышает или равно значение, false в противном случае
     */
    public boolean numberReceiptsExceedsOrEqualValue(UUID uuid, int value) {

        if (!rewardAlreadyTaken(uuid)) {
            return false;
        }

        int countReceipts = takenRewardsAndCount.get(uuid);
        return value >= countReceipts;
    }

    /**
     * Была ли награда уже получена хотя бы 1 раз
     *
     * @param uuid идентификатор награды, которую необходимо проверить
     * @return true, если награда была получена хотя бы 1 раз, false в противном случае
     */
    public boolean rewardAlreadyTaken(UUID uuid) {
        return takenRewardsAndCount.containsKey(uuid);
    }

    public Map<UUID, Integer> getTakenRewardsAndCount() {
        return new HashMap<>(takenRewardsAndCount);
    }
}
