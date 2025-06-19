package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;

/**
 * Exception thrown when an error occurs while a {@link StorageIsolationContext} is being committed. This exception will always have
 * a cause.
 */
public class ContextCommitException extends RuntimeException {

    /**
     * Create a new {@link ContextCommitException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link ContextCommitException}.
     */
    public ContextCommitException(String message, Throwable cause) {

        super(message, cause);
    }

}
