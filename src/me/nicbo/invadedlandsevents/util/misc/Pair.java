package me.nicbo.invadedlandsevents.util.misc;

/**
 * A generic class for a pair of two objects
 *
 * @author Nicbo
 * @param <L> the type of the left object
 * @param <R> the type of the right object
 */
public class Pair<L, R> {
    private final L left;
    private final R right;

    public Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
