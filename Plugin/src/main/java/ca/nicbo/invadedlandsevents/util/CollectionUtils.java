package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Utility class for collections.
 * <p>
 * Most of this is needed because we build with Java 8.
 *
 * @author Nicbo
 */
public final class CollectionUtils {
    private CollectionUtils() {
    }

    @SafeVarargs
    public static <T> List<T> unmodifiableList(T... elements) {
        Validate.checkArgumentNotNull(elements, "elements");
        return Collections.unmodifiableList(Arrays.asList(elements));
    }

    public static <T> List<T> reversedCopy(List<T> elements) {
        Validate.checkArgumentNotNull(elements, "elements");
        List<T> reversed = new ArrayList<>(elements);
        Collections.reverse(reversed);
        return reversed;
    }

    public static <T> List<T> shuffledCopy(List<T> elements) {
        Validate.checkArgumentNotNull(elements, "elements");
        List<T> reversed = new ArrayList<>(elements);
        Collections.shuffle(reversed);
        return reversed;
    }

    public static <T> T getOther(List<T> elements, T element) {
        Validate.checkArgumentNotNull(elements, "elements");
        Validate.checkArgument(elements.size() == 2, "elements size must be 2");
        Validate.checkArgumentNotNull(element, "element");
        int elementIndex = elements.indexOf(element);
        Validate.checkArgument(elementIndex != -1, "element must be in elements");
        return elements.get(1 - elementIndex);
    }

    public static <T> List<T> toList(Iterable<T> iterable) {
        Validate.checkArgumentNotNull(iterable, "iterable");
        List<T> list = new ArrayList<>();
        for (T element : iterable) {
            list.add(element);
        }
        return list;
    }

    public static <K> int incrementMap(Map<K, Integer> map, K key) {
        Validate.checkArgumentNotNull(map, "map");
        Validate.checkArgumentNotNull(key, "key");
        return map.merge(key, 1, Integer::sum);
    }

    public static <T> Collector<T, ?, List<T>> toUnmodifiableList() {
        return Collectors.collectingAndThen(Collectors.toList(), Collections::unmodifiableList);
    }

    public static <T> Iterator<T> unmodifiableIterator(Iterator<T> iterator) {
        Validate.checkArgumentNotNull(iterator, "iterator");
        return new UnmodifiableIterator<>(iterator);
    }

    private static final class UnmodifiableIterator<E> implements Iterator<E> {
        private final Iterator<E> iterator;

        private UnmodifiableIterator(Iterator<E> iterator) {
            this.iterator = iterator;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("can't remove from an unmodifiable iterator");
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            iterator.forEachRemaining(action);
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public E next() {
            return iterator.next();
        }
    }
}
