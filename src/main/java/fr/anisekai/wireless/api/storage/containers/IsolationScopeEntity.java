package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.Library;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;

/**
 * Interface used as placeholder to resolve a named {@link IsolationContextHolder}.
 */
public interface IsolationScopeEntity extends ScopedEntity {

    /**
     * Create a fake {@link IsolationScopeEntity} just to resolve a {@link IsolationContextHolder} directory in a
     * {@link Library}.
     *
     * @param name
     *         The name of the original {@link IsolationContextHolder}.
     *
     * @return A placeholder
     */
    static IsolationScopeEntity of(String name) {

        return () -> name;
    }

    /**
     * Create a placeholder {@link IsolationScopeEntity} with a random name.
     *
     * @return A placeholder.
     */
    static IsolationScopeEntity random() {

        return of(LibraryManager.getRandomName());
    }

}
