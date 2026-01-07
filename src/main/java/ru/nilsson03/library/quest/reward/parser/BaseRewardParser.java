package ru.nilsson03.library.quest.reward.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.parser.Parser;
import ru.nilsson03.library.quest.reward.QuestReward;
import ru.nilsson03.library.quest.reward.impl.SimpleQuestReward;

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

        List<String> commands = section.getStringList("commands");
        if (commands.isEmpty()) {
            throw new IllegalArgumentException("commands cannot be empty");
        }

        List<String> message = section.getStringList("message");

        return new SimpleQuestReward(uniqueIdentificationKey, commands, message);
    }
}
