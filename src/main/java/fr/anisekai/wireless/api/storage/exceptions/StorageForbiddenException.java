package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Exception thrown when unauthorized access is observed on a {@link StorageStore}.
 */
public class StorageForbiddenException extends RuntimeException {

    /**
     * Create a new {@link StorageForbiddenException}
     *
     * @param message
     *         The error message for this exception
     */
    public StorageForbiddenException(String message) {

        super(message);
    }

}
