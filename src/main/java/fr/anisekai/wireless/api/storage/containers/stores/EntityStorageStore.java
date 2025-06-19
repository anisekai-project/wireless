package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

import java.io.File;

/**
 * Implementation of {@link StorageStore} for {@link StoreType#ENTITY_FILE}.
 *
 * @param name
 *         The name of the {@link StorageStore}.
 * @param entityClass
 *         Type of {@link ScopedEntity} supported by this {@link StorageStore}.
 * @param extension
 *         Extension for every {@link File} contained in this {@link StorageStore}.
 */
public record EntityStorageStore(
        String name,
        Class<? extends ScopedEntity> entityClass,
        String extension
) implements StorageStore {

    @Override
    public StoreType type() {

        return StoreType.ENTITY_FILE;
    }

}
