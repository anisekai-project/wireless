package fr.anisekai.wireless.api.media;

import fr.anisekai.wireless.api.media.enums.Codec;

import java.io.File;

/**
 * Represents a single media stream extracted from a container file.
 * <p>
 * Each stream has an index (as recognized by ffmpeg), a codec type, and an optional language tag. This is used for identifying,
 * processing, and outputting individual media tracks.
 *
 * @param index
 *         The stream index within the container.
 * @param codec
 *         The codec used for this stream.
 * @param language
 *         The ISO 639-1/2 language code, or {@code null} if unspecified.
 */
public record MediaStream(int index, Codec codec, String language) {

    /**
     * Generates a {@link File} representing this stream's output path using its own codec's extension.
     *
     * @param root
     *         The root directory for the output file.
     *
     * @return A {@link File} named with the stream index and its codec's extension (e.g., {@code 0.aac}).
     */
    public File asFile(File root) {

        return new File(root, "%s.%s".formatted(this.index, this.codec.getExtension()));
    }

    /**
     * Generates a {@link File} representing this stream's output path using a custom codec's extension.
     *
     * @param root
     *         The root directory for the output file.
     * @param override
     *         The codec whose extension should be used for the output file.
     *
     * @return A {@link File} named with the stream index and the given codec's extension.
     */
    public File asFile(File root, Codec override) {

        return new File(root, "%s.%s".formatted(this.index, override.getExtension()));
    }

}
