package me.nicbo.invadedlandsevents.util.misc;

/**
 * Made to infinitely loop through array by getting the next element
 * If last element is reached it returns to the beginning
 *
 * @author Nicbo
 */

public final class Looper<E> {
    private final E[] elements;
    private int index;

    /**
     * Constructor for Looper that defaults initialIndex to 0
     *
     * @param elements the elements to loop through
     */
    public Looper(E[] elements) {
        this(elements, 0);
    }

    /**
     * Constructor for Looper
     *
     * @param elements the elements to loop through
     * @param initialIndex the index to start at
     * @throws IllegalArgumentException if the index is greater than the size of the array minus one or the elements length is 0
     */
    public Looper(E[] elements, int initialIndex) {
        if (elements.length == 0) {
            throw new IllegalArgumentException("elements.length must be greater than 0");
        }

        this.elements = elements;

        if (elements.length - 1 < initialIndex) {
            throw new IllegalArgumentException("initialIndex must be less than elements.length - 1.");
        }

        this.index = initialIndex;
    }


    /**
     * Get the next element
     *
     * @return the next element
     */
    public E next() {
        if (index == elements.length) {
            index = 0;
        }

        return elements[index++];
    }
}
