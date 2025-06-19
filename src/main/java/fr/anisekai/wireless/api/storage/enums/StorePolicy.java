package fr.anisekai.wireless.api.storage.enums;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;
import fr.anisekai.wireless.api.storage.interfaces.FileStore;

/**
 * Describe how a {@link FileStore} should be handled by the {@link LibraryManager} with {@link FileIsolationContext}.
 */
public enum StorePolicy {

    /**
     * The {@link FileStore} can only be used within {@link LibraryManager}. Any attempt to use such {@link FileStore} within a
     * {@link FileIsolationContext} will be denied.
     */
    PRIVATE(false, false),

    /**
     * The {@link FileStore} can be used within a {@link FileIsolationContext} and its content will be copied over the main
     * storage once disposed of, replacing existing files only.
     */
    OVERWRITE(true, true),

    /**
     * The {@link FileStore} can be used within a {@link FileIsolationContext} and its content will completely replace the one
     * present in the main storage, removing every unused files.
     */
    FULL_SWAP(true, true),

    /**
     * The {@link FileStore} is unique to each instance of {@link FileIsolationContext} and its content will be discarded once the
     * {@link FileIsolationContext} is closed.
     */
    DISCARD(true, false);

    private final boolean allowIsolation;
    private final boolean allowEditingFile;

    StorePolicy(boolean allowIsolation, boolean allowEditingFile) {

        this.allowIsolation   = allowIsolation;
        this.allowEditingFile = allowEditingFile;
    }

    /**
     * Check if the current {@link StorePolicy} allows a {@link FileStore} to be used within a {@link FileIsolationContext}.
     *
     * @return True if it allows usage within a {@link FileIsolationContext}, false otherwise.
     */
    public boolean allowIsolation() {

        return this.allowIsolation;
    }

    /**
     * Check if the current {@link StorePolicy} allows a {@link FileIsolationContext} content to be retrieved into the main
     * {@link FileStore}.
     *
     * @return True if it allows content within a {@link FileIsolationContext} to be retrieved, false otherwise.
     */
    public boolean allowEditingFile() {

        return this.allowEditingFile;
    }

}
