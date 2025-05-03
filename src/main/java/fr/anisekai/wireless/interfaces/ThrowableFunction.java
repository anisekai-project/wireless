package fr.anisekai.wireless.interfaces;

import java.util.function.Function;

/**
 * A functional interface similar to {@link Function}, but designed to allow the throwing of checked exceptions.
 * <p>
 * This is useful when working with lambda expressions or method references that may throw checked exceptions, which are not
 * supported by standard functional interfaces.
 *
 * @param <I>
 *         The input type.
 * @param <O>
 *         The output type.
 */
public interface ThrowableFunction<I, O> {

    /**
     * Applies this function to the given input, potentially throwing a checked exception.
     *
     * @param input
     *         The input value
     *
     * @return The function result
     *
     * @throws Exception
     *         Threw if an error occurs during function execution
     */
    O apply(I input) throws Exception;

    /**
     * Applies this function to the given input, and transforms any thrown exception using the provided transformer.
     * <p>
     * This allows callers to wrap or convert checked exceptions into runtime exceptions or custom exception types.
     *
     * @param input
     *         The input value
     * @param transformer
     *         A function to transform thrown exceptions into a specific type
     * @param <E>
     *         The type of the exception to be thrown
     *
     * @return The function result
     *
     * @throws E
     *         The transformed exception
     */
    default <E extends Exception> O apply(I input, Function<Exception, E> transformer) throws E {

        try {
            return this.apply(input);
        } catch (Exception e) {
            throw transformer.apply(e);
        }
    }

}
