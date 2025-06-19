package fr.anisekai.wireless.api.storage.interfaces;

import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.exceptions.StorageAccessException;
import fr.anisekai.wireless.api.storage.exceptions.StorageForbiddenException;
import fr.anisekai.wireless.api.storage.exceptions.StorageOutOfBoundException;
import fr.anisekai.wireless.api.storage.exceptions.StorageRegistrationException;

import java.io.File;
import java.util.Collections;
import java.util.Set;

/**
 * Interface representing an object capable of managing a library.
 */
public interface Library extends StorageIsolationAware {

    /**
     * Create a {@link StorageIsolationContext} within the current {@link StorageIsolationAware} context, without any
     * {@link AccessScope} granted to it.
     *
     * @return A {@link StorageIsolationContext}.
     */
    default StorageIsolationContext createIsolation() {

        return this.createIsolation(Collections.emptySet());
    }

    /**
     * Create a {@link StorageIsolationContext} within the current {@link StorageIsolationAware} context granting it the provided
     * array of {@link AccessScope}
     *
     * @param scopes
     *         An array of {@link AccessScope} to grant.
     *
     * @return A {@link StorageIsolationContext}.
     */
    default StorageIsolationContext createIsolation(AccessScope... scopes) {

        return this.createIsolation(Set.of(scopes));
    }

    /**
     * Create a {@link StorageIsolationContext} within the current {@link StorageIsolationAware} context granting it the provided
     * set of {@link AccessScope}
     *
     * @param scopes
     *         A set of {@link AccessScope} to grant.
     *
     * @return A {@link StorageIsolationContext}.
     */
    StorageIsolationContext createIsolation(Set<AccessScope> scopes);

    /**
     * Register the provided {@link StorageStore} and apply a specific {@link StorePolicy} to it.
     *
     * @param store
     *         The {@link StorageStore} to add to this {@link Library}
     * @param policy
     *         The {@link StorePolicy} to use when {@link StorageIsolationContext} uses the {@link StorageStore}.
     *
     * @throws StorageOutOfBoundException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StorageRegistrationException
     *         If the {@link StorageStore} directory could not be created.
     * @throws StorageAccessException
     *         If an invalid action is attempted on the {@link StorageStore}
     */
    void registerStore(StorageStore store, StorePolicy policy);

    /**
     * Check if this {@link Library} contains the provided {@link StorageStore}.
     *
     * @param store
     *         The {@link StorageStore} to check.
     *
     * @return True if the {@link Library} contains the store, false otherwise.
     */
    boolean hasStore(StorageStore store);

    /**
     * Check if the current {@link StorageAware} can use the provided {@link StorageStore}.
     *
     * @param store
     *         The {@link StorageStore} to check
     *
     * @throws StorageForbiddenException
     *         If the provided {@link StorageStore} is not accessible in the current context.
     */
    void checkStore(StorageStore store);

    /**
     * Check if the current {@link StorageAware} can use the provided {@link StorageStore}.
     *
     * @param store
     *         The {@link StorageStore} to check
     * @param entity
     *         The {@link ScopedEntity} used as the {@link StorageStore} scope.
     *
     * @throws StorageForbiddenException
     *         If the provided {@link StorageStore} is not accessible in the current context.
     */
    void checkStore(StorageStore store, ScopedEntity entity);

    /**
     * Retrieve the {@link StorePolicy} used by the provided {@link StorageStore} in this {@link Library}.
     *
     * @param store
     *         The {@link StorageStore} from which to retrieve the {@link StorePolicy}
     *
     * @return A {@link StorePolicy}
     */
    StorePolicy getStorePolicy(StorageStore store);

}
