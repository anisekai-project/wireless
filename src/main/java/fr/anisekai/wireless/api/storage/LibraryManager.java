package fr.anisekai.wireless.api.storage;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.storage.containers.AccessScope;
import fr.anisekai.wireless.api.storage.containers.IsolationContext;
import fr.anisekai.wireless.api.storage.containers.IsolationContextHolder;
import fr.anisekai.wireless.api.storage.containers.stores.RawFileStore;
import fr.anisekai.wireless.api.storage.enums.StorePolicy;
import fr.anisekai.wireless.api.storage.enums.StoreType;
import fr.anisekai.wireless.api.storage.exceptions.*;
import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;
import fr.anisekai.wireless.api.storage.interfaces.ScopedEntity;
import fr.anisekai.wireless.utils.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class allowing the structured management of a directory using {@link FileStore}.
 */
public class LibraryManager {

    /**
     * {@link FileStore} representing a temporary directory. This is a special store that is implicit granted on every
     * {@link FileIsolationContext} without the need for an {@link AccessScope}. Trying to claim this {@link FileStore} using an
     * {@link AccessScope} will result in a {@link ScopeDefinitionException}.
     */
    public static final FileStore STORE_TMP = new RawFileStore("tmp");

    private static final FileStore STORE_ISOLATION = new RawFileStore("isolation");

    private final Map<FileStore, StorePolicy>         stores;
    private final Map<String, IsolationContextHolder> contexts;
    private final File                                root;

