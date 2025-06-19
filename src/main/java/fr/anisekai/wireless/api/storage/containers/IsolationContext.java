package fr.anisekai.wireless.api.storage.containers;

import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationAware;
import fr.anisekai.wireless.api.storage.interfaces.StorageIsolationContext;

/**
 * Default implementation of a {@link StorageIsolationContext}.
 *
 * @param owner
 *         The {@link StorageIsolationAware} that created this {@link StorageIsolationContext}.
 * @param name
 *         The name of this {@link StorageIsolationContext}
 */
public record IsolationContext(StorageIsolationAware owner, String name) implements StorageIsolationContext {

}
