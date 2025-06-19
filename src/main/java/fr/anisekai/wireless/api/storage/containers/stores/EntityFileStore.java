package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;

import java.io.File;

/**
 * Implementation of {@link FileStore} for {@link StoreType#ENTITY_FILE}.
 *
 * @param name
 *         The name of the {@link FileStore}.
 * @param entityClass
 *         Type of {@link ScopedEntity} supported by this {@link FileStore}.
 * @param extension
 *         Extension for every {@link File} contained in this {@link FileStore}.
 */
public record EntityFileStore(
        String name,
        Class<? extends ScopedEntity> entityClass,
        String extension
) implements FileStore {

    @Override
    public StoreType type() {

        return StoreType.ENTITY_FILE;
    }

}
