package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.FileStore;

/**
 * Exception thrown when a {@link FileStore} query goes beyond its intended scope (i.e. going too far up the file tree)
 */
public class StoreBreakoutException extends RuntimeException {

    /**
     * Create a new {@link StoreBreakoutException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StoreBreakoutException(String message) {

        super(message);
    }

}
