package ru.nilsson03.library.quest.condition.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import ru.nilsson03.library.quest.condition.QuestCondition;
import ru.nilsson03.library.quest.user.data.QuestUserData;

public class LevelCondition implements QuestCondition {
    private final int minLevel;
    private final int maxLevel;
    private final ConditionType conditionType;

    public LevelCondition(int minLevel, int maxLevel) {
        this(minLevel, maxLevel, ConditionType.START);
    }

    public LevelCondition(int minLevel) {
        this(minLevel, Integer.MAX_VALUE, ConditionType.START);
    }
    
    public LevelCondition(int minLevel, int maxLevel, ConditionType conditionType) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.conditionType = conditionType;
    }

    @Override
    public boolean isMet(QuestUserData userData) {
        Player player = Bukkit.getPlayer(userData.uuid());
        int level = player.getLevel();
        return level >= minLevel && level <= maxLevel;
    }
    
    @Override
    public ConditionType getType() {
        return conditionType;
    }
}
