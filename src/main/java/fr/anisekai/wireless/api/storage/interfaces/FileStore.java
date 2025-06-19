package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.enums.StoreType;

/**
 * Interface representing a storage space within a library for a specific content.
 */
public interface FileStore {

    /**
     * Retrieve this {@link FileStore}'s {@link StoreType}.
     *
     * @return A {@link StoreType}.
     */
    StoreType type();

    /**
     * Retrieve to which type of {@link ScopedEntity} this {@link FileStore} is associated. Can only be used if the current
     * {@link #type()} is {@link StoreType#RAW}.
     *
     * @return An {@link ScopedEntity} class.
     */
    Class<? extends ScopedEntity> entityClass();

    /**
     * Retrieve the file extension this {@link FileStore} uses. Can only be used if the current {@link #type()} is
     * {@link StoreType#ENTITY_FILE}.
     *
     * @return A file extension
     */
    String extension();

    /**
     * Retrieve the name of this {@link FileStore}.
     *
     * @return The name on the disk.
     */
    String name();

}
