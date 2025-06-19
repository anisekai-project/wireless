package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;

/**
 * Exception thrown when a {@link FileIsolationContext} is used after being committed or discarded.
 */
public class ContextUnavailableException extends RuntimeException {

    /**
     * Create a new {@link ContextUnavailableException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public ContextUnavailableException(String message) {

        super(message);
    }

}
