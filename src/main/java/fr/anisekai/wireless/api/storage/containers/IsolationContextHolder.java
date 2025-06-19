package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.exceptions.ContextUnavailableException;
import fr.anisekai.wireless.api.storage.exceptions.StorageForbiddenException;
import fr.anisekai.wireless.api.storage.interfaces.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;

/**
 * Class holding the data related to a {@link StorageIsolationContext} and its {@link AccessScope}.
 */
public final class IsolationContextHolder implements StorageAware, IsolationScopeEntity {

    private final Library                 owner;
    private final StorageAware            resolver;
    private final Set<AccessScope>        scopes;
    private final StorageIsolationContext isolation;
    private       boolean                 committed = false;

    /**
     * Create a new {@link IsolationContextHolder}
     *
     * @param owner
     *         The {@link Library} owning this {@link IsolationContextHolder}.
     * @param root
     *         {@link File} into which the {@link StorageIsolationContext} is active.
     * @param scopes
     *         A {@link Set} of {@link AccessScope} granted to the {@link AccessScope}
     * @param isolation
     *         The {@link StorageIsolationContext}.
     */
    public IsolationContextHolder(Library owner, Path root, Set<AccessScope> scopes, StorageIsolationContext isolation) {

        this.owner     = owner;
        this.scopes    = scopes;
        this.isolation = isolation;
        this.resolver  = new StoragePathResolver(root);
    }

    /**
     * Retrieve this {@link IsolationContextHolder}'s set of claimed {@link AccessScope}. The returned set is a copy so modifying
     * it will not have any effect on this {@link IsolationContextHolder}. To grant additional {@link AccessScope}, use
     * {@link #grant(AccessScope)}.
     *
     * @return A set of {@link AccessScope}.
     */
    public Set<AccessScope> grants() {

        return Set.copyOf(this.scopes);
    }

    /**
     * Grant an {@link AccessScope} to this {@link IsolationContextHolder}.
     *
     * @param scope
     *         The {@link AccessScope} to grant.
     */
    public void grant(AccessScope scope) {

        this.scopes.add(scope);
    }

    /**
     * Retrieve the {@link StorageIsolationContext} for which this {@link IsolationContextHolder} has been created.
     *
     * @return A {@link StorageIsolationContext}.
     */
    public StorageIsolationContext getIsolation() {

        return this.isolation;
    }

    /**
     * Check if this {@link IsolationContextHolder}'s {@link StorageIsolationContext} has already been committed.
     *
     * @return True if the {@link StorageIsolationContext} has been committed, false otherwise.
     */
    public boolean isCommitted() {

        return this.committed;
    }

    /**
     * Define if this {@link IsolationContextHolder}'s {@link StorageIsolationContext} has been committed.
     *
     * @param committed
     *         True if the {@link StorageIsolationContext} has been committed, false otherwise.
     */
    public void setCommitted(boolean committed) {

        this.committed = committed;
    }

    @Override
    public String name() {

        return this.resolver.name();
    }

    @Override
    public Path root() {

        return this.resolver.root();
    }

    private void checkSelf() {

        if (this.isCommitted()) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already committed and cannot be used anymore.",
                    this.name()
            ));
        }
    }

    private void checkStore(StorageStore store) {

        this.owner.checkStore(store);
        StorePolicy policy = this.owner.getStorePolicy(store);

        if (policy == StorePolicy.PRIVATE) {
            throw new StorageForbiddenException(String.format(
                    "The store '%s' has been declared as private by the library.",
                    store.name()
            ));
        }
    }

    private void checkStore(StorageStore store, ScopedEntity entity) {

        this.owner.checkStore(store, entity);
        StorePolicy policy = this.owner.getStorePolicy(store);

        if (policy == StorePolicy.PRIVATE) {
            throw new StorageForbiddenException(String.format(
                    "The store '%s' has been declared as private by the library.",
                    store.name()
            ));
        }

        if (policy == StorePolicy.DISCARD) {
            return; // Implicit grant — will not modify the library
        }

        AccessScope scope = new AccessScope(store, entity);
        if (!this.scopes.contains(scope)) {
            throw new StorageForbiddenException(String.format(
                    "The scope '%s' has not been granted to this isolation context.",
                    scope
            ));
        }
    }

    private void checkStore(StorageStore store, ScopedEntity entity, String filename) {

        this.checkStore(store, entity);
        if (store.type() == StoreType.ENTITY_FILE) {
            throw new StorageForbiddenException(String.format(
                    "The store '%s' does not allow custom filenames.",
                    store.name()
            ));
        }
    }

    @Override
    public Path resolveDirectory(StorageStore store) {

        this.checkStore(store);
        this.checkSelf();
        return this.resolver.resolveDirectory(store);
    }

    @Override
    public Path resolveDirectory(StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        this.checkSelf();
        return this.resolver.resolveDirectory(store, entity);
    }

    @Override
    public Path resolveFile(StorageStore store, String filename) {

        this.checkStore(store);
        this.checkSelf();
        return this.resolver.resolveFile(store, filename);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        this.checkSelf();
        return this.resolver.resolveFile(store, entity);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity, String filename) {

        this.checkStore(store, entity, filename);
        this.checkSelf();
        return this.resolver.resolveFile(store, entity, filename);
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IsolationContextHolder) obj;
        return Objects.equals(this.name(), that.name()) &&
                Objects.equals(this.root(), that.root()) &&
                Objects.equals(this.scopes, that.scopes) &&
                Objects.equals(this.isolation, that.isolation);
    }

    @Override
    public @NotNull String getScopedName() {

        return this.name();
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.name(), this.root(), this.scopes, this.isolation);
    }

    @Override
    public String toString() {

        return String.format(
                "IsolationContextHolder{name='%s', root=%s, isolation=%s, committed=%s}",
                this.name(),
                this.root(),
                this.isolation,
                this.committed
        );
    }

}
