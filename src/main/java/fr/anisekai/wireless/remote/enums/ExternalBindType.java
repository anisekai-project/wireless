package fr.anisekai.wireless.remote.enums;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;

/**
 * Used in conjonction of the {@link ExternallyBoundBy} annotation to provide a better insight of when the value associated to it
 * might change.
 */
public enum ExternalBindType {
    /**
     * The value will be kept synchronized when a web authentication occurs.
     */
    AUTH,
    /**
     * The value will be kept synchronized when an application interaction occurs.
     */
    INTERACTION,
    /**
     * The value is bound by a discord entity (in most cases, a message)
     */
    DISCORD,
    /**
     * The value is bound by the transmission daemon for torrents.
     */
    TRANSMISSION
}
