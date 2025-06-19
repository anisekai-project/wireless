package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.enums.StoreType;

import java.nio.file.Path;

/**
 * Interface representing an object that can hold references and manage to {@link StorageStore}.
 */
public interface StorageAware {

    /**
     * Retrieve this {@link StorageAware} name.
     *
     * @return A name.
     */
    String name();

    /**
     * Retrieve this {@link StorageAware}'s root directory.
     *
     * @return A {@link Path}.
     */
    Path root();

    /**
     * Retrieve the {@link Path} pointing toward the {@link StorageStore} root within that {@link StorageAware} context.
     *
     * @param store
     *         The {@link StorageStore} from which the directory should be resolved.
     *
     * @return The {@link Path} pointing to an existing directory.
     */
    Path resolveDirectory(StorageStore store);

    /**
     * Retrieve the {@link Path} pointing to the a {@link ScopedEntity} directory in the provided {@link StorageStore} withing
     * that {@link StorageAware} context.
     *
     * @param store
     *         The {@link StorageStore} from which the directory should be resolved.
     * @param entity
     *         The {@link  ScopedEntity} from which the child directory name should be resolved.
     *
     * @return The {@link Path} pointing to an existing directory.
     */
    Path resolveDirectory(StorageStore store, ScopedEntity entity);

    /**
     * Retrieve the {@link Path} pointing to the provided {@code filename} in the provided {@link StorageStore} withing that
     * {@link StorageAware} context.
     *
     * @param store
     *         The {@link StorageStore} from which the directory should be resolved.
     * @param filename
     *         The {@code filename} within the resolved directory.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageStore store, String filename);

    /**
     * Retrieve the {@link Path} pointing to the a {@link ScopedEntity} file in the provided {@link StorageStore} withing that
     * {@link StorageAware} context.
     *
     * @param store
     *         The {@link StorageStore} from which the directory should be resolved.
     * @param entity
     *         The {@link  ScopedEntity} from which the filename should be resolved.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageStore store, ScopedEntity entity);

    /**
     * Retrieve the {@link Path} pointing to the a {@link ScopedEntity} content file in the provided {@link StorageStore} withing
     * that {@link StorageAware} context.
     *
     * @param store
     *         The {@link StorageStore} from which the directory should be resolved.
     * @param entity
     *         The {@link  ScopedEntity} from which the child directory name should be resolved.
     * @param filename
     *         The {@code filename} within the resolved {@link ScopedEntity} directory.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageStore store, ScopedEntity entity, String filename);

    /**
     * Retrieve the {@link Path} pointing toward the directory or the file granted by the provided {@link AccessScope} withing
     * that {@link StorageAware} context.
     *
     * @param scope
     *         The {@link AccessScope} to resolve.
     *
     * @return A {@link Path} pointing to a directory or a file.
     */
    default Path resolveScope(AccessScope scope) {

        if (scope.getStore().type() == StoreType.ENTITY_DIRECTORY) {
            return this.resolveDirectory(scope.getStore(), scope.getClaim());
        }
        return this.resolveFile(scope.getStore(), scope.getClaim());
    }

    /**
     * Retrieve the {@link Path} pointing toward the file granted by the provided {@link AccessScope} withing the
     * {@link StorageAware}.
     *
     * @param scope
     *         The {@link AccessScope} to resolve.
     * @param filename
     *         The file name from which the {@link Path} within the {@link AccessScope} granted directory should be resolved.
     *
     * @return A {@link Path} pointing to a file.
     */
    default Path resolveScope(AccessScope scope, String filename) {

        return this.resolveFile(scope.getStore(), scope.getClaim(), filename);
    }

}
