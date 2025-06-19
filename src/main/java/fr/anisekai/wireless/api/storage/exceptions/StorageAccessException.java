package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Exception thrown when an invalid operation is executed on a {@link StorageStore}, or when the {@link StorageStore} content
 * cannot be accessed.
 */
public class StorageAccessException extends RuntimeException {

    /**
     * Create a new {@link StorageAccessException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StorageAccessException(String message) {

        super(message);
    }

    /**
     * Create a new {@link StorageAccessException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link StorageAccessException}.
     */
    public StorageAccessException(String message, Throwable cause) {

        super(message, cause);
    }

    /**
     * Create a new {@link StorageAccessException}.
     *
     * @param cause
     *         The {@link Throwable} that caused this {@link StorageAccessException}.
     */
    public StorageAccessException(Throwable cause) {

        super(cause);
    }

}
