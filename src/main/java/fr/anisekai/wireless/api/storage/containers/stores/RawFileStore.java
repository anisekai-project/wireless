package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;

/**
 * Implementation of {@link FileStore} for {@link StoreType#RAW}.
 *
 * @param name
 *         The name of the {@link FileStore}.
 */
public record RawFileStore(String name) implements FileStore {

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

        throw new UnsupportedOperationException("This FileStore does not support AccessScopeEntity");
    }

    @Override
    public String extension() {

        throw new UnsupportedOperationException("This FileStore does not support extension");
    }

}
