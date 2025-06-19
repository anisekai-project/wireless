package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;

/**
 * Exception thrown when an error occurs while a {@link FileIsolationContext} is being discarded. This exception will always have
 * a cause.
 */
public class ContextDiscardException extends RuntimeException {

    /**
     * Create a new {@link ContextDiscardException}.
     *
     * @param message
     *         The error message for this exception.
     * @param cause
     *         The {@link Throwable} that caused this {@link ContextDiscardException}.
     */
    public ContextDiscardException(String message, Throwable cause) {

        super(message, cause);
    }

}
