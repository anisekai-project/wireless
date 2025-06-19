package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;

/**
 * Exception thrown when an {@link AccessScope} cannot be granted to a {@link StorageIsolationContext}.
 */
public class ScopeGrantException extends RuntimeException {

    /**
     * Create a new {@link ScopeGrantException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public ScopeGrantException(String message) {

        super(message);
    }

}
