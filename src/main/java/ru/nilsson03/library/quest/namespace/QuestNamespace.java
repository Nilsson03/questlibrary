package ru.nilsson03.library.quest.namespace;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Pattern;

public class QuestNamespace {

    private static final Pattern KEY_PATTERN = Pattern.compile("^[a-zA-Z0-9_-]+$");

    private static final Map<String, QuestNamespace> namespaceCache = new HashMap<>();

    private final String key;

    /**
     * Создает новый Namespace с указанным ключом.
     *
     * @param key Уникальный ключ для типа региона.
     * @throws IllegalArgumentException Если ключ не соответствует требованиям.
     */
    private QuestNamespace(String key) {
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (!KEY_PATTERN.matcher(key)
                        .matches()) {
            throw new IllegalArgumentException(
                    "Key must contain only alphanumeric characters, underscores, or hyphens");
        }
        this.key = key;
    }

    /**
     * Фабричный метод для создания или получения Namespace.
     * в
     *
     * @param key Уникальный ключ для типа региона.
     * @return Существующий или новый Namespace.
     * @throws IllegalArgumentException Если ключ уже существует.
     */
    public static QuestNamespace of(String key) {
        synchronized (namespaceCache) {
            if (namespaceCache.containsKey(key)) {
                return namespaceCache.get(key);
            }
            QuestNamespace namespace = new QuestNamespace(key);
            namespaceCache.put(key, namespace);
            return namespace;
        }
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QuestNamespace that = (QuestNamespace) o;
        return key.equals(that.key);
    }

    /**
     * Возвращает хэш-код Namespace.
     *
     * @return Хэш-код.
     */
    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    /**
     * Возвращает строковое представление Namespace.
     *
     * @return Ключ Namespace.
     */
    @Override
    public String toString() {
        return key;
    }
}
