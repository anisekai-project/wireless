package fr.anisekai.wireless.remote.keys;

import fr.anisekai.wireless.remote.interfaces.AnimeEntity;
import fr.anisekai.wireless.remote.interfaces.UserEntity;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Represents a composite key linking a specific anime to a specific user.
 * <p>
 * Used to uniquely identify a user's interest in a particular anime.
 *
 * @param anime
 *         The {@link AnimeEntity} ID
 * @param user
 *         The {@link UserEntity} ID
 */
public record InterestKey(long anime, long user) implements Serializable {

    /**
     * Creates a new {@link InterestKey} from the given {@link AnimeEntity} and {@link UserEntity}.
     * <p>
     * Both entities must have non-null IDs.
     *
     * @param anime
     *         The {@link AnimeEntity}
     * @param user
     *         The {@link UserEntity}
     *
     * @return A new {@link InterestKey} representing the link between an {@link AnimeEntity} and an {@link UserEntity}.
     *
     * @throws AssertionError
     *         Threw if either {@link AnimeEntity#getId()} or {@link UserEntity#getId()} is {@code null}
     */
    public static @NotNull InterestKey create(@NotNull AnimeEntity<?> anime, @NotNull UserEntity user) {

        assert anime.getId() != null;
        assert user.getId() != null;
        return new InterestKey(anime.getId(), user.getId());
    }

}
