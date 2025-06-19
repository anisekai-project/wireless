package fr.anisekai.wireless.api.storage.enums;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.services.Transmission;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Enums representing one of the type possible for a {@link StorageStore}
 */
public enum StoreType {

    /**
     * The {@link StorageStore} is a reference to a directory containing one directory for each {@link Entity}.
     */
    ENTITY_DIRECTORY(true),

    /**
     * The {@link StorageStore} is a reference to a directory containing one file for each {@link Entity}.
     */
    ENTITY_FILE(true),

    /**
     * The {@link StorageStore} is a reference to a directory without proper structure. Most of the time used for
     * {@link Transmission} downloads or temporary files.
     */
    RAW(false);

    private final boolean entityScoped;

    StoreType(boolean entityScoped) {

        this.entityScoped = entityScoped;
    }

    /**
     * Check if the current {@link StoreType} require an {@link Entity} to be used.
     *
     * @return True if it depends on an {@link Entity}, false otherwise.
     */
    public boolean isEntityScoped() {

        return this.entityScoped;
    }
}
