package ru.nilsson03.library.quest.daily.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

import ru.nilsson03.library.quest.daily.config.DailyQuestConfig;
import ru.nilsson03.library.quest.meta.QuestRarity;
import ru.nilsson03.library.quest.meta.impl.RarityQuestMeta;
import ru.nilsson03.library.quest.quest.simple.BaseQuest;

public final class WeightedRaritySelector {

    private final DailyQuestConfig config;
    private final Random random;

    public WeightedRaritySelector(DailyQuestConfig config) {
        this(config, new Random());
    }

    public WeightedRaritySelector(DailyQuestConfig config, Random random) {
        this.config = Objects.requireNonNull(config, "config");
        this.random = Objects.requireNonNull(random, "random");
    }

    public List<BaseQuest> select(List<BaseQuest> pool) {
        return select(pool, config.limit());
    }

    public List<BaseQuest> select(List<BaseQuest> pool, int limit) {
        if (pool == null || pool.isEmpty() || limit <= 0) {
            return List.of();
        }

        List<BaseQuest> weighted = new ArrayList<>();
        for (BaseQuest quest : pool) {
            QuestRarity rarity = resolveRarity(quest);
            if (rarity == null) {
                continue;
            }
            int weight = config.weightOf(rarity);
            for (int i = 0; i < weight; i++) {
                weighted.add(quest);
            }
        }

        if (weighted.isEmpty()) {
            return List.of();
        }

        Collections.shuffle(weighted, random);
        Set<BaseQuest> selected = new LinkedHashSet<>();
        for (BaseQuest quest : weighted) {
            selected.add(quest);
            if (selected.size() >= limit) {
                break;
            }
        }
        return List.copyOf(selected);
    }

    static QuestRarity resolveRarity(BaseQuest quest) {
        if (quest == null || quest.questMeta() == null) {
            return null;
        }
        if (quest.questMeta() instanceof RarityQuestMeta rarityMeta) {
            return rarityMeta.rarity();
        }
        return null;
    }
}
