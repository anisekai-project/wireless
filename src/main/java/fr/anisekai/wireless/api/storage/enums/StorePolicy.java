package fr.anisekai.wireless.api.storage.enums;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.StorageStore;

/**
 * Describe how a {@link StorageStore} should be handled by the {@link LibraryManager} with {@link StorageIsolationContext}.
 */
public enum StorePolicy {

    /**
     * The {@link StorageStore} can only be used within {@link LibraryManager}. Any attempt to use such {@link StorageStore} within a
     * {@link StorageIsolationContext} will be denied.
     */
    PRIVATE(false, false),

    /**
     * The {@link StorageStore} can be used within a {@link StorageIsolationContext} and its content will be copied over the main
     * storage once disposed of, replacing existing files only.
     */
    OVERWRITE(true, true),

    /**
     * The {@link StorageStore} can be used within a {@link StorageIsolationContext} and its content will completely replace the one
     * present in the main storage, removing every unused files.
     */
    FULL_SWAP(true, true),

    /**
     * The {@link StorageStore} is unique to each instance of {@link StorageIsolationContext} and its content will be discarded once the
     * {@link StorageIsolationContext} is closed.
     */
    DISCARD(true, false);

    private final boolean allowIsolation;
    private final boolean allowEditingFile;

    StorePolicy(boolean allowIsolation, boolean allowEditingFile) {

        this.allowIsolation   = allowIsolation;
        this.allowEditingFile = allowEditingFile;
    }

    /**
     * Check if the current {@link StorePolicy} allows a {@link StorageStore} to be used within a {@link StorageIsolationContext}.
     *
     * @return True if it allows usage within a {@link StorageIsolationContext}, false otherwise.
     */
    public boolean allowIsolation() {

        return this.allowIsolation;
    }

    /**
     * Check if the current {@link StorePolicy} allows a {@link StorageIsolationContext} content to be retrieved into the main
     * {@link StorageStore}.
     *
     * @return True if it allows content within a {@link StorageIsolationContext} to be retrieved, false otherwise.
     */
    public boolean allowEditingFile() {

        return this.allowEditingFile;
    }

}
