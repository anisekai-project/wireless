package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.enums.AnimeList;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Interface representing the base structure for a watchlist.
 *
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface WatchlistEntity<A extends AnimeEntity<?>> extends Entity<AnimeList> {

    /**
     * Retrieve this {@link WatchlistEntity}'s discord message id.
     *
     * @return A message id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    @Nullable Long getMessageId();

    /**
     * Define this {@link WatchlistEntity}'s discord message id.
     *
     * @param messageId
     *         A message id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    void setMessageId(@Nullable Long messageId);

    /**
     * Retrieve this {@link WatchlistEntity}'s {@link AnimeEntity} content.
     *
     * @return A {@link Set} of {@link AnimeEntity}.
     */
    @NotNull Set<A> getAnimes();

    /**
     * Define this {@link WatchlistEntity}'s {@link AnimeEntity} content.
     *
     * @param animes
     *         A {@link Set} of {@link AnimeEntity}.
     */
    void setAnimes(@NotNull Set<A> animes);

}
