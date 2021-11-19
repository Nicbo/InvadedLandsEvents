package ca.nicbo.invadedlandsevents.util;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

/**
 * Random weighted collection taken from https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java.
 *
 * @author Nicbo
 */
public class RandomWeightedCollection<E> {
    private final NavigableMap<Double, E> map;
    private final Random random;
    private double total;

    public RandomWeightedCollection() {
        this(new Random());
    }

    public RandomWeightedCollection(Random random) {
        this.map = new TreeMap<>();
        this.random = random;
        this.total = 0;
    }

    public RandomWeightedCollection<E> add(E element, double weight) {
        if (weight <= 0) {
            return this;
        }

        total += weight;
        map.put(total, element);
        return this;
    }

    public E next() {
        double value = random.nextDouble() * total;
        return map.higherEntry(value).getValue();
    }
}
