package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.exceptions.ScopeGrantException;
import fr.anisekai.wireless.api.storage.exceptions.StoreAccessException;
import fr.anisekai.wireless.api.storage.exceptions.StoreBreakoutException;
import fr.anisekai.wireless.api.storage.exceptions.StoreRegistrationException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Interface representing an isolated library.
 * <p>
 * An isolated library is a directory within another that serve as a workspace mirror, allowing programs to write files to it
 * without every touching the original library.
 */
public interface FileIsolationContext extends AutoCloseable {

    /**
     * Retrieve the {@link LibraryManager} which created this {@link FileIsolationContext}.
     *
     * @return A {@link LibraryManager}.
     */
    LibraryManager manager();

    /**
     * Retrieve this {@link FileIsolationContext} name.
     *
     * @return A name.
     */
    String name();

    /**
     * Try to store the provided {@link InputStream} in this {@link FileIsolationContext}.
     *
     * @param scope
     *         The {@link AccessScope} to use for the write query.
     * @param is
     *         The {@link InputStream} to write.
     *
     * @return The {@link File} into which the content has been written.
     *
     * @throws IOException
     *         If an error occurs while trying to write the file.
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}.
     */
    default File store(AccessScope scope, InputStream is) throws IOException {

        return this.manager().store(this, scope.getFileStore(), scope.getClaim(), is);
    }

    /**
     * Try to store the provided {@link InputStream} in the {@link FileIsolationContext}.
     *
     * @param scope
     *         The {@link AccessScope} to use for the write query.
     * @param name
     *         The filename of the {@link File} into which the {@link InputStream} content will be written
     * @param is
     *         The {@link InputStream} to write.
     *
     * @return The {@link File} into which the content has been written.
     *
     * @throws IOException
     *         If an error occurs while trying to write the file.
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}.
     */
    default File store(AccessScope scope, String name, InputStream is) throws IOException {

        return this.manager().store(this, scope.getFileStore(), scope.getClaim(), name, is);
    }

    /**
     * Retrieve the {@link File} pointing to the {@link FileStore} directory within this {@link FileIsolationContext}.
     *
     * @param store
     *         The {@link FileStore} to retrieve.
     *
     * @return A {@link File}
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    default File getStoreFile(FileStore store) {

        return this.manager().getStoreFile(this, store);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory within this {@link FileIsolationContext}.
     *
     * @param store
     *         The {@link FileStore} to retrieve.
     * @param entity
     *         The {@link ScopedEntity} to retrieve within the {@link FileStore}
     *
     * @return A {@link File} pointing to either a file representing the {@link ScopedEntity} (if {@link FileStore#type()} is
     *         {@link StoreType#ENTITY_FILE}) or an existing directory (if {@link FileStore#type()} is
     *         {@link StoreType#ENTITY_DIRECTORY})
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    default File getStoreFile(FileStore store, ScopedEntity entity) {

        return this.manager().getStoreFile(this, store, entity);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory within this {@link FileIsolationContext}.
     *
     * @param store
     *         The {@link FileStore} to retrieve.
     * @param entity
     *         The {@link ScopedEntity} to retrieve within the {@link FileStore}
     * @param name
     *         The filename to retrieve within the {@link FileStore}
     *
     * @return A {@link File} pointing toward a file.
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    default File getStoreFile(FileStore store, ScopedEntity entity, String name) {

        return this.manager().getStoreFile(this, store, entity, name);
    }

    /**
     * Request a temporary {@link File} in this {@link FileIsolationContext}.
     *
     * @param extension
     *         The file extension to give to the temporary {@link File}
     *
     * @return A {@link File} to use as temporary file.
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}.
     */
    default File requestTemporaryFile(String extension) {

        return this.manager().requestTemporaryFile(this, extension);
    }

    /**
     * Request a new {@link AccessScope} for this {@link FileIsolationContext}.
     *
     * @param scope
     *         The {@link AccessScope} to claim.
     *
     * @throws ScopeGrantException
     *         If the {@link AccessScope} could not be granted to the current {@link FileIsolationContext}
     */
    default void requestScope(AccessScope scope) {

        this.manager().requestScope(this, scope);
    }

    /**
     * Commit this {@link FileIsolationContext} to the owning {@link LibraryManager}.
     */
    default void commit() {

        this.manager().commit(this);
    }

    @Override
    default void close() {

        this.manager().discard(this);
    }

}
