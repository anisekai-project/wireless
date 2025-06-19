package fr.anisekai.wireless.interfaces;

/**
 * Represents an operation that does not return a result but can throw an {@link Exception}.
 */
public interface ThrowableRunnable {

    /**
     * Run the action
     *
     * @throws Exception
     *         If the action throws an error.
     */
    void run() throws Exception;

}
