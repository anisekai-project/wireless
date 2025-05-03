package fr.anisekai.wireless.api.media.enums;

/**
 * Represents the type of codec: video, audio, or subtitle.
 * <p>
 * Each type is associated with a character used by ffmpeg to identify stream types.
 */
public enum CodecType {

    /**
     * Codec type used for video tracks.
     */
    VIDEO('v'),

    /**
     * Codec type used for audio tracks.
     */
    AUDIO('a'),

    /**
     * Codec type used for subtitle tracks.
     */
    SUBTITLE('s');

    private final char typeChar;

    CodecType(char typeChar) {

        this.typeChar = typeChar;
    }

    /**
     * Returns the character used by ffmpeg to identify this codec type.
     *
     * @return The character representing this codec type
     */
    public char getTypeChar() {

        return this.typeChar;
    }

    /**
     * Attempts to resolve a {@link CodecType} from a given string.
     *
     * @param codecTypeName
     *         The name to match (case-insensitive)
     *
     * @return The matching {@link CodecType}, or {@code null} if no match is found
     */
    public static CodecType fromString(String codecTypeName) {

        for (CodecType type : CodecType.values()) {
            if (type.name().equalsIgnoreCase(codecTypeName)) {
                return type;
            }
        }
        return null;
    }
}
