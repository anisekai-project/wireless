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
     * @param source
     *         Store-qualified source descriptor. Workers build the download URL from it.
     * @param hash
     *         Expected hexadecimal SHA-256 digest of the source content.
     */
    public record Episode(
            UUID id,
            Source source,
            String hash
    ) {

        private static final Pattern SHA_256 = Pattern.compile("[0-9a-fA-F]{64}");

        /**
         * Validates and normalizes the episode source descriptor.
         */
        public Episode {

            Objects.requireNonNull(id, "id");
            Objects.requireNonNull(source, "source");
            Objects.requireNonNull(hash, "hash");

            if (!SHA_256.matcher(hash).matches()) {
                throw new IllegalArgumentException("Source hash must be a SHA-256 hexadecimal digest");
            }
            hash = hash.toLowerCase(Locale.ROOT);
        }

    }

    /**
     * Origin store of a conversion source.
     */
    public enum Store {

        /**
         * Manually imported file under the {@code imports} store. The reference is a normalized relative path
         * (e.g. {@code batch/episode.mkv}).
         */
        IMPORTS,

        /**
         * Torrent-downloaded file under the {@code downloads} store. The reference is
         * {@code {torrentId}/{fileIndex}} (e.g. {@code 123e4567-e89b-12d3-a456-426614174000/3}).
         */
        DOWNLOADS
    }

    /**
     * Store-qualified reference to a conversion source.
     * <p>
     * Workers resolve the download URL from this descriptor:
     * <ul>
     *     <li>{@link Store#IMPORTS} → {@code GET /api/v3/library/imports/{reference}}
     *     (one or two path segments).</li>
     *     <li>{@link Store#DOWNLOADS} → {@code GET /api/v3/library/downloads/{reference}}
     *     ({@code {torrentId}/{fileIndex}}).</li>
     * </ul>
     *
     * @param store
     *         Origin store of the source.
     * @param reference
     *         Store-relative source reference.
     */
    public record Source(
            Store store,
            String reference
    ) {

        /**
         * Validates and normalizes the source descriptor.
         */
        public Source {

            Objects.requireNonNull(store, "store");
            Objects.requireNonNull(reference, "reference");

            if (reference.isBlank()) {
                throw new IllegalArgumentException("Source reference must not be blank");
            }

            switch (store) {
                case IMPORTS -> reference = validatedImportReference(reference);
                case DOWNLOADS -> reference = validatedDownloadReference(reference);
            }
        }

        private static String validatedImportReference(String reference) {

            try {
                Path path = Path.of(reference);
                if (path.isAbsolute() || path.getNameCount() == 0 || !path.equals(path.normalize())
                        || path.getNameCount() > 2) {
                    throw new IllegalArgumentException(
                            "Import reference must be a normalized relative file or directory/file path");
                }
                for (Path element : path) {
                    if (element.toString().equals(".") || element.toString().equals("..")) {
                        throw new IllegalArgumentException("Import reference must not contain traversal segments");
                    }
                }
                return path.toString();
            } catch (InvalidPathException e) {
                throw new IllegalArgumentException("Import reference is not a valid path", e);
            }
        }

        private static String validatedDownloadReference(String reference) {

            String[] segments = reference.split("/", -1);
            if (segments.length != 2 || segments[0].isBlank() || segments[1].isBlank()) {
                throw new IllegalArgumentException(
                        "Download reference must be '{torrentId}/{fileIndex}'");
            }
            try {
                UUID.fromString(segments[0]);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Download reference torrent id must be a UUID", e);
            }
            try {
                int index = Integer.parseInt(segments[1]);
                if (index < 0) {
                    throw new IllegalArgumentException("Download reference file index must be positive");
                }
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Download reference file index must be an integer", e);
            }
            return segments[0] + "/" + segments[1];
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
