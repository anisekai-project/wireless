package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.containers.AccessScope;
import org.jetbrains.annotations.NotNull;

/**
 * Represent an object that can be used to claim {@link AccessScope}.
 */
public interface ScopedEntity {

    /**
     * Retrieve the name representing this {@link ScopedEntity} to use while querying {@link FileStore}.
     *
     * @return The scoped name.
     */
    @NotNull String getScopedName();

}
