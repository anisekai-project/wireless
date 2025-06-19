package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.FileStore;

/**
 * Exception thrown when unauthorized access is observed on a {@link FileStore}.
 */
public class StoreScopeException extends RuntimeException {

    /**
     * Create a new {@link StoreScopeException}
     *
     * @param message
     *         The error message for this exception
     */
    public StoreScopeException(String message) {

        super(message);
    }

}
