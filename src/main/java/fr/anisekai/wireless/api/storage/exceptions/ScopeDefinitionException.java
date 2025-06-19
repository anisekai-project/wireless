package fr.anisekai.wireless.api.storage.exceptions;

import fr.anisekai.wireless.api.storage.containers.AccessScope;

/**
 * Exception thrown when an {@link AccessScope} has been wrongly instantiated.
 */
public class ScopeDefinitionException extends RuntimeException {

    /**
     * Create a new {@link ScopeDefinitionException}.
     *
     * @param message
     *         The error message for this exception.
     */
    public ScopeDefinitionException(String message) {

        super(message);
    }

}
