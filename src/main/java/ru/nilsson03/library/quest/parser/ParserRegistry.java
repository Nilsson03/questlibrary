package ru.nilsson03.library.quest.parser;

import org.bukkit.configuration.ConfigurationSection;
import ru.nilsson03.library.quest.parser.exception.ParserNotRegisterException;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public abstract class ParserRegistry<P extends Parser<O>, O> {

    private final Map<String, P> parsers = new ConcurrentHashMap<>();

    /**
     * Метод для регистрации нового парсера.
     * Рекомендуется использовать только при инициализации плагина.
     *
     * @param pluginName   имя плагина, регистрирующего парсер
     * @param registerType тип значения, которое необходимо зарегистрировать
     * @param parser       класс, который реализует интерфейс Parser
     * @throws IllegalArgumentException если аргументы равны null
     * @throws IllegalStateException    если парсер для данного типа уже зарегистрирован
     */
    public void registerParser(String pluginName, String registerType, P parser) {
        Objects.requireNonNull(pluginName, "pluginName cannot be null");
        Objects.requireNonNull(registerType, "registerType cannot be null");
        Objects.requireNonNull(parser, "parser cannot be null");

        String namespacedKey = pluginName + ":" + registerType;
        if (parsers.containsKey(namespacedKey)) {
            throw new IllegalStateException(
                    "Parser for register type " + namespacedKey + " is already registered!");
        }

        
        parsers.put(namespacedKey, parser);
        parsers.putIfAbsent(registerType, parser);
    }

    /**
     * Метод для парсинга значения из конфигурации.
     *
     * @param section секция конфигурации, содержащая данные для парсинга
     * @return результат парсинга
     * @throws IllegalArgumentException если тип условия неизвестен
     */
    public O parse(ConfigurationSection section) {
        String parserType = section.getKeys(false)
                                      .iterator()
                                      .next();
        P parser = parsers.get(parserType);
        if (parser == null) {
            throw new IllegalArgumentException("Unknown parser type: " + parserType);
        }
        return parser.parse(section.getConfigurationSection(parserType));
    }

    /**
     * Метод для получения зарегистрированного парсера по типу.
     *
     * @param registerType тип парсера
     * @return зарегистрированный парсер или null, если парсер не найден
     */
    public P getParser(String registerType) {
        if (!isParserRegistered(registerType)) {
            throw new ParserNotRegisterException(
                    "Couldn't get the parser using the " + registerType + " key");
        }
        return parsers.get(registerType);
    }

    /**
     * Получение парсера с учётом имени плагина. Сначала ищем по pluginName:registerType,
     * если не найден — используем короткий ключ.
     */
    public P getParser(String pluginName, String registerType) {
        Objects.requireNonNull(pluginName, "pluginName cannot be null");
        Objects.requireNonNull(registerType, "registerType cannot be null");

        String namespacedKey = pluginName + ":" + registerType;
        if (parsers.containsKey(namespacedKey)) {
            return parsers.get(namespacedKey);
        }

        return getParser(registerType);
    }

    /**
     * Метод для проверки, зарегистрирован ли парсер для данного типа.
     *
     * @param registerType тип парсера
     * @return true, если парсер зарегистрирован, иначе false
     */
    public boolean isParserRegistered(String registerType) {
        return parsers.containsKey(registerType);
    }

    public abstract void onRegistryInit();
}
