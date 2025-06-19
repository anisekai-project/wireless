package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.interfaces.Library;

/**
 * Exception thrown when a {@link Library} could not have been initialized properly
 */
public class LibraryInitializationException extends RuntimeException {
    /**
     * Create a new {@link LibraryInitializationException}.
     *
     * @param cause
     *         The {@link Throwable} that caused this {@link LibraryInitializationException}.
     */
    public LibraryInitializationException(Throwable cause) {

        super(cause);
    }

}
