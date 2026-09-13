package fr.anisekai.wireless.tasks.conversion;

import fr.anisekai.media.enums.Codec;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Reports the tracks produced by a media conversion.
 *
 * @param tracks
 *         Converted track descriptors.
 */
public record MediaConversionOutput(
        List<Track> tracks
) {

    /**
     * Copies the converted track collection into an immutable list.
     */
    public MediaConversionOutput {

        tracks = List.copyOf(Objects.requireNonNull(tracks, "tracks"));
    }

    /**
     * Describes a converted output track.
     *
     * @param uuid
     *         New track identifier.
     * @param name
     *         Display name.
     * @param codec
     *         Output codec.
     * @param language
     *         Optional source language.
     * @param dispositions
     *         Encoded FFmpeg disposition flags.
     */
    public record Track(UUID uuid, String name, Codec codec, String language, int dispositions) {

        /**
         * Validates the required track metadata.
         */
        public Track {

            Objects.requireNonNull(uuid, "uuid");
            Objects.requireNonNull(name, "name");
            Objects.requireNonNull(codec, "codec");
        }

    }

}
