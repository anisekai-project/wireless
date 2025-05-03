package fr.anisekai.wireless.utils;

import java.util.Collection;

/**
 * Utility class for performing argument validation checks.
 * <p>
 * Provides simple assertions to ensure method arguments meet expected conditions.
 */
public final class MakeSureThat {

    private MakeSureThat() {}

    /**
     * Ensures the given object is not {@code null}.
     *
     * @param object
     *         The object to check.
     * @param message
     *         The exception message if the check fails.
     *
     * @throws IllegalArgumentException
     *         Threw if {@code object} is {@code null}.
     */
    public static void isNotNull(Object object, String message) {

        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures the given collection is not empty.
     *
     * @param object
     *         The collection to check.
     * @param message
     *         The exception message if the check fails.
     *
     * @throws IllegalArgumentException
     *         Threw if the collection is empty.
     */
    public static void isNotEmpty(Collection<?> object, String message) {

        if (object.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * Ensures the given string does not exceed the specified length.
     *
     * @param string
     *         The string to check.
     * @param length
     *         The maximum allowed length.
     * @param message
     *         The exception message if the check fails.
     *
     * @throws IllegalArgumentException
     *         Threw if the string length is greater than {@code length}.
     */
    public static void isNotLongerThan(CharSequence string, int length, String message) {

        if (string.length() > length) {
            throw new IllegalArgumentException(message);
        }
    }

}
