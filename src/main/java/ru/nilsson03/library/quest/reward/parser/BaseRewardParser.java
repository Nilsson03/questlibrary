package ru.nilsson03.library.quest.reward.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.impl.SimpleQuestReward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BaseRewardParser implements Parser<QuestReward> {

    @Override
    public QuestReward parse(ConfigurationSection section) {
        String uuidString = section.getString("uniqueIdentificationKey");
        UUID uniqueIdentificationKey;
        if (uuidString == null) {
            uniqueIdentificationKey = UUID.randomUUID();
        } else {
            uniqueIdentificationKey = UUID.fromString(uuidString);
        }

        List<String> commands = new ArrayList<>();
        if (section.contains("commands")) {
            commands = section.getStringList("commands");
        }

        List<String> description = new ArrayList<>();
        if (section.contains("description")) {
            description = section.getStringList("description");
        }

        List<String> message = new ArrayList<>();
        if (section.contains("message")) {
            message = section.getStringList("message");
        }

        return new SimpleQuestReward(uniqueIdentificationKey, commands, message, description);
    }
}