    /**
     * Create a random name that can be used either as temporary file name or as a name for a {@link FileIsolationContext}.
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
    public LibraryManager(File root) {

        this.root     = root;
        this.contexts = new HashMap<>();
        this.stores   = new HashMap<>();

        FileUtils.ensureDirectoryExists(root, ex -> new IllegalStateException("Failure while initializing library.", ex));

        // Internal store
        this.register(STORE_TMP, StorePolicy.DISCARD);
        this.register(STORE_ISOLATION, StorePolicy.PRIVATE);
    }

    /**
     * Retrieve the requested {@link FileStore} root directory within the provided {@link File}.
     *
     * @param relativeTo
     *         Base directory into which the {@link FileStore} should be retrieved.
     * @param store
     *         The {@link FileStore} to retrieve.
     *
     * @return An existing directory pointing to the root of the {@link FileStore}.
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If the provided base {@link File} does not point to a directory.
     */
    private static File findTarget(File relativeTo, FileStore store) {

        File fStore = new File(relativeTo, store.name());

        if (!FileUtils.isDirectChild(relativeTo, fStore)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound for '%s' store.",
                    store.name()
            ));
        }

        FileUtils.ensureDirectoryExists(
                fStore,
                ex -> new StoreAccessException(String.format("Failure while accessing '%s' store.", store.name()), ex)
        );

        return fStore;
    }

    /**
     * Retrieve the requested target of the {@link FileStore} within the provided {@link File}.
     *
     * @param relativeTo
     *         Base directory into which the {@link FileStore} should be retrieved.
     * @param store
     *         The {@link ScopedEntity} to retrieve.
     * @param entity
     *         The {@link Entity} associated to the target to retrieve.
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
    private static File findTarget(File relativeTo, FileStore store, ScopedEntity entity) {

        if (!store.type().isEntityScoped()) {
            throw new StoreAccessException(String.format(
                    "Tried to use '%s' as an entity store.",
                    store.name()
            ));
        }

        File fStore = findTarget(relativeTo, store);
        File target;

        if (store.type() == StoreType.ENTITY_FILE) {
            target = new File(fStore, String.format("%s.%s", entity.getScopedName(), store.extension()));
        } else {
            target = new File(fStore, entity.getScopedName());
        }

        if (!FileUtils.isDirectChild(fStore, target)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound for entity '%s' within the '%s' store.",
                    entity.getScopedName(),
                    store.name()
            ));
        }

        if (store.type() == StoreType.ENTITY_FILE) return target;

        FileUtils.ensureDirectoryExists(
                target,
                ex -> new StoreAccessException(String.format("Failure while accessing '%s' store.", store.name()), ex)
        );

        return target;
    }

    /**
     * Retrieve the requested target of the {@link FileStore} within the provided {@link File}.
     *
     * @param relativeTo
     *         Base directory into which the {@link FileStore} should be retrieved.
     * @param store
     *         The {@link FileStore} to retrieve.
     * @param name
     *         Name of the {@link File} within the {@link FileStore} to retrieve.
     *
     * @return A {@link File} pointing to either a file or a directory, depending on the custom management defined
     *         ({@link FileStore#type()} is {@link StoreType#RAW})
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    private static File findTarget(File relativeTo, FileStore store, String name) {

        if (store.type().isEntityScoped()) {
            throw new StoreAccessException(String.format(
                    "Tried to use '%s' as a raw store.",
                    store.name()
            ));
        }

        File fStore = findTarget(relativeTo, store);
        File target = new File(fStore, name);

        if (!FileUtils.isDirectChild(fStore, target)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound for file '%s' in '%s' store.",
                    name,
                    store.name()
            ));
        }

        return target;
    }

    /**
     * Retrieve the requested target of the {@link FileStore} within the provided {@link File}.
     *
     * @param relativeTo
     *         Base directory into which the {@link FileStore} should be retrieved.
     * @param store
     *         The {@link FileStore} to retrieve.
     * @param entity
     *         The {@link ScopedEntity} associated to the target to retrieve.
     * @param name
     *         Name of the {@link File} within the {@link FileStore} to retrieve.
     *
     * @return A {@link File} pointing to either a file or a directory, depending on the custom management defined
     *         ({@link FileStore#type()} is {@link StoreType#RAW})
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    private static File findTarget(File relativeTo, FileStore store, ScopedEntity entity, String name) {

        if (!store.type().isEntityScoped()) {
            throw new StoreAccessException(String.format(
                    "Tried to use '%s' as an entity store.",
                    store.name()
            ));
        }

        File fStore = findTarget(relativeTo, store, entity);
        File target = new File(fStore, name);

        if (!FileUtils.isDirectChild(fStore, target)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound for file '%s' in '%s' store.",
                    name,
                    store.name()
            ));
        }

        return target;
    }

    /**
     * Check the requested {@link AccessScope} against a known collection of already-claimed {@link AccessScope}. This method
     * either returns normally (the scope is valid and free to claim) or throws a {@link ScopeGrantException}.
     *
     * @param claimedScopes
     *         Collection of already-claimed {@link AccessScope}.
     * @param requestedScope
     *         The {@link AccessScope} that need to be checked.
     *
     * @throws ScopeGrantException
     *         If the requested {@link AccessScope} cannot be granted or is not usable with this {@link LibraryManager}.
     */
    private void checkScope(Collection<AccessScope> claimedScopes, AccessScope requestedScope) {

        if (claimedScopes.contains(requestedScope)) {
            throw new ScopeGrantException(String.format(
                    "Scope '%s' is already claimed by another isolation context.",
                    requestedScope
            ));
        }

        this.checkStoreAvailability(requestedScope.getFileStore());
    }

    /**
     * Check if the provided {@link FileStore} can be used with this {@link LibraryManager}. This method either returns normally
     * (the store is valid) or throws a {@link StoreAccessException}.
     * <p>
     * <b>Note:</b> The check considers that the {@link FileStore} use-case will be external of this {@link LibraryManager}, so
     * if the {@link FileStore} is registered with the {@link StorePolicy#PRIVATE} policy, this method will fail.
     *
     * @param store
     *         The {@link FileStore} to check.
     *
     * @throws StoreAccessException
     *         If the {@link FileStore} cannot be used with this {@link LibraryManager}.
     */
    private void checkStoreAvailability(FileStore store) {

        if (!this.stores.containsKey(store)) {
            throw new StoreAccessException(String.format(
                    "This library manager does not support the '%s' store.",
                    store.name()
            ));
        }

        if (this.stores.get(store) == StorePolicy.PRIVATE) {
            throw new StoreAccessException(String.format(
                    "The '%s' store is not accessible outside the global library.",
                    store.name()
            ));
        }
    }

    /**
     * Check if the provided {@link IsolationContextHolder} can access and use the {@link FileStore}. This method either returns
     * normally (the store is usable) or throws an error.
     *
     * @param context
     *         The {@link IsolationContextHolder} for which the {@link FileStore} access must be checked.
     * @param store
     *         The {@link FileStore} to check.
     *
     * @throws StoreAccessException
     *         If the {@link FileStore} cannot be used with this {@link LibraryManager}.
     * @throws StoreScopeException
     *         If the {@link IsolationContextHolder} does not have proper {@link AccessScope} claims to use the
     *         {@link FileStore}.
     */
    private void checkIsolation(IsolationContextHolder context, FileStore store) {

        this.checkStoreAvailability(store);

        if (context.isCommitted()) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already committed and cannot be used anymore.",
                    context.getName()
            ));
        }

        if (this.stores.get(store) == StorePolicy.DISCARD) {
            return; // Implicit grant on discard raw stores
        }

        throw new StoreScopeException(String.format(
                "This isolation context cannot access the '%s' store without a scope",
                store.name()
        ));
    }

    /**
     * Check if the provided {@link IsolationContextHolder} can access and use the {@link FileStore} by checking the presence of
     * an {@link AccessScope} claim. This method either returns normally (the store is usable) or throws an error.
     *
     * @param context
     *         The {@link IsolationContextHolder} for which the {@link FileStore} access must be checked.
     * @param store
     *         The {@link FileStore} to check.
     *
     * @throws StoreAccessException
     *         If the {@link FileStore} cannot be used with this {@link LibraryManager}.
     * @throws StoreScopeException
     *         If the {@link AccessScope} is not claimed by the {@link IsolationContextHolder}.
     */
    private void checkIsolation(IsolationContextHolder context, FileStore store, AccessScope scope) {

        this.checkStoreAvailability(store);

        if (context.isCommitted()) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already committed and cannot be used anymore.",
                    context.getName()
            ));
        }

        if (!context.getScopes().contains(scope)) {
            throw new StoreScopeException(String.format(
                    "This isolation context does not have the '%s' scope",
                    scope
            ));
        }
    }

    /**
     * Retrieve the {@link IsolationContextHolder} from the provided {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} from which the {@link IsolationContextHolder} must be retrieved.
     *
     * @return An {@link IsolationContextHolder}.
     *
     * @throws StoreAccessException
     *         If the {@link FileIsolationContext} cannot be used with this {@link LibraryManager}.
     */
    private IsolationContextHolder getContextHolder(FileIsolationContext isolation) {

        if (!this.contexts.containsKey(isolation.name())) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already discarded or was not created using this library manager.",
                    isolation.name()
            ));
        }

        IsolationContextHolder ctx = this.contexts.get(isolation.name());

        if (!ctx.getName().equals(isolation.name())) {
            throw new StoreAccessException(String.format(
                    "Isolation context name mismatch: should be '%s' but found '%s'.",
                    ctx.getName(),
                    isolation.name()
            ));
        }

        return ctx;
    }

    /**
     * Retrieve the {@link File} pointing to the {@link FileStore} directory.
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
    public File getStoreFile(FileStore store) {

        if (!this.stores.containsKey(store)) {
            throw new StoreAccessException(String.format(
                    "This library manager do not support the '%s' store.",
                    store.name()
            ));
        }

        return findTarget(this.root, store);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory.
     *
     * @param store
     *         The {@link FileStore} to retrieve.
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
    public File getStoreFile(FileStore store, String name) {

        if (!this.stores.containsKey(store)) {
            throw new StoreAccessException(String.format(
                    "This library manager do not support the '%s' store.",
                    store.name()
            ));
        }

        return findTarget(this.root, store, name);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory.
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
    public File getStoreFile(FileStore store, ScopedEntity entity) {

        if (!this.stores.containsKey(store)) {
            throw new StoreAccessException(String.format(
                    "This library manager do not support the '%s' store.",
                    store.name()
            ));
        }

        return findTarget(this.root, store, entity);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory.
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
    public File getStoreFile(FileStore store, ScopedEntity entity, String name) {

        if (!this.stores.containsKey(store)) {
            throw new StoreAccessException(String.format(
                    "This library manager do not support the '%s' store.",
                    store.name()
            ));
        }

        File fStore = findTarget(this.root, store, entity);
        File target = new File(fStore, name);

        if (!FileUtils.isDirectChild(fStore, target)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound with filename '%s' for entity '%s' within the '%s' store.",
                    name,
                    entity.getScopedName(),
                    store.name()
            ));
        }

        return target;
    }

    /**
     * Retrieve the {@link File} pointing to the {@link FileStore} directory within the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} from which the {@link File} will be retrieved.
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
    public File getStoreFile(FileIsolationContext isolation, FileStore store) {

        IsolationContextHolder context = this.getContextHolder(isolation);
        this.checkIsolation(context, store);
        return findTarget(context.getRoot(), store);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory within the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} from which the {@link File} will be retrieved.
     * @param store
     *         The {@link FileStore} to retrieve.
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
    public File getStoreFile(FileIsolationContext isolation, FileStore store, String name) {

        IsolationContextHolder context = this.getContextHolder(isolation);
        this.checkIsolation(context, store);
        return findTarget(context.getRoot(), store, name);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory within the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} from which the {@link File} will be retrieved.
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
    public File getStoreFile(FileIsolationContext isolation, FileStore store, ScopedEntity entity) {

        IsolationContextHolder context = this.getContextHolder(isolation);
        AccessScope            scope   = new AccessScope(store, entity);
        this.checkIsolation(context, store, scope);
        return findTarget(context.getRoot(), store, entity);
    }

    /**
     * Retrieve the {@link File} pointing to a file in the {@link FileStore} directory within the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} from which the {@link File} will be retrieved.
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
    public File getStoreFile(FileIsolationContext isolation, FileStore store, ScopedEntity entity, String name) {

        IsolationContextHolder context = this.getContextHolder(isolation);
        AccessScope            scope   = new AccessScope(store, entity);
        this.checkIsolation(context, store, scope);
        return findTarget(context.getRoot(), store, entity, name);
    }

    /**
     * Register the provided {@link FileStore} and apply a specific {@link StorePolicy} to it.
     *
     * @param store
     *         The {@link FileStore} to add to this {@link LibraryManager}
     * @param policy
     *         The {@link StorePolicy} to use when {@link FileIsolationContext} uses the {@link FileStore}.
     *
     * @throws StoreBreakoutException
     *         If the {@link File} retrieved goes outside the allowed boundaries.
     * @throws StoreRegistrationException
     *         If the {@link FileStore} directory could not be created.
     * @throws StoreAccessException
     *         If an invalid action is attempted on the {@link FileStore}
     */
    public void register(FileStore store, StorePolicy policy) {

        if (!store.type().isEntityScoped() && policy.allowEditingFile()) {
            throw new StoreRegistrationException(String.format(
                    "The '%s' scoped store cannot be registered under the '%s' policy.",
                    store.name(),
                    policy.name()
            ));
        }

        if (this.stores.keySet().stream().anyMatch(registeredStore -> registeredStore.name().equals(store.name()))) {
            throw new StoreRegistrationException(String.format(
                    "A store with the name '%s' is already registered.",
                    store.name()
            ));
        }

        File fStore = new File(this.root, store.name());

        if (!FileUtils.isDirectChild(this.root, fStore)) {
            throw new StoreBreakoutException(String.format(
                    "Destination out-of-bound for '%s' store.",
                    store.name()
            ));
        }

        FileUtils.ensureDirectoryExists(
                fStore,
                ex -> new StoreRegistrationException(String.format("Failure while registering '%s' store.", store.name()), ex)
        );

        findTarget(this.root, store);
        this.stores.put(store, policy);
    }

    /**
     * Try to store the provided {@link InputStream} in the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} into which the {@link InputStream} will be written
     * @param store
     *         The {@link FileStore} into which the {@link InputStream} will be written
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
    public File store(FileIsolationContext isolation, FileStore store, String name, InputStream is) throws IOException {

        if (store.type() != StoreType.RAW) {
            throw new StoreAccessException(String.format(
                    "Isolation context tried to use '%s' store as a raw store.",
                    store.name()
            ));
        }

        File target = this.getStoreFile(isolation, store, name);
        try (OutputStream os = new FileOutputStream(target)) {
            is.transferTo(os);
        }
        return target;
    }

    /**
     * Try to store the provided {@link InputStream} in the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} into which the {@link InputStream} will be written
     * @param store
     *         The {@link FileStore} into which the {@link InputStream} will be written
     * @param entity
     *         The {@link ScopedEntity} associated to the {@link File} into which the {@link InputStream} content will be written
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
    public File store(FileIsolationContext isolation, FileStore store, ScopedEntity entity, InputStream is) throws IOException {

        if (store.type() != StoreType.ENTITY_FILE) {
            throw new StoreAccessException(String.format(
                    "Isolation context tried to use '%s' store as an entity file store.",
                    store.name()
            ));
        }

        File target = this.getStoreFile(isolation, store, entity);
        try (OutputStream os = new FileOutputStream(target)) {
            is.transferTo(os);
        }
        return target;
    }

    /**
     * Try to store the provided {@link InputStream} in the {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} into which the {@link InputStream} will be written
     * @param store
     *         The {@link FileStore} into which the {@link InputStream} will be written
     * @param entity
     *         The {@link ScopedEntity} associated to the directory into which the {@link InputStream} content will be written
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
    public File store(FileIsolationContext isolation, FileStore store, ScopedEntity entity, String name, InputStream is) throws IOException {

        if (store.type() != StoreType.ENTITY_DIRECTORY) {
            throw new StoreAccessException(String.format(
                    "Isolation context tried to use '%s' store as an entity directory store.",
                    store.name()
            ));
        }

        File target = this.getStoreFile(isolation, store, entity, name);
        try (OutputStream os = new FileOutputStream(target)) {
            is.transferTo(os);
        }
        return target;
    }

    /**
     * Request a temporary {@link File} in the specified {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} to which the {@link File} will point
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
    public File requestTemporaryFile(FileIsolationContext isolation, String extension) {

        String name = String.format("%s.%s", getRandomName(), extension);
        return this.getStoreFile(isolation, STORE_TMP, name);
    }

    /**
     * Try to create a new {@link FileIsolationContext} without any {@link AccessScope}.
     *
     * @return A new {@link FileIsolationContext}.
     *
     * @throws StoreAccessException
     *         If the {@link FileIsolationContext} directory could not be created.
     */
    public FileIsolationContext createIsolation() {

        return this.createIsolation(Collections.emptySet());
    }

    /**
     * Try to create a new {@link FileIsolationContext} with a single {@link AccessScope}.
     *
     * @param scope
     *         A {@link AccessScope} that the new {@link FileIsolationContext} will claim.
     *
     * @return A new {@link FileIsolationContext}.
     *
     * @throws ScopeGrantException
     *         If one of the requested {@link AccessScope} could not be granted.
     * @throws StoreAccessException
     *         If the {@link FileIsolationContext} directory could not be created.
     */
    public FileIsolationContext createIsolation(AccessScope scope) {

        return this.createIsolation(Set.of(scope));
    }

    /**
     * Try to create a new {@link FileIsolationContext} with the provided set of {@link AccessScope}.
     *
     * @param scopes
     *         A set of {@link AccessScope} that the new {@link FileIsolationContext} will claim.
     *
     * @return A new {@link FileIsolationContext}.
     *
     * @throws ScopeGrantException
     *         If one of the requested {@link AccessScope} could not be granted.
     * @throws StoreAccessException
     *         If the {@link FileIsolationContext} directory could not be created.
     */
    public FileIsolationContext createIsolation(Set<AccessScope> scopes) {

        Set<AccessScope> claimedScopes = this.contexts
                .values()
                .stream()
                .map(IsolationContextHolder::getScopes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        for (AccessScope requestedScope : scopes) {
            this.checkScope(claimedScopes, requestedScope);
        }

        String name          = LibraryManager.getRandomName();
        File   isolationRoot = this.getStoreFile(STORE_ISOLATION, name);

        FileUtils.ensureDirectoryExists(
                isolationRoot,
                ex -> new StoreAccessException(String.format("Could not create '%s' isolation context.", name), ex)
        );

        FileIsolationContext   isolation = new IsolationContext(this, name);
        IsolationContextHolder context   = new IsolationContextHolder(name, isolationRoot, new HashSet<>(scopes), isolation);

        this.contexts.put(name, context);
        return isolation;
    }

    /**
     * Try to claim the requested {@link AccessScope} for the provided {@link FileIsolationContext}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} for which the {@link AccessScope} should be granted.
     * @param requestedScope
     *         The {@link AccessScope} to grant.
     *
     * @throws ScopeGrantException
     *         If the requested {@link AccessScope} could not be granted.
     */
    public void requestScope(FileIsolationContext isolation, AccessScope requestedScope) {

        Set<AccessScope> claimedScopes = this.contexts
                .values()
                .stream()
                .map(IsolationContextHolder::getScopes)
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        this.checkScope(claimedScopes, requestedScope);
        IsolationContextHolder context = this.getContextHolder(isolation);
        context.getScopes().add(requestedScope);
    }

    /**
     * Commits the contents of the given {@link FileIsolationContext} to the library, applying the corresponding {@link FileStore}
     * policies defined in this {@link LibraryManager}. All {@link AccessScope} claims associated with the isolation context are
     * released upon commit.
     * <p>
     * After a context has been committed, and any further use of the same {@link FileIsolationContext} is considered invalid.
     * <p>
     * Partial failure during commit may result in some {@link AccessScope} being updated and others not, as each
     * {@link AccessScope} is committed independently.
     *
     * @param isolation
     *         The {@link FileIsolationContext} to commit.
     */
    public void commit(FileIsolationContext isolation) {

        IsolationContextHolder context = this.getContextHolder(isolation);

        if (context.isCommitted()) {
            throw new ContextUnavailableException(String.format(
                    "The isolation context '%s' is already committed and cannot be used anymore.",
                    context.getName()
            ));
        }

        for (AccessScope scope : context.getScopes()) {
            try {
                this.commitScope(isolation, scope);
            } catch (IOException e) {
                throw new ContextCommitException(String.format("Failed to commit scope '%s'.", scope), e);
            }
        }

        context.setCommitted(true);
    }

    /**
     * Commits the contents of the given {@link FileIsolationContext} under the {@link AccessScope} to the library, applying the
     * corresponding {@link FileStore} policies defined in this {@link LibraryManager}.
     *
     * @param isolation
     *         The {@link FileIsolationContext} to commit.
     * @param scope
     *         The {@link AccessScope} to commit.
     */
    private void commitScope(FileIsolationContext isolation, AccessScope scope) throws IOException {

        FileStore    store  = scope.getFileStore();
        ScopedEntity claim  = scope.getClaim();
        StorePolicy  policy = this.stores.get(store);

        if (policy == StorePolicy.DISCARD) return;
        if (!store.type().isEntityScoped()) return;

        File fsContext        = this.getStoreFile(store);
        File localContent     = this.getStoreFile(store, claim);
        File isolatedContent  = this.getStoreFile(isolation, store, claim);
        File safeLocalContent = new File(fsContext, "." + localContent.getName());

        boolean backupMade = false;
        try {
            if (localContent.exists()) {
                FileUtils.copyRecursively(localContent.toPath(), safeLocalContent.toPath());
                backupMade = true;
            }

            if (store.type() == StoreType.ENTITY_DIRECTORY) {

                if (policy == StorePolicy.OVERWRITE) {
                    FileUtils.copyRecursively(
                            isolatedContent.toPath(),
                            localContent.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    FileUtils.deleteRecursively(safeLocalContent);
                    return;
                }

                if (policy == StorePolicy.FULL_SWAP) {
                    FileUtils.deleteRecursively(localContent);
                    FileUtils.copyRecursively(
                            isolatedContent.toPath(),
                            localContent.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );
                    FileUtils.deleteRecursively(safeLocalContent);
                    return;
                }

                throw new IllegalStateException("Encountered an unhandled directory policy " + policy.name());
            }

            if (store.type() == StoreType.ENTITY_FILE) {

                if (policy == StorePolicy.OVERWRITE) {
                    if (isolatedContent.exists()) {
                        Files.copy(isolatedContent.toPath(), localContent.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    FileUtils.deleteRecursively(safeLocalContent);
                    return;
                }

                if (policy == StorePolicy.FULL_SWAP) {
                    FileUtils.deleteRecursively(localContent);
                    if (isolatedContent.exists()) {
                        Files.copy(isolatedContent.toPath(), localContent.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                    FileUtils.deleteRecursively(safeLocalContent);
                    return;
                }

                throw new IllegalStateException("Encountered an unhandled file policy " + policy.name());
            }
        } catch (IOException e) {
            // Try to restore backup
            if (backupMade) {
                FileUtils.deleteRecursively(localContent);
                //noinspection ResultOfMethodCallIgnored
                safeLocalContent.renameTo(localContent);
            }
            throw e;
        }
    }

    /**
     * Discard the provided {@link FileIsolationContext}, releasing all {@link AccessScope} claimed. The
     * {@link FileIsolationContext} will be deleted on a best-effort basis, but has no impact on the overall library.
     *
     * @param isolation
     *         The {@link IsolationContext} to drop.
     */
    public void discard(FileIsolationContext isolation) {

        IsolationContextHolder context = this.getContextHolder(isolation);

        // This drop the claimed scopes
        this.contexts.remove(context.getName());

        try {
            // Remove recursively the isolated context. At that point even if it fails, we already dropped
            // the scopes claims, making the isolation context unusable so it does not matter anymore.
            FileUtils.deleteRecursively(context.getRoot());
        } catch (IOException e) {
            throw new ContextCommitException(String.format("Failed to discard store '%s'.", context.getRoot()), e);
        }
    }

}
