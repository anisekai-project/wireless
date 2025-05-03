package fr.anisekai.wireless.api.media;

import fr.anisekai.wireless.api.media.bin.FFMpeg;
import fr.anisekai.wireless.api.media.enums.Codec;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * Represents a single media track (such as audio, video, or subtitle) used to reassemble a complete media file.
 * <p>
 * This class encapsulates the metadata of a media file, including its file path, optional name, language, and codec. It is
 * typically used as input for the {@link FFMpeg#combine(MediaMeta...)} method to combine tracks into a unified media container.
 */
public class MediaMeta {


    private final File   file;
    private final String name;
    private final String language;
    private final Codec  codec;

    /**
     * Create a {@link MediaMeta} instance for a given media file with optional metadata.
     *
     * @param file
     *         The media file.
     * @param name
     *         The name of the track.
     * @param language
     *         The language of the track.
     *
     * @throws IllegalArgumentException
     *         Threw if the codec cannot be determined from the file extension.
     */
    public MediaMeta(File file, @Nullable String name, @Nullable String language) {

        this.file     = file;
        this.name     = name;
        this.language = language;
        this.codec    = Codec.fromExtension(file);

        if (this.codec == null) {
            throw new IllegalArgumentException("Couldn't find codec for " + file);
        }
    }

    /**
     * Retrieve the media file associated with this track.
     *
     * @return The {@link File} representing the media track.
     */
    public File getFile() {

        return this.file;
    }

    /**
     * Retrieve the name of the media track, or {@code null} if unspecified.
     *
     * @return The name of the track.
     */
    public String getName() {

        return this.name;
    }

    /**
     * Retrieve the language of the media track, or {@code null} if unspecified.
     *
     * @return The language of the track.
     */
    public String getLanguage() {

        return this.language;
    }

    /**
     * Retrieve the codec detected for this media track based on its file extension.
     *
     * @return The {@link Codec} used for this track.
     */
    public Codec getCodec() {

        return this.codec;
    }

}
