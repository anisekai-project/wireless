package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Implementation of {@link StorageStore} for {@link StoreType#RAW}.
 *
 * @param name
 *         The name of the {@link StorageStore}.
 */
public record RawStorageStore(String name) implements StorageStore {

    @Override
    public String name() {

        return this.name;
    }

    @Override
    public StoreType type() {

        return StoreType.RAW;
    }

    @Override
    public Class<? extends ScopedEntity> entityClass() {

        throw new UnsupportedOperationException("This store does not support scoped access");
    }

    @Override
    public String extension() {

        throw new UnsupportedOperationException("This store does not support extension");
    }

}
