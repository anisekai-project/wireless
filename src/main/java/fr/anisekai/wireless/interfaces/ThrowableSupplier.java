package fr.anisekai.wireless.interfaces;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A functional interface similar to {@link Supplier}, but allows throwing checked exceptions.
 *
 * <p>This is useful for deferred computations that may fail and need to propagate checked exceptions,
 * especially when used in lambda expressions or method references.</p>
 *
 * @param <T>
 *         The type of the result supplied by this supplier.
 */
public interface ThrowableSupplier<T> {

    /**
     * Supply a value, potentially throwing a checked exception.
     *
     * @return The value
     *
     * @throws Exception
     *         Threw if an error occurs while supplying the value
     */
    T get() throws Exception;

    /**
     * Supply a value, transforming any thrown exception using the provided transformer.
     * <p>
     * This allows callers to wrap or convert checked exceptions into runtime exceptions or custom exception types.
     *
     * @param transformer
     *         A function to convert thrown exceptions
     * @param <E>
     *         The type of the exception to be thrown
     *
     * @return The value
     *
     * @throws E
     *         The transformed exception
     */
    default <E extends Exception> T get(Function<Exception, E> transformer) throws E {

        try {
            return this.get();
        } catch (Exception e) {
            throw transformer.apply(e);
        }
    }

}
