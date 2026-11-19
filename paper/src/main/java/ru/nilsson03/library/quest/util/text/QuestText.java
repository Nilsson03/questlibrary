package ru.nilsson03.library.quest.util.text;

import java.util.List;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public final class QuestText {
    private static final Pattern MINI_TAG = Pattern.compile("<[!?/]?[a-zA-Z][^<>]*>");
    private static final Pattern HEX = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.builder()
            .hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    private QuestText() {}

    public static Component component(String text) {
        if (text == null) return Component.empty();
        Component component = miniMessage(text);
        if (component == null) component = LEGACY.deserialize(legacy(text));
        return component.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    public static List<Component> components(List<String> text) {
        return text == null ? List.of() : text.stream().map(QuestText::component).toList();
    }

    public static String colorize(String text) {
        if (text == null) return null;
        Component component = miniMessage(text);
        return legacy(component == null ? text : LEGACY.serialize(component));
    }

    public static String replacePlaceholders(String text, ReplaceData... replacements) {
        for (ReplaceData replacement : replacements) {
            if (replacement != null && replacement.getKey() != null) {
                text = text.replace(replacement.getKey(), legacy(String.valueOf(replacement.getObject())));
            }
        }
        return text;
    }

    public static void send(CommandSender sender, String message) {
        sender.sendMessage(component(message));
    }

    public static void send(CommandSender sender, List<String> messages) {
        messages.forEach(message -> send(sender, message));
    }

    private static Component miniMessage(String text) {
        if (!MINI_TAG.matcher(text).find()) return null;
        try {
            return MiniMessage.miniMessage().deserialize(text);
        } catch (IllegalArgumentException invalidMixedFormatting) {
            return null;
        }
    }

    private static String legacy(String input) {
        String colored = HEX.matcher(input).replaceAll(match -> {
            var result = new StringBuilder("§x");
            for (char digit : match.group(1).toCharArray()) result.append('§').append(digit);
            return result.toString();
        });
        return ChatColor.translateAlternateColorCodes('&', colored);
    }
}
