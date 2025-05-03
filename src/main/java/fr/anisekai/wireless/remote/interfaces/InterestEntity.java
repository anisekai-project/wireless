package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.keys.InterestKey;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing the base structure for an interest.
 *
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 * @param <U>
 *         Type for the {@link UserEntity} implementation.
 */
public interface InterestEntity<A extends AnimeEntity<U>, U extends UserEntity> extends Entity<InterestKey> {

    /**
     * Retrieve this {@link Entity} primary key.
     *
     * @return The primary key.
     */
    @Override
    default InterestKey getId() {

        return InterestKey.create(this.getAnime(), this.getUser());
    }

    /**
     * Retrieve the {@link UserEntity} to which this {@link InterestEntity} belongs.
     *
     * @return An {@link UserEntity}
     */
    @NotNull U getUser();

    /**
     * Define the {@link UserEntity} to which this {@link InterestEntity} belongs.
     *
     * @param user
     *         An {@link UserEntity}.
     */
    void setUser(@NotNull U user);

    /**
     * Retrieve the {@link AnimeEntity} to which this {@link InterestEntity} is associated.
     *
     * @return An {@link AnimeEntity}.
     */
    @NotNull A getAnime();

    /**
     * Define the {@link AnimeEntity} to which this {@link InterestEntity} is associated.
     *
     * @param anime
     *         An {@link AnimeEntity}.
     */
    void setAnime(@NotNull A anime);

    /**
     * Retrieve this {@link InterestEntity}'s level.
     *
     * @return A level.
     */
    byte getLevel();

    /**
     * Define this {@link InterestEntity}'s level.
     *
     * @param level
     *         A level.
     */
    void setLevel(byte level);

}
