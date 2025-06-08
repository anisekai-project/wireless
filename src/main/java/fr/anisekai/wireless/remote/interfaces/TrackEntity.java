package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing the base structure for a track.
 *
 * @param <E>
 *         Type for the {@link EpisodeEntity} implementation.
 */
public interface TrackEntity<E extends EpisodeEntity<?>> extends Entity<Long> {

    /**
     * Retrieve the {@link EpisodeEntity} to which this {@link TrackEntity} belongs.
     *
     * @return An {@link EpisodeEntity}.
     */
    @NotNull E getEpisode();

    /**
     * Define the {@link EpisodeEntity} to which this {@link TrackEntity} belongs.
     *
     * @param episode
     *         An {@link EpisodeEntity}.
     */
    void setEpisode(@NotNull E episode);

    /**
     * Retrieve this {@link TrackEntity}'s name. This will be the name used on the player UI, if applicable.
     *
     * @return A name.
     */
    @NotNull String getName();

    /**
     * Define this {@link TrackEntity}'s name. This will be the name used on the player UI, if applicable.
     *
     * @param name
     *         A name.
     */
    void setName(@NotNull String name);

    /**
     * Retrieve this {@link TrackEntity}'s {@link Codec}.
     *
     * @return A {@link Codec}.
     */
    @NotNull Codec getCodec();

    /**
     * Define this {@link TrackEntity}'s {@link Codec}.
     *
     * @param codec
     *         A {@link Codec}/
     */
    void setCodec(@NotNull Codec codec);

    /**
     * Retrieve this {@link TrackEntity}'s language. This is only applicable to {@link CodecType#AUDIO} and
     * {@link CodecType#SUBTITLE} and is using an ISO 639-2 compliant value.
     *
     * @return An ISO 639-2 value.
     *
     * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes">ISO 639-2 Reference</a>
     */
    @Nullable String getLanguage();

    /**
     * Retrieve this {@link TrackEntity}'s language. This is only applicable to {@link CodecType#AUDIO} and
     * {@link CodecType#SUBTITLE} and is using an ISO 639-2 compliant value.
     *
     * @param language
     *         An ISO 639-2 value.
     *
     * @see <a href="https://en.wikipedia.org/wiki/List_of_ISO_639-2_codes">ISO 639-2 Reference</a>
     */
    void setLanguage(@Nullable String language);

    /**
     * Check if this {@link TrackEntity} is a forced track. Only applicable to {@link CodecType#SUBTITLE}.
     *
     * @return True if the {@link TrackEntity} is forced, false otherwise.
     */
    boolean isForced();

    /**
     * Define if this {@link TrackEntity} is a forced track. Only applicable to {@link CodecType#SUBTITLE}.
     *
     * @param forced
     *         True if the {@link TrackEntity} is forced, false otherwise.
     */
    void setForced(boolean forced);

}
