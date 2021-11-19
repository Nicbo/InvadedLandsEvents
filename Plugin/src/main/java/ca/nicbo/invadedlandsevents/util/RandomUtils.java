package ca.nicbo.invadedlandsevents.util;

import ca.nicbo.invadedlandsevents.api.util.Validate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiPredicate;

/**
 * Utility class for random.
 *
 * @author Nicbo
 */
public final class RandomUtils {
    private static final int MAX_RANDOM_ELEMENT_NOT_EQUAL_ATTEMPTS = 200;

    private RandomUtils() {
    }

    public static int randomMinMax(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static boolean randomBoolean() {
        return ThreadLocalRandom.current().nextBoolean();
    }

    public static <T> T randomElement(List<T> elements) {
        Validate.checkArgumentNotNull(elements, "elements");
        Validate.checkArgument(!elements.isEmpty(), "elements can't be empty");
        return elements.get(ThreadLocalRandom.current().nextInt(elements.size()));
    }

    public static <T> T randomElementNotEqual(List<T> elements, T element) {
        return randomElementNotEqual(elements, element, Object::equals); // default impl
    }

    // elementEquals should be symmetric
    public static <T> T randomElementNotEqual(List<T> elements, T element, BiPredicate<T, T> elementEquals) {
        Validate.checkArgumentNotNull(elements, "elements");
        Validate.checkArgument(elements.size() >= 2, "elements size must be >= 2, provided size: %d", elements.size());
        Validate.checkArgumentNotNull(element, "element");
        Validate.checkArgumentNotNull(elementEquals, "elementEquals");

        int attempts = 0;
        T selected;
        do {
            if (++attempts == MAX_RANDOM_ELEMENT_NOT_EQUAL_ATTEMPTS + 1) {
                throw new IllegalStateException("max randomElementNotEqual attempts reached: " + attempts);
            }

            selected = randomElement(elements);
        } while (elementEquals.test(selected, element));

        return selected;
    }
}
