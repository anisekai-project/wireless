package fr.anisekai.wireless.api.media.bin.wrapper.commands;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommand;
import fr.anisekai.wireless.api.media.bin.wrapper.tasks.ConvertTask;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A {@link FFMpegCommand} builder specifically built to convert a {@link MediaFile} streams into a single or multiple files.
 */
public class ConvertCommandBuilder {

    private final MediaFile input;
    private       Codec     video;
    private       Codec     audio;
    private       Codec     subtitle;
    private       Path      outputDir;

    /**
     * Create a new {@link ConvertCommandBuilder} targeting the provided {@link MediaFile}.
     *
     * @param input
     *         A {@link MediaFile}.
     */
    public ConvertCommandBuilder(MediaFile input) {

        this.input = input;
    }

    /**
     * Disable all video streams.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder noVideo() {

        this.video = null;
        return this;
    }

    /**
     * Copy all video streams (disable encoding).
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder copyVideo() {

        this.video = Codec.VIDEO_COPY;
        return this;
    }

    /**
     * Convert all video streams using the provided {@link Codec}.
     *
     * @param video
     *         The {@link Codec} for video encoding.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder video(Codec video) {

        if (video.getType() != CodecType.VIDEO) {
            throw new IllegalArgumentException(String.format("The codec '%s' is not a video codec.", video));
        }
        this.video = video;
        return this;
    }

    /**
     * Disable all audio streams.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder noAudio() {

        this.audio = null;
        return this;
    }

    /**
     * Copy all audio streams (disable encoding).
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder copyAudio() {

        this.audio = Codec.AUDIO_COPY;
        return this;
    }

    /**
     * Convert all audio streams using the provided {@link Codec}.
     *
     * @param audio
     *         The {@link Codec} for audio encoding.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder audio(Codec audio) {

        if (audio.getType() != CodecType.AUDIO) {
            throw new IllegalArgumentException(String.format("The codec '%s' is not an audio codec.", audio));
        }
        this.audio = audio;
        return this;
    }

    /**
     * Disable all subtitle streams.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder noSubtitle() {

        this.subtitle = null;
        return this;
    }

    /**
     * Copy all subtitle streams (disable encoding).
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder copySubtitle() {

        this.subtitle = Codec.SUBTITLE_COPY;
        return this;
    }

    /**
     * Convert all subtitle streams using the provided {@link Codec}.
     *
     * @param subtitle
     *         The {@link Codec} for subtitle encoding.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder subtitle(Codec subtitle) {

        if (subtitle.getType() != CodecType.SUBTITLE) {
            throw new IllegalArgumentException(String.format("The codec '%s' is not a subtitle codec.", subtitle));
        }
        this.subtitle = subtitle;
        return this;
    }

    /**
     * Set the base {@link Path} into which ffmpeg will be run. If using {@link #split()} or {@link #split(BiFunction)}, this will
     * define the directory into which all files will be extracted. If using {@link #file(String)}, this will be used as the
     * containing directory to resolve the full path of the output.
     *
     * @param directory
     *         The {@link Path} pointing to a directory.
     *
     * @return The same instance for chaining.
     */
    public ConvertCommandBuilder into(Path directory) {

        Path normalized = directory.toAbsolutePath().normalize();

        if (!Files.isDirectory(normalized)) {
            throw new IllegalArgumentException(String.format("The path '%s' is not a directory", normalized));
        }

        this.outputDir = normalized;
        return this;
    }

    /**
     * Set the output {@link Path} into which the {@link MediaFile} will be converted. This will also feed the parent path of the
     * provided one into {@link #into(Path)}.
     *
     * @param file
     *         The {@link Path} pointing to a directory
     *
     * @return A {@link FFMpegCommand} ready to convert the {@link MediaFile} into a single file.
     */
    public FFMpegCommand<Path> file(Path file) {

        Path normalized = file.toAbsolutePath().normalize();

        if (Files.exists(normalized)) {
            throw new IllegalArgumentException(String.format("The path '%s' already exists", normalized));
        }

        return this.into(normalized.getParent()).file(normalized.getFileName().toString());
    }

    /**
     * Set the output filename into which the {@link MediaFile} will be converted. If {@link #into(Path)} was not called earlier,
     * the default runtime working directory will be used.
     *
     * @param filename
     *         The filename
     *
     * @return A {@link FFMpegCommand} ready to convert the {@link MediaFile} into a single file.
     */
    public FFMpegCommand<Path> file(String filename) {

        return ConvertTask.of(
                this.input,
                this.video,
                this.audio,
                this.subtitle,
                this.outputDir,
                filename
        );
    }

    /**
     * Set the default rules of stream naming for the {@link MediaFile} conversion.
     *
     * @return A {@link FFMpegCommand} ready to convert the {@link MediaFile} streams into separate files.
     */
    public FFMpegCommand<Map<MediaStream, Path>> split() {

        return this.split((stream, codec) -> String.format("%s.%s", stream.getId(), codec.getExtension()));
    }

    /**
     * Set a custom rules of stream naming for the {@link MediaFile} conversion.
     *
     * @param streamNamingFunction
     *         The {@link Function} used to name each {@link MediaStream} being converted.
     *
     * @return A {@link FFMpegCommand} ready to convert the {@link MediaFile} streams into separate files.
     */
    public FFMpegCommand<Map<MediaStream, Path>> split(BiFunction<MediaStream, Codec, String> streamNamingFunction) {

        return ConvertTask.of(
                this.input,
                this.video,
                this.audio,
                this.subtitle,
                this.outputDir,
                streamNamingFunction
        );
    }

}
