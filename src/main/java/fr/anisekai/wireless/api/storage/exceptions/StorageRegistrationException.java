package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Exception thrown when a {@link StorageStore} could not be added to a {@link LibraryManager}.
 */
public class StorageRegistrationException extends RuntimeException {

    /**
     * Create a new {@link StorageRegistrationException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StorageRegistrationException(String message) {

        super(message);
    }

    /**
     * Create a new {@link StorageRegistrationException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link StorageRegistrationException}.
     */
    public StorageRegistrationException(String message, Throwable cause) {

        super(message, cause);
    }

}
