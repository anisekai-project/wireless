package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Implementation of {@link StorageStore} for {@link StoreType#ENTITY_DIRECTORY}.
 *
 * @param name
 *         The name of the {@link StorageStore}.
 * @param entityClass
 *         Type of {@link ScopedEntity} supported by this {@link StorageStore}.
 */
public record EntityDirectoryStore(String name, Class<? extends ScopedEntity> entityClass) implements StorageStore {

    @Override
    public StoreType type() {

        return StoreType.ENTITY_DIRECTORY;
    }

    @Override
    public String extension() {

        throw new UnsupportedOperationException("This store does not support extension");
    }

}
