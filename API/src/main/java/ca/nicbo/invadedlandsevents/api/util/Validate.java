package ca.nicbo.invadedlandsevents.api.util;

/**
 * Used for validation.
 *
 * @author Nicbo
 */
public final class Validate {
    private Validate() {
    }

    /**
     * Checks if the passed in object is null.
     *
     * @param object the object
     * @throws NullPointerException if the object is null
     */
    public static void checkNotNull(Object object) {
        if (object == null) {
            throw new NullPointerException();
        }
    }

    /**
     * Checks if the passed in object is null.
     *
     * @param object the object
     * @param message the error message to use
     * @throws NullPointerException if the object is null
     */
    public static void checkNotNull(Object object, String message) {
        if (object == null) {
            throw new NullPointerException(message);
        }
    }

    /**
     * Checks if the passed in object is null.
     *
     * @param object the object
     * @param message the error message to use
     * @param args the arguments to use for the error message
     * @throws NullPointerException if the object is null
     */
    public static void checkNotNull(Object object, String message, Object... args) {
        if (object == null) {
            throw new NullPointerException(String.format(message, args));
        }
    }

    /**
     * Checks if the passed in object is null.
     *
     * @param object the object
     * @param identifier the identifier of the object
     * @throws NullPointerException if the object is null
     */
    public static void checkArgumentNotNull(Object object, String identifier) {
        Validate.checkNotNull(object, "%s can't be null", identifier);
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @throws IllegalStateException if expression is false
     */
    public static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @throws IllegalStateException if expression is false
     */
    public static void checkState(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @param args the arguments to use for the error message
     * @throws IllegalStateException if expression is false
     */
    public static void checkState(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalStateException(String.format(message, args));
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @throws IllegalArgumentException if expression is false
     */
    public static void checkArgument(boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException();
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @throws IllegalArgumentException if expression is false
     */
    public static void checkArgument(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @param args the arguments to use for the error message
     * @throws IllegalArgumentException if expression is false
     */
    public static void checkArgument(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, args));
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @throws UnsupportedOperationException if expression is false
     */
    public static void checkSupported(boolean expression) {
        if (!expression) {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @throws UnsupportedOperationException if expression is false
     */
    public static void checkSupported(boolean expression, String message) {
        if (!expression) {
            throw new UnsupportedOperationException(message);
        }
    }

    /**
     * Checks if the passed in expression is false.
     *
     * @param expression the expression
     * @param message the error message to use
     * @param args the arguments to use for the error message
     * @throws UnsupportedOperationException if expression is false
     */
    public static void checkSupported(boolean expression, String message, Object... args) {
        if (!expression) {
            throw new UnsupportedOperationException(String.format(message, args));
        }
    }
}
