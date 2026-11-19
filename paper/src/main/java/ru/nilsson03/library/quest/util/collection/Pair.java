package ru.nilsson03.library.quest.util.collection;

import java.util.Map;
import java.util.Objects;

public abstract class Pair<L, R> implements Map.Entry<L, R> {

    public static final Pair<?, ?>[] EMPTY_ARRAY = {};

    @SuppressWarnings("unchecked")
    public static <L, R> Pair<L, R>[] emptyArray() {
        return (Pair<L, R>[]) EMPTY_ARRAY;
    }

    public static <L, R> Pair<L, R> of(final L left, final R right) {
        return ImmutablePair.of(left, right);
    }

    public static <L, R> Pair<L, R> of(final Map.Entry<L, R> pair) {
        return ImmutablePair.of(pair);
    }

    public static <L, R> Pair<L, R> ofNonNull(final L left, final R right) {
        return ImmutablePair.ofNonNull(left, right);
    }


    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof Map.Entry<?, ?>) {
            final Map.Entry<?, ?> other = (Map.Entry<?, ?>) obj;
            return Objects.equals(getKey(), other.getKey())
                    && Objects.equals(getValue(), other.getValue());
        }
        return false;
    }

    @Override
    public final L getKey() {
        return getLeft();
    }

    public abstract L getLeft();

    public abstract R getRight();

    @Override
    public R getValue() {
        return getRight();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
    }

    @Override
    public String toString() {
        return "(" + getLeft() + ',' + getRight() + ')';
    }

    public String toString(final String format) {
        return String.format(format, getLeft(), getRight());
    }

}
