package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Exception thrown when a {@link StorageStore} query goes beyond its intended scope (i.e. going too far up the file tree)
 */
public class StorageOutOfBoundException extends RuntimeException {

    /**
     * Create a new {@link StorageOutOfBoundException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public StorageOutOfBoundException(String message) {

        super(message);
    }

}
