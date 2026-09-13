package fr.anisekai.wireless.tasks.conversion;

import fr.anisekai.media.enums.Codec;
import fr.anisekai.media.enums.CodecType;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Describes an episode source and its requested conversion codecs.
 *
 * @param episode
 *         Episode source descriptor.
 * @param conversionOptions
 *         Output codec selection.
 */
public record MediaConversionInput(
        Episode episode,
        ConversionOptions conversionOptions
) {

    /**
     * Validates the conversion request.
     */
    public MediaConversionInput {

        Objects.requireNonNull(episode, "episode");
        Objects.requireNonNull(conversionOptions, "conversionOptions");
    }

    /**
     * Identifies an episode source without exposing an absolute client path.
     *
     * @param id
     *         Episode identifier.
     * @param sourceReference
     *         Normalized relative source reference.
     * @param hash
     *         Expected hexadecimal SHA-256 digest.
     */
    public record Episode(
            UUID id,
            String sourceReference,
            String hash
    ) {

        private static final Pattern SHA_256 = Pattern.compile("[0-9a-fA-F]{64}");

        /**
         * Validates and normalizes the episode source descriptor.
         */
        public Episode {

            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(sourceReference, "sourceReference");
            Objects.requireNonNull(hash, "hash");

            if (sourceReference.isBlank()) {
                throw new IllegalArgumentException("Source reference must not be blank");
            }

            try {
                Path reference = Path.of(sourceReference);
                if (reference.isAbsolute() || reference.getNameCount() == 0 || !reference.equals(reference.normalize())) {
                    throw new IllegalArgumentException("Source reference must be a normalized relative path");
                }
                for (Path element : reference) {
                    if (element.toString().equals(".") || element.toString().equals("..")) {
                        throw new IllegalArgumentException("Source reference must not contain traversal segments");
                    }
                }
                sourceReference = reference.toString();
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException("Source reference is not a valid path", e);
            }

            if (!SHA_256.matcher(hash).matches()) {
                throw new IllegalArgumentException("Source hash must be a SHA-256 hexadecimal digest");
            }
            hash = hash.toLowerCase(Locale.ROOT);
        }

    }

    /**
     * Selects one output codec for each supported media type.
     *
     * @param audioCodec
     *         Output audio codec.
     * @param videoCodec
     *         Output video codec.
     * @param subtitlesCodec
     *         Output subtitle codec.
     */
    public record ConversionOptions(
            Codec audioCodec,
            Codec videoCodec,
            Codec subtitlesCodec
    ) {

        /**
         * Validates that every selected codec matches its media type.
         */
        public ConversionOptions {

            requireCodecType(audioCodec, CodecType.AUDIO, "audioCodec");
            requireCodecType(videoCodec, CodecType.VIDEO, "videoCodec");
            requireCodecType(subtitlesCodec, CodecType.SUBTITLE, "subtitlesCodec");
        }

        private static void requireCodecType(Codec codec, CodecType type, String name) {

            Objects.requireNonNull(codec, name);
            if (codec.getType() != type) {
                throw new IllegalArgumentException("%s must be a %s codec".formatted(name, type.name().toLowerCase(Locale.ROOT)));
            }
        }

    }

}
