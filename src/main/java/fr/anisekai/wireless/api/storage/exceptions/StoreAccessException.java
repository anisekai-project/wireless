package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.FileStore;

/**
 * Exception thrown when an invalid operation is executed on a {@link FileStore}, or when the {@link FileStore} content cannot be
 * accessed.
 */
public class StoreAccessException extends RuntimeException {

    /**
     * Create a new {@link StoreAccessException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StoreAccessException(String message) {

        super(message);
    }

    /**
     * Create a new {@link StoreAccessException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link StoreAccessException}.
     */
    public StoreAccessException(String message, Throwable cause) {

        super(message, cause);
    }

}
