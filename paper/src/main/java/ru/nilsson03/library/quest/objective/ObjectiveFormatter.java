package ru.nilsson03.library.quest.objective;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

import ru.nilsson03.library.quest.core.config.Config;
import ru.nilsson03.library.quest.objective.registry.ObjectiveRegistry;
import ru.nilsson03.library.quest.objective.registry.ObjectiveType;

public class ObjectiveFormatter {
    private final Map<ObjectiveType, String> objectiveFormats; 
    
    public ObjectiveFormatter() {
        this.objectiveFormats = new HashMap<>();
    }
    
    public void registerFormat(ObjectiveType type, String format) {
        this.objectiveFormats.put(type, format);
    }
    
    public String getFormat(ObjectiveType type) {
        return this.objectiveFormats.get(type);
    }

    public void onRegistryInit(ObjectiveRegistry registry) {
        ConfigurationSection formatsSection = Config.getConfig().getConfigurationSection("objective_formats");
        if (formatsSection == null) {
            return;
        }
        
        Set<String> keys = formatsSection.getKeys(false);
        for (String key : keys) {
            String format = formatsSection.getString(key);
            if (format != null) {
                try {
                    ObjectiveType type = registry.getObjectiveType(key);
                    registerFormat(type, format);
                } catch (IllegalArgumentException ignored) {
                    
                }
            }
        }
    }
    
    public String formatWithPlaceholders(ObjectiveType type, Map<String, String> placeholders) {
        String format = getFormat(type);
        if (format == null) {
            return null;
        }
        
        String result = format;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue());
        }
        return result;
    }
    
    public static String getFormatFromConfig(String objectiveTypeKey) {
        ConfigurationSection formatsSection = Config.getConfig().getConfigurationSection("objective_formats");
        if (formatsSection == null) {
            return null;
        }
        return formatsSection.getString(objectiveTypeKey);
    }
}
