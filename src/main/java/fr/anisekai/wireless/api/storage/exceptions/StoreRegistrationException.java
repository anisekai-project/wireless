package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;

/**
 * Exception thrown when a {@link FileStore} could not be added to a {@link LibraryManager}.
 */
public class StoreRegistrationException extends RuntimeException {

    /**
     * Create a new {@link StoreRegistrationException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StoreRegistrationException(String message) {

        super(message);
    }

    /**
     * Create a new {@link StoreRegistrationException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link StoreRegistrationException}.
     */
    public StoreRegistrationException(String message, Throwable cause) {

        super(message, cause);
    }

}
