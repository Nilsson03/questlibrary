package ru.nilsson03.library.quest.util.collection;

import java.util.Map;
import java.util.Objects;

public class ImmutablePair<L, R> extends Pair<L, R> {

    public static final ImmutablePair<?, ?>[] EMPTY_ARRAY = {};

    @SuppressWarnings("rawtypes")
    private static final ImmutablePair NULL = new ImmutablePair<>(null, null);

    @SuppressWarnings("unchecked")
    public static <L, R> ImmutablePair<L, R>[] emptyArray() {
        return (ImmutablePair<L, R>[]) EMPTY_ARRAY;
    }

    public static <L, R> Pair<L, R> left(final L left) {
        return ImmutablePair.of(left, null);
    }

    @SuppressWarnings("unchecked")
    public static <L, R> ImmutablePair<L, R> nullPair() {
        return NULL;
    }

    public static <L, R> ImmutablePair<L, R> of(final L left, final R right) {
        return left != null || right != null ? new ImmutablePair<>(left, right) : nullPair();
    }

    public static <L, R> ImmutablePair<L, R> of(final Map.Entry<L, R> pair) {
        return pair != null ? new ImmutablePair<>(pair.getKey(), pair.getValue()) : nullPair();
    }

    public static <L, R> ImmutablePair<L, R> ofNonNull(final L left, final R right) {
        return of(Objects.requireNonNull(left, "left"), Objects.requireNonNull(right, "right"));
    }

    public static <L, R> Pair<L, R> right(final R right) {
        return ImmutablePair.of(null, right);
    }

    public final L left;


    public final R right;


    public ImmutablePair(final L left, final R right) {
        this.left = left;
        this.right = right;
    }


    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public R getRight() {
        return right;
    }

    @Override
    public R setValue(final R value) {
        throw new UnsupportedOperationException();
    }

}
