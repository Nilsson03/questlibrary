package ru.nilsson03.library.quest.util;


import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public final class Namespace {

    // Регулярное выражение для валидации ключа
    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final Map<String, Map<String, Namespace>> pluginNamespacesCache = new HashMap<>();

    private final String pluginName;
    private final String key;

    /**
     * Создает новый Namespace с указанным именем плагина и ключом.
     *
     * @param pluginName Имя плагина, к которому привязан namespace.
     * @param key Уникальный ключ для типа региона.
     * @throws IllegalArgumentException Если ключ не соответствует требованиям.
     */
    private Namespace(String pluginName, String key) {
        if (pluginName == null || pluginName.isEmpty()) {
            QuestLog.debug("QuestLibrary", "Plugin name cannot be null or empty, class %s", "Namespace");
            throw new IllegalArgumentException("Plugin name cannot be null or empty");
        }
        if (key == null || key.isEmpty()) {
            QuestLog.debug("QuestLibrary", "Namespace Key cannot be null or empty, class %s", "Namespace");
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (!KEY_PATTERN.matcher(key).matches()) {
            QuestLog.debug("QuestLibrary", "Key %s must contain only alphanumeric characters, underscores, or hyphens, class %s", key, "Namespace");
            throw new IllegalArgumentException("Key must contain only alphanumeric characters, underscores, or hyphens");
        }

        this.pluginName = pluginName;
        this.key = key;
    }

    public static boolean isRegistered(String pluginName, String namespaceKey) {
        Objects.requireNonNull(pluginName, "pluginName cannot be null");
        if (namespaceKey == null || namespaceKey.isEmpty()) {
            QuestLog.error("QuestLibrary", "Namespace key cannot be null or empty, class %s", "Namespace");
            return false;
        }
        Map<String, Namespace> pluginNamespaces = pluginNamespacesCache.get(pluginName);
        return pluginNamespaces.containsKey(namespaceKey);
    }

    /**
     * Фабричный метод для создания или получения Namespace.
     *
     * @param pluginName Имя плагина, к которому привязан namespace.
     * @param key Уникальный ключ для типа региона.
     * @return Существующий или новый Namespace.
     */
    public static Namespace of(String pluginName, String key) {
        synchronized (pluginNamespacesCache) {
            Map<String, Namespace> pluginNamespaces = pluginNamespacesCache.computeIfAbsent(
                    pluginName, k -> new HashMap<>()
            );

            if (pluginNamespaces.containsKey(key)) {
                return pluginNamespaces.get(key);
            }

            Namespace namespace = new Namespace(pluginName, key);
            pluginNamespaces.put(key, namespace);
            return namespace;
        }
    }

    /**
     * Возвращает имя плагина, к которому привязан namespace.
     *
     * @return Имя плагина.
     */
    public String getPluginName() {
        return pluginName;
    }

    /**
     * Возвращает ключ Namespace.
     *
     * @return Уникальный ключ.
     */
    public String getKey() {
        return key;
    }

    /**
     * Проверяет, равен ли текущий Namespace другому объекту.
     *
     * @param o Объект для сравнения.
     * @return true, если объекты равны, иначе false.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Namespace that = (Namespace) o;
        return pluginName.equals(that.pluginName) && key.equals(that.key);
    }

    /**
     * Возвращает хэш-код Namespace.
     *
     * @return Хэш-код.
     */
    @Override
    public int hashCode() {
        return Objects.hash(pluginName, key);
    }

    /**
     * Возвращает строковое представление Namespace.
     *
     * @return Строка в формате "pluginName:key".
     */
    @Override
    public String toString() {
        return pluginName + ":" + key;
    }
}
