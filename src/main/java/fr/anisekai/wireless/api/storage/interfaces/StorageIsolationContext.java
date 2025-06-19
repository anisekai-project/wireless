package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.containers.AccessScope;

import java.nio.file.Path;

/**
 * Interface representing an isolated library.
 * <p>
 * An isolated library is a directory within the main library that serve as a workspace mirror, allowing programs to write files
 * to it without ever touching the original library.
 */
public interface StorageIsolationContext extends StorageAware, AutoCloseable {

    /**
     * Retrieve the {@link StorageIsolationAware} which create this {@link StorageIsolationContext}.
     *
     * @return The owning {@link StorageIsolationAware}.
     */
    StorageIsolationAware owner();

    @Override
    default Path root() {

        return this.owner().resolveDirectory(this);
    }

    /**
     * Try to claim additional {@link AccessScope} for this {@link StorageIsolationContext}.
     *
     * @param scopes
     *         The {@link AccessScope} to grant.
     */
    default void requestScope(AccessScope... scopes) {

        this.owner().requestScope(this, scopes);
    }

    @Override
    default Path resolveDirectory(StorageStore store) {

        return this.owner().resolveDirectory(this, store);
    }

    @Override
    default Path resolveDirectory(StorageStore store, ScopedEntity entity) {

        return this.owner().resolveDirectory(this, store, entity);
    }

    @Override
    default Path resolveFile(StorageStore store, String filename) {

        return this.owner().resolveFile(this, store, filename);
    }

    @Override
    default Path resolveFile(StorageStore store, ScopedEntity entity) {

        return this.owner().resolveFile(this, store, entity);
    }

    @Override
    default Path resolveFile(StorageStore store, ScopedEntity entity, String filename) {

        return this.owner().resolveFile(this, store, entity, filename);
    }

    /**
     * Retrieve a {@link Path} pointing to a temporary file.
     *
     * @param extension
     *         The file extension to use for the temporary file.
     *
     * @return A {@link Path} pointing to a temporary file.
     */
    default Path requestTemporaryFile(String extension) {

        return this.owner().requestTemporaryFile(this, extension);
    }

    /**
     * Request the owning {@link StorageIsolationAware} to commit this {@link StorageIsolationContext} to the disk.
     */
    default void commit() {

        this.owner().commit(this);
    }

    @Override
    default void close() {

        this.owner().discard(this);
    }

}
