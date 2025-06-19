package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.LibraryManager;
import fr.anisekai.wireless.api.storage.interfaces.FileIsolationContext;

/**
 * Default implementation of a {@link FileIsolationContext}.
 *
 * @param manager
 *         The {@link LibraryManager} that created this {@link FileIsolationContext}.
 * @param name
 *         The name of this {@link FileIsolationContext}
 */
public record IsolationContext(LibraryManager manager, String name) implements FileIsolationContext {

}
