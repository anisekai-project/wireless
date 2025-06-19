package fr.anisekai.wireless.api.storage.containers.stores;

import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;

/**
 * Implementation of {@link FileStore} for {@link StoreType#ENTITY_DIRECTORY}.
 *
 * @param name
 *         The name of the {@link FileStore}.
 * @param entityClass
 *         Type of {@link ScopedEntity} supported by this {@link FileStore}.
 */
public record EntityDirectoryStore(String name, Class<? extends ScopedEntity> entityClass) implements FileStore {

    @Override
    public StoreType type() {

        return StoreType.ENTITY_DIRECTORY;
    }

    @Override
    public String extension() {

        throw new UnsupportedOperationException("This FileStore does not support extension");
    }

}
