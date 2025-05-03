package fr.anisekai.wireless.api.media.enums;

import java.io.File;

/**
 * Represents various multimedia codecs used for video, audio, and subtitle encoding/decoding.
 * <p>
 * Each codec is associated with a {@link CodecType}, a default file extension, and the corresponding library codec name.
 */
public enum Codec {

    // Video Codecs
    @SuppressWarnings("MissingJavadoc") H264(CodecType.VIDEO, "mp4", "libx264"),
    @SuppressWarnings("MissingJavadoc") H265(CodecType.VIDEO, "mp4", "libx265"),
    @SuppressWarnings("MissingJavadoc") VP9(CodecType.VIDEO, "webm", "libvpx-vp9"),
    @SuppressWarnings("MissingJavadoc") AV1(CodecType.VIDEO, "mkv", "libaom-av1"),
    @SuppressWarnings("MissingJavadoc") MPEG2(CodecType.VIDEO, "mpg", "mpeg2video"),
    @SuppressWarnings("MissingJavadoc") PRORES(CodecType.VIDEO, "mov", "prores_ks"),
    @SuppressWarnings("MissingJavadoc") DV(CodecType.VIDEO, "dv", "dvvideo"),

    // Audio Codecs
    @SuppressWarnings("MissingJavadoc") AAC(CodecType.AUDIO, "m4a", "aac"),
    @SuppressWarnings("MissingJavadoc") MP3(CodecType.AUDIO, "mp3", "libmp3lame"),
    @SuppressWarnings("MissingJavadoc") OPUS(CodecType.AUDIO, "ogg", "libopus"),
    @SuppressWarnings("MissingJavadoc") VORBIS(CodecType.AUDIO, "ogg", "libvorbis"),
    @SuppressWarnings("MissingJavadoc") FLAC(CodecType.AUDIO, "flac", "flac"),
    @SuppressWarnings("MissingJavadoc") AC3(CodecType.AUDIO, "ac3", "ac3"),
    @SuppressWarnings("MissingJavadoc") EAC3(CodecType.AUDIO, "eac3", "eac3"),
    @SuppressWarnings("MissingJavadoc") DTS(CodecType.AUDIO, "dts", "libdts"),
    @SuppressWarnings("MissingJavadoc") TRUEHD(CodecType.AUDIO, "mlp", "truehd"),
    @SuppressWarnings("MissingJavadoc") ALAC(CodecType.AUDIO, "m4a", "alac"),
    @SuppressWarnings("MissingJavadoc") WAV(CodecType.AUDIO, "wav", "pcm_s16le"),

    // Subtitle Codecs
    @SuppressWarnings("MissingJavadoc") SRT(CodecType.SUBTITLE, "srt", "srt"),
    @SuppressWarnings("MissingJavadoc") ASS(CodecType.SUBTITLE, "ass", "ass"),
    @SuppressWarnings("MissingJavadoc") PGS(CodecType.SUBTITLE, "sup", "pgssub"),
    @SuppressWarnings("MissingJavadoc") VTT(CodecType.SUBTITLE, "vtt", "webvtt"),
    @SuppressWarnings("MissingJavadoc") DVB_SUB(CodecType.SUBTITLE, "sub", "dvbsub"),
    @SuppressWarnings("MissingJavadoc") MOV_TEXT(CodecType.SUBTITLE, "mov_text", "mov_text"),
    @SuppressWarnings("MissingJavadoc") SSA(CodecType.SUBTITLE, "ssa", "ssa");

    private final CodecType type;
    private final String    extension;
    private final String    libName;

    Codec(CodecType type, String extension, String libName) {

        this.type      = type;
        this.extension = extension;
        this.libName   = libName;
    }

    /**
     * Retrieve the type of this codec (video, audio, or subtitle).
     *
     * @return The {@link CodecType} associated with this codec
     */
    public CodecType getType() {

        return this.type;
    }

    /**
     * Returns the most common file extension associated with this codec.
     *
     * @return The default file extension (e.g., "mp4", "mp3")
     */
    public String getExtension() {

        return this.extension;
    }

    /**
     * Retrieve the system library name used for this codec.
     *
     * @return The encoder library name (e.g., "libx264", "aac")
     */

    public String getLibName() {

        return this.libName;
    }

    /**
     * Attempts to resolve a {@link Codec} from a given string.
     *
     * @param codecName
     *         The name to match (case-insensitive)
     *
     * @return The matching {@link Codec}, or {@code null} if no match is found
     */
    public static Codec fromString(String codecName) {

        for (Codec codec : values()) {
            if (codec.name().equalsIgnoreCase(codecName)) {
                return codec;
            }
        }
        return null;
    }

    /**
     * Attempts to resolve a {@link Codec} based on the extension of the given file.
     *
     * @param file
     *         The file whose extension will be used for matching
     *
     * @return The matching {@link Codec}, or {@code null} if no match is found
     */
    public static Codec fromExtension(File file) {

        String extension = file.getName().substring(file.getName().lastIndexOf(".") + 1);
        return fromExtension(extension);
    }

    /**
     * Attempts to resolve a {@link Codec} based on a file extension.
     *
     * @param extension
     *         The file extension to match (e.g., "mp4", "ogg")
     *
     * @return The matching {@link Codec}, or {@code null} if no match is found
     */
    public static Codec fromExtension(String extension) {

        for (Codec codec : values()) {
            if (codec.getExtension().equalsIgnoreCase(extension)) {
                return codec;
            }
        }
        return null;
    }
}
