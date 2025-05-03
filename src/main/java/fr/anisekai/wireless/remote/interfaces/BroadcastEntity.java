package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import fr.anisekai.wireless.remote.enums.BroadcastStatus;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing the base structure for a broadcast.
 *
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface BroadcastEntity<A extends AnimeEntity<?>> extends Entity<Long>, Planifiable<A> {

    /**
     * Retrieve this {@link BroadcastEntity}'s discord event id.
     *
     * @return An event id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    @Nullable Long getEventId();

    /**
     * Define this {@link BroadcastEntity}'s discord event id.
     *
     * @param eventId
     *         An event id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    void setEventId(@Nullable Long eventId);

    /**
     * Retrieve this {@link BroadcastEntity}'s {@link BroadcastStatus}.
     *
     * @return A {@link BroadcastStatus}.
     */
    @NotNull BroadcastStatus getStatus();

    /**
     * Define this {@link BroadcastEntity}'s {@link BroadcastStatus}.
     *
     * @param status
     *         A {@link BroadcastStatus}.
     */
    void setStatus(@NotNull BroadcastStatus status);

}
