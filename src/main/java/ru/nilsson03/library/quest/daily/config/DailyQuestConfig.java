package ru.nilsson03.library.quest.daily.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import ru.nilsson03.library.bukkit.util.TimeUtil;
import ru.nilsson03.library.quest.daily.DailyAssignmentMode;
import ru.nilsson03.library.quest.meta.QuestRarity;

public final class DailyQuestConfig {

    private final int limit;
    private final String updatePeriod;
    private final long updatePeriodMillis;
    private final DailyAssignmentMode assignmentMode;
    private final Map<String, RarityDefinition> rarities;

    public DailyQuestConfig(
            int limit,
            String updatePeriod,
            DailyAssignmentMode assignmentMode,
            Map<String, ? extends Object> rarityWeightsOrDefinitions) {
        this.limit = Math.max(1, limit);
        this.updatePeriod = Objects.requireNonNull(updatePeriod, "updatePeriod");
        this.updatePeriodMillis = TimeUtil.parseStringToTime(updatePeriod) * 1000L;
        this.assignmentMode = Objects.requireNonNull(assignmentMode, "assignmentMode");
        this.rarities = Collections.unmodifiableMap(normalizeRarities(rarityWeightsOrDefinitions));
    }

    private static Map<String, RarityDefinition> normalizeRarities(Map<String, ? extends Object> input) {
        Map<String, RarityDefinition> result = new LinkedHashMap<>();
        if (input == null) {
            return result;
        }
        input.forEach((key, value) -> {
            if (key == null || key.isBlank()) {
                return;
            }
            String id = key.trim().toUpperCase(Locale.ROOT);
            if (value instanceof RarityDefinition definition) {
                result.put(id, definition);
            } else if (value instanceof Number number) {
                result.put(id, new RarityDefinition(number.intValue(), id));
            } else if (value != null) {
                try {
                    int weight = Integer.parseInt(value.toString());
                    result.put(id, new RarityDefinition(weight, id));
                } catch (NumberFormatException ignored) {
                    result.put(id, new RarityDefinition(0, id));
                }
            }
        });
        return result;
    }

    public static DailyQuestConfig fromYaml(FileConfiguration config) {
        Objects.requireNonNull(config, "config");
        int limit = config.getInt("limit", 5);
        String updatePeriod = config.getString("update-period", "1d");
        DailyAssignmentMode mode = DailyAssignmentMode.fromString(config.getString("assignment-mode", "SHARED"));

        Map<String, RarityDefinition> rarities = new LinkedHashMap<>();
        ConfigurationSection raritiesSection = config.getConfigurationSection("rarities");
        if (raritiesSection != null) {
            for (String key : raritiesSection.getKeys(false)) {
                String id = key.trim().toUpperCase(Locale.ROOT);
                if (raritiesSection.isConfigurationSection(key)) {
                    ConfigurationSection entry = raritiesSection.getConfigurationSection(key);
                    int weight = entry.getInt("weight", 0);
                    String displayName = entry.getString("display-name", id);
                    if (displayName == null || displayName.isBlank()) {
                        displayName = id;
                    }
                    rarities.put(id, new RarityDefinition(weight, displayName));
                } else {
                    rarities.put(id, new RarityDefinition(raritiesSection.getInt(key, 0), id));
                }
            }
        }
        return new DailyQuestConfig(limit, updatePeriod, mode, rarities);
    }

    public DailyQuestConfig withOverrides(
            Integer limitOverride,
            String updatePeriodOverride,
            DailyAssignmentMode modeOverride) {
        return new DailyQuestConfig(
                limitOverride != null ? limitOverride : limit,
                updatePeriodOverride != null ? updatePeriodOverride : updatePeriod,
                modeOverride != null ? modeOverride : assignmentMode,
                rarities);
    }

    public int limit() {
        return limit;
    }

    public String updatePeriod() {
        return updatePeriod;
    }

    public long updatePeriodMillis() {
        return updatePeriodMillis;
    }

    public DailyAssignmentMode assignmentMode() {
        return assignmentMode;
    }

    public Map<String, Integer> rarityWeights() {
        Map<String, Integer> weights = new LinkedHashMap<>();
        rarities.forEach((id, definition) -> weights.put(id, definition.weight()));
        return Collections.unmodifiableMap(weights);
    }

    public Map<String, RarityDefinition> rarities() {
        return rarities;
    }

    public int weightOf(QuestRarity rarity) {
        if (rarity == null) {
            return 0;
        }
        return weightOf(rarity.id());
    }

    public int weightOf(String rarityId) {
        RarityDefinition definition = definitionOf(rarityId);
        return definition == null ? 0 : definition.weight();
    }

    public String displayNameOf(QuestRarity rarity) {
        if (rarity == null) {
            return "";
        }
        return displayNameOf(rarity.id());
    }

    public String displayNameOf(String rarityId) {
        if (rarityId == null || rarityId.isBlank()) {
            return "";
        }
        String id = rarityId.trim().toUpperCase(Locale.ROOT);
        RarityDefinition definition = rarities.get(id);
        return definition == null ? id : definition.displayName();
    }

    public boolean hasRarity(String rarityId) {
        if (rarityId == null || rarityId.isBlank()) {
            return false;
        }
        return rarities.containsKey(rarityId.trim().toUpperCase(Locale.ROOT));
    }

    private RarityDefinition definitionOf(String rarityId) {
        if (rarityId == null || rarityId.isBlank()) {
            return null;
        }
        return rarities.get(rarityId.trim().toUpperCase(Locale.ROOT));
    }
}
