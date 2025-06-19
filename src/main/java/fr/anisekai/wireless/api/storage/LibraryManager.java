package fr.anisekai.wireless.api.storage;

import fr.anisekai.wireless.api.storage.containers.*;
import fr.anisekai.wireless.api.storage.containers.stores.EntityDirectoryStore;
import fr.anisekai.wireless.api.storage.containers.stores.RawStorageStore;
import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.exceptions.*;
import fr.anisekai.wireless.api.storage.interfaces.Library;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;
import fr.anisekai.wireless.utils.FileUtils;
import fr.anisekai.wireless.utils.FlowUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class allowing the structured management of a directory using {@link StorageStore}.
 */
public class LibraryManager implements Library {

    private static final StorageStore STORE_TMP       = new RawStorageStore("tmp");
    private static final StorageStore STORE_ISOLATION = new EntityDirectoryStore("isolation", IsolationScopeEntity.class);

    private final Map<StorageStore, StorePolicy>      stores;
    private final Map<String, IsolationContextHolder> contexts;
    private final StoragePathResolver                 resolver;

    /**
     * Create a random name that can be used either as temporary file name or as a name for a {@link StorageIsolationContext}.
     *
     * @return A random name.
     */
    public static String getRandomName() {

        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    /**
     * Create a {@link LibraryManager} targeting the provided {@link File}. If the targeted directory does not exist, it will be
     * automatically created.
     *
     * @param root
     *         A {@link File}
     */
    public LibraryManager(Path root) {

        this.resolver = new StoragePathResolver(root);
        this.contexts = new HashMap<>();
        this.stores   = new HashMap<>();

        FlowUtils.wrapException(() -> FileUtils.ensureDirectory(this.resolver.root()), LibraryInitializationException::new);
        // Internal store
        this.registerStore(STORE_TMP, StorePolicy.DISCARD);
        this.registerStore(STORE_ISOLATION, StorePolicy.PRIVATE);
    }

    private IsolationContextHolder getIsolationHolder(StorageIsolationContext isolation) {

        if (!isolation.owner().equals(this)) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' does not belong to this library.",
                    isolation.name()
            ));
        }

        if (!this.contexts.containsKey(isolation.name())) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' has already been discarded.",
                    isolation.name()
            ));
        }

        IsolationContextHolder holder = this.contexts.get(isolation.name());

        if (!holder.name().equals(isolation.name())) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' has a dirty state.",
                    isolation.name()
            ));
        }

        return holder;
    }

    private void checkScopes(Iterable<AccessScope> scopes) {

        Set<AccessScope> claimedScopes = this.contexts
                .values()
                .stream()
                .map(IsolationContextHolder::grants)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (AccessScope requestedScope : scopes) {
            this.checkStore(requestedScope.getStore(), requestedScope.getClaim());

            if (claimedScopes.contains(requestedScope)) {
                throw new ScopeGrantException(String.format(
                        "Scope '%s' is already claimed by another isolation context.",
                        requestedScope
                ));
            }

            if (this.stores.get(requestedScope.getStore()) == StorePolicy.PRIVATE) {
                throw new ScopeGrantException(String.format(
                        "Scope '%s' is target a private store.",
                        requestedScope
                ));
            }
        }
    }

    @Override
    public String name() {

        return this.resolver.name();
    }

    @Override
    public Path root() {

        return this.resolver.root();
    }

    /**
     * Register the provided {@link StorageStore} and apply a specific {@link StorePolicy} to it.
     *
     * @param store
     *         The {@link StorageStore} to add to this {@link LibraryManager}
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
    @Override
    public void registerStore(StorageStore store, StorePolicy policy) {

        if (!store.type().isEntityScoped() && policy.allowEditingFile()) {
            throw new StorageRegistrationException(String.format(
                    "The '%s' scoped store cannot be registered under the '%s' policy.",
                    store.name(),
                    policy.name()
            ));
        }

        if (this.stores.keySet().stream().anyMatch(registeredStore -> registeredStore.name().equals(store.name()))) {
            throw new StorageRegistrationException(String.format(
                    "A store with the name '%s' is already registered.",
                    store.name()
            ));
        }

        try {
            this.resolver.resolveDirectory(store);
        } catch (Exception e) {
            throw new StorageRegistrationException(String.format("The '%s' store could not be registered.", store.name()), e);
        }

        this.stores.put(store, policy);
    }

    @Override
    public StorePolicy getStorePolicy(StorageStore store) {

        if (!this.hasStore(store)) {
            throw new StorageAccessException(String.format(
                    "The store '%s' is not registered on the library",
                    store.name()
            ));
        }

        return this.stores.get(store);
    }

    @Override
    public boolean hasStore(StorageStore store) {

        return this.stores.containsKey(store);
    }

    @Override
    public void checkStore(StorageStore store) {

        if (!this.hasStore(store)) {
            throw new StorageAccessException(String.format(
                    "The store '%s' is not registered on the library",
                    store.name()
            ));
        }

        if (store.type().isEntityScoped()) {
            throw new StorageAccessException(String.format(
                    "The store '%s' is not a raw store.",
                    store.name()
            ));
        }
    }

    @Override
    public void checkStore(StorageStore store, ScopedEntity entity) {

        if (!this.hasStore(store)) {
            throw new StorageAccessException(String.format(
                    "The store '%s' is not registered on the library",
                    store.name()
            ));
        }

        if (!store.type().isEntityScoped()) {
            throw new StorageAccessException(String.format(
                    "The store '%s' is not a scoped store.",
                    store.name()
            ));
        }
    }

    @Override
    public Path resolveDirectory(StorageStore store) {

        this.checkStore(store);
        return this.resolver.resolveDirectory(store);
    }

    @Override
    public Path resolveDirectory(StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        return this.resolver.resolveDirectory(store, entity);
    }

    @Override
    public Path resolveFile(StorageStore store, String filename) {

        this.checkStore(store);
        return this.resolver.resolveFile(store, filename);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        return this.resolver.resolveFile(store, entity);
    }

    @Override
    public Path resolveFile(StorageStore store, ScopedEntity entity, String filename) {

        this.checkStore(store, entity);
        return this.resolver.resolveFile(store, entity, filename);
    }

    @Override
    public Path resolveDirectory(StorageIsolationContext context) {

        return this.getIsolationHolder(context).root();
    }

    @Override
    public Path resolveDirectory(StorageIsolationContext context, StorageStore store) {

        this.checkStore(store);
        return this.getIsolationHolder(context).resolveDirectory(store);
    }

    @Override
    public Path resolveDirectory(StorageIsolationContext context, StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        return this.getIsolationHolder(context).resolveDirectory(store, entity);
    }

    @Override
    public Path resolveFile(StorageIsolationContext context, StorageStore store, String filename) {

        this.checkStore(store);
        return this.getIsolationHolder(context).resolveFile(store, filename);
    }

    @Override
    public Path resolveFile(StorageIsolationContext context, StorageStore store, ScopedEntity entity) {

        this.checkStore(store, entity);
        return this.getIsolationHolder(context).resolveFile(store, entity);
    }

    @Override
    public Path resolveFile(StorageIsolationContext context, StorageStore store, ScopedEntity entity, String filename) {

        this.checkStore(store, entity);
        return this.getIsolationHolder(context).resolveFile(store, entity, filename);
    }

    @Override
    public StorageIsolationContext createIsolation(Set<AccessScope> scopes) {

        this.checkScopes(scopes);

        IsolationScopeEntity entity = IsolationScopeEntity.random();
        String               name   = entity.getScopedName();

        Path path = this.resolver.resolveDirectory(STORE_ISOLATION, entity);

        StorageIsolationContext isolation = new IsolationContext(this, name);
        IsolationContextHolder  holder    = new IsolationContextHolder(this, path, new HashSet<>(scopes), isolation);

        this.contexts.put(name, holder);
        return isolation;
    }

    @Override
    public void requestScope(StorageIsolationContext context, AccessScope... scopes) {

        Set<AccessScope> requestedScopes = Set.of(scopes);
        this.checkScopes(requestedScopes);

        IsolationContextHolder holder = this.getIsolationHolder(context);
        requestedScopes.forEach(holder::grant);
    }

    @Override
    public Path requestTemporaryFile(StorageIsolationContext isolation, String extension) {

        String name = String.format("%s.%s", getRandomName(), extension);
        return this.getIsolationHolder(isolation).resolveFile(STORE_TMP, name);
    }

    @Override
    public void commit(StorageIsolationContext isolation) {

        IsolationContextHolder context = this.getIsolationHolder(isolation);

        if (context.isCommitted()) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already committed and cannot be used anymore.",
                    context.name()
            ));
        }

        for (AccessScope scope : context.grants()) {
            try {
                this.commitScope(isolation, scope);
            } catch (IOException e) {
                throw new ContextCommitException(String.format("Failed to commit scope '%s'.", scope), e);
            }
        }

        context.setCommitted(true);
    }

    /**
     * Commits the contents of the given {@link IsolationContextHolder} under the {@link AccessScope} to the library, applying the
     * corresponding {@link StorageStore} policies defined in this {@link LibraryManager}.
     *
     * @param storage
     *         The {@link StorageIsolationContext} to commit.
     * @param scope
     *         The {@link AccessScope} to commit.
     */
    private void commitScope(StorageIsolationContext storage, AccessScope scope) throws IOException {

        StorageStore store  = scope.getStore();
        StorePolicy  policy = this.stores.get(store);

        if (policy == StorePolicy.DISCARD) return;
        if (!store.type().isEntityScoped()) return;

        Path    contextPath   = this.resolver.resolveDirectory(store);
        Path    localPath     = this.resolver.resolveScope(scope);
        Path    isolationPath = storage.resolveScope(scope);
        Path    safeLocalPath = contextPath.resolve("." + localPath.getFileName().toString());
        boolean hasBackup     = false;

        FileUtils.delete(safeLocalPath);

        if (Files.exists(localPath)) {
            FileUtils.copy(localPath, safeLocalPath);
            hasBackup = true;
        }

        try {
            if (store.type() == StoreType.ENTITY_DIRECTORY && policy == StorePolicy.FULL_SWAP) {
                FileUtils.delete(localPath);
                FileUtils.copy(isolationPath, localPath, StandardCopyOption.COPY_ATTRIBUTES);
            } else if (store.type() == StoreType.ENTITY_DIRECTORY && policy == StorePolicy.OVERWRITE) {
                FileUtils.copy(isolationPath, localPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            } else if (store.type() == StoreType.ENTITY_FILE) {
                if (policy == StorePolicy.FULL_SWAP) {
                    FileUtils.delete(localPath);
                }
                if (Files.isRegularFile(isolationPath)) {
                    FileUtils.copy(
                            isolationPath,
                            localPath,
                            StandardCopyOption.COPY_ATTRIBUTES,
                            StandardCopyOption.REPLACE_EXISTING
                    );
                }
            }
        } catch (Exception e) {
            // Avoid partial commit
            FileUtils.delete(localPath);
            if (hasBackup) Files.move(safeLocalPath, localPath);
        } finally {
            if (hasBackup) FileUtils.delete(safeLocalPath);
        }
    }

    @Override
    public void discard(StorageIsolationContext isolation) {

        IsolationContextHolder holder = this.getIsolationHolder(isolation);

        // This drop the claimed scopes
        this.contexts.remove(holder.name());

        try {
            // Remove recursively the isolated context. At that point even if it fails, we already dropped
            // the scopes claims, making the isolation context unusable so it does not matter anymore.
            FileUtils.delete(holder.root());
        } catch (IOException e) {
            throw new ContextCommitException(String.format("Failed to discard store '%s'.", holder.root()), e);
        }
    }

    @Override
    public void close() throws Exception {

        this.contexts.clear();
        Path store = this.resolver.resolveDirectory(STORE_ISOLATION);
        FileUtils.delete(store);
    }

    @Override
    public boolean equals(Object o) {

        if (!(o instanceof LibraryManager manager)) return false;
        return Objects.equals(this.stores, manager.stores) && Objects.equals(this.resolver, manager.resolver);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.stores, this.resolver);
    }

}
