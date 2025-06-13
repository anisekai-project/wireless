package fr.anisekai.wireless.api.media.enums;

import fr.anisekai.wireless.api.media.MediaStream;
import org.jetbrains.annotations.Nullable;

/**
 * Enum for the most common {@link MediaStream} dispositions.
 */
public enum Disposition {

    /**
     * Indicates that the stream is used by default in the media
     */
    DEFAULT,

    /**
     * Indicates that the stream is a dubbed version
     */
    DUB,

    /**
     * Indicates that the stream is in the original media language
     */
    ORIGINAL,

    /**
     * Indicates that the stream is a commentary
     */
    COMMENT,

    /**
     * Indicates a subtitle stream meant to be always displayed.
     */
    FORCED;

    /**
     * Attempts to resolve a {@link Disposition} from a given string.
     *
     * @param name
     *         The name to match (case-insensitive)
     *
     * @return The matching {@link Disposition}, or {@code null}.
     */
    public static @Nullable Disposition from(String name) {

        for (Disposition disposition : Disposition.values()) {
            if (disposition.name().equalsIgnoreCase(name)) {
                return disposition;
            }
        }
        return null;
    }

}
