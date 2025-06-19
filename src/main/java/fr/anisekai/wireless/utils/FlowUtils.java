package fr.anisekai.wireless.utils;

import fr.anisekai.wireless.interfaces.ThrowableRunnable;
import fr.anisekai.wireless.interfaces.ThrowableSupplier;

import java.util.function.Function;

/**
 * Class holding various utilities for sugar-syntax.
 */
public final class FlowUtils {

    private FlowUtils() {}

    /**
     * Execute the provided {@code action} and returns its result. If any {@link Exception} occurs while executing it, it will be
     * wrapped into a {@link RuntimeException} using the provided {@link Function}.
     *
     * @param action
     *         The action to execute
     * @param wrapper
     *         The {@link Function} to use to wrap an {@link Exception} into a {@link RuntimeException}.
     * @param <T>
     *         Type of the result
     *
     * @return The {@code action}'s result.
     */
    public static <T> T wrapException(ThrowableSupplier<T> action, Function<Exception, ? extends RuntimeException> wrapper) {

        try {
            return action.get();
        } catch (Exception e) {
            throw wrapper.apply(e);
        }
    }

    /**
     * Execute the provided {@code action}. If any {@link Exception} occurs while executing it, it will be wrapped into a
     * {@link RuntimeException} using the provided {@link Function}.
     *
     * @param action
     *         The action to execute
     * @param wrapper
     *         The {@link Function} to use to wrap an {@link Exception} into a {@link RuntimeException}.
     */
    public static void wrapException(ThrowableRunnable action, Function<Exception, ? extends RuntimeException> wrapper) {

        try {
            action.run();
        } catch (Exception e) {
            throw wrapper.apply(e);
        }
    }


}
