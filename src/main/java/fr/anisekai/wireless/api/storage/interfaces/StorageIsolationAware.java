package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.containers.AccessScope;

import java.nio.file.Path;

/**
 * Interface representing an object that can manage and hold references to {@link StorageIsolationContext}.
 */
public interface StorageIsolationAware extends StorageAware, AutoCloseable {

    /**
     * Try to claim additional {@link AccessScope} for the provided {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} for which additional {@link AccessScope} should be granted.
     * @param scopes
     *         The {@link AccessScope} to grant.
     */
    void requestScope(StorageIsolationContext context, AccessScope... scopes);

    /**
     * Commits the contents of the given {@link StorageIsolationContext} to the library, applying the corresponding
     * {@link StorageStore} policies defined in this {@link Library}. All {@link AccessScope} claims associated with the
     * isolation context are released upon commit.
     * <p>
     * After a context has been committed, and any further use of the same {@link StorageIsolationContext} is considered invalid.
     * <p>
     * Partial failure during commit may result in some {@link AccessScope} being updated and others not, as each
     * {@link AccessScope} is committed independently.
     *
     * @param context
     *         The {@link StorageIsolationContext} to commit.
     */
    void commit(StorageIsolationContext context);


    /**
     * Discard the provided {@link StorageIsolationContext}, releasing all {@link AccessScope} claimed. The
     * {@link StorageIsolationContext} content will be deleted on a best-effort basis, but has no impact on the overall library.
     *
     * @param context
     *         The {@link StorageIsolationContext} to drop.
     */
    void discard(StorageIsolationContext context);

    /**
     * Call this method will discard every {@link StorageIsolationContext} in this {@link StorageIsolationAware} without
     * committing them.
     *
     * @throws Exception
     *         If something happen during discard.
     */
    @Override
    void close() throws Exception;

    /**
     * Retrieve the {@link Path} pointing toward the {@link StorageIsolationContext} directory within that
     * {@link StorageIsolationAware}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     *
     * @return The {@link Path} pointing to an existing directory.
     */
    Path resolveDirectory(StorageIsolationContext context);

    /**
     * Retrieve the {@link Path} pointing toward the {@link StorageStore} directory within the {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param store
     *         The {@link StorageStore} from which the directory within the {@link StorageIsolationContext} should be resolved.
     *
     * @return The {@link Path} pointing to an existing directory.
     */
    Path resolveDirectory(StorageIsolationContext context, StorageStore store);

    /**
     * Retrieve the {@link Path} pointing toward the {@link ScopedEntity} directory within the {@link StorageStore} within the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param store
     *         The {@link StorageStore} from which the directory within the {@link StorageIsolationContext} should be resolved.
     * @param entity
     *         The {@link ScopedEntity} from which the directory within the {@link StorageStore} should be resolved.
     *
     * @return The {@link Path} pointing to an existing directory.
     */
    Path resolveDirectory(StorageIsolationContext context, StorageStore store, ScopedEntity entity);

    /**
     * Retrieve the {@link Path} pointing toward the {@code filename} within the {@link StorageStore} within the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param store
     *         The {@link StorageStore} from which the directory within the {@link StorageIsolationContext} should be resolved.
     * @param filename
     *         The file name from which the {@link Path} within the {@link StorageStore} should be resolved.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageIsolationContext context, StorageStore store, String filename);

    /**
     * Retrieve the {@link Path} pointing toward the {@link ScopedEntity} file within the {@link StorageStore} within the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param store
     *         The {@link StorageStore} from which the directory within the {@link StorageIsolationContext} should be resolved.
     * @param entity
     *         The {@link ScopedEntity} from which the directory within the {@link StorageStore} should be resolved.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageIsolationContext context, StorageStore store, ScopedEntity entity);

    /**
     * Retrieve the {@link Path} pointing toward the {@code filename} within the {@link ScopedEntity} directory within the
     * {@link StorageStore} within the {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param store
     *         The {@link StorageStore} from which the directory within the {@link StorageIsolationContext} should be resolved.
     * @param entity
     *         The {@link ScopedEntity} from which the directory within the {@link StorageStore} should be resolved.
     * @param filename
     *         The file name from which the {@link Path} within the {@link ScopedEntity} directory should be resolved.
     *
     * @return The {@link Path} pointing to a file.
     */
    Path resolveFile(StorageIsolationContext context, StorageStore store, ScopedEntity entity, String filename);

    /**
     * Retrieve the {@link Path} pointing toward the directory granted by the provided {@link AccessScope} withing the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param scope
     *         The {@link AccessScope} to resolve.
     *
     * @return A {@link Path} pointing to a directory.
     */
    default Path resolveDirectory(StorageIsolationContext context, AccessScope scope) {

        return this.resolveDirectory(scope.getStore(), scope.getClaim());
    }

    /**
     * Retrieve the {@link Path} pointing toward the file granted by the provided {@link AccessScope} withing the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param scope
     *         The {@link AccessScope} to resolve.
     *
     * @return A {@link Path} pointing to a file.
     */
    default Path resolveFile(StorageIsolationContext context, AccessScope scope) {

        return this.resolveFile(scope.getStore(), scope.getClaim());
    }

    /**
     * Retrieve the {@link Path} pointing toward the file granted by the provided {@link AccessScope} withing the
     * {@link StorageIsolationContext}.
     *
     * @param context
     *         The {@link StorageIsolationContext} from which the directory should be resolved.
     * @param scope
     *         The {@link AccessScope} to resolve.
     * @param filename
     *         The file name from which the {@link Path} within the {@link AccessScope} granted directory should be resolved.
     *
     * @return A {@link Path} pointing to a file.
     */
    default Path resolveFile(StorageIsolationContext context, AccessScope scope, String filename) {

        return this.resolveFile(scope.getStore(), scope.getClaim(), filename);
    }

    /**
     * Retrieve a {@link Path} pointing to a temporary file.
     *
     * @param context
     *         The {@link StorageIsolationContext} requesting the temporary file.
     * @param extension
     *         The file extension to use for the temporary file.
     *
     * @return A {@link Path} pointing to a temporary file.
     */
    Path requestTemporaryFile(StorageIsolationContext context, String extension);

}
