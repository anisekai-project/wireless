package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing the base structure for an episode.
 *
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface EpisodeEntity<A extends AnimeEntity<?>> extends Entity<Long> {

    /**
     * Retrieve the {@link AnimeEntity} to which this {@link EpisodeEntity} belongs.
     *
     * @return An {@link AnimeEntity}.
     */
    @NotNull A getAnime();

    /**
     * Define the {@link AnimeEntity} to which this {@link EpisodeEntity} belongs.
     *
     * @param anime
     *         The {@link AnimeEntity}
     */
    void setAnime(@NotNull A anime);

    /**
     * Retrieve this {@link EpisodeEntity}'s number within the {@link AnimeEntity}.
     *
     * @return A number
     */
    int getNumber();

    /**
     * Define this {@link EpisodeEntity}'s number within the {@link AnimeEntity}.
     *
     * @param number
     *         A number
     */
    void setNumber(int number);

}
