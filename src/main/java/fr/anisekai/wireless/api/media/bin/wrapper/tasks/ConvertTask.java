package fr.anisekai.wireless.api.media.bin.wrapper.tasks;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.Binary;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommand;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommandTask;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Specific implementation {@link FFMpegCommand} allowing to convert a {@link MediaFile} into a single file or split its stream
 * into multiple files.
 *
 * @param <T>
 *         The type of the result for this {@link ConvertTask}.
 */
public final class ConvertTask<T> extends FFMpegCommandTask<T> {

    /**
     * Create a {@link ConvertTask} that will convert a {@link MediaFile} into another file.
     *
     * @param input
     *         The {@link MediaFile} to convert.
     * @param video
     *         The {@link Codec} to use for the video.
     * @param audio
     *         The {@link Codec} to use for the audio.
     * @param subtitle
     *         The {@link Codec} to use for subtitle.
     * @param outputDir
     *         The {@link Path} to use as output directory.
     * @param filename
     *         The filename for the output file.
     *
     * @return A {@link ConvertTask}.
     */
    public static ConvertTask<Path> of(MediaFile input, Codec video, Codec audio, Codec subtitle, @Nullable Path outputDir, String filename) {

        return new ConvertTask<>(ConvertTask::getOutputFile, input, video, audio, subtitle, outputDir, filename);
    }

    /**
     * Create a {@link ConvertTask} that will convert a {@link MediaFile} into multiple file for each stream.
     *
     * @param input
     *         The {@link MediaFile} to convert.
     * @param video
     *         The {@link Codec} to use for the video.
     * @param audio
     *         The {@link Codec} to use for the audio.
     * @param subtitle
     *         The {@link Codec} to use for subtitle.
     * @param outputDir
     *         The {@link Path} to use as output directory.
     * @param streamNameFunction
     *         The {@link Function} that define the naming rule of each stream.
     *
     * @return A {@link ConvertTask}.
     */
    public static ConvertTask<Map<MediaStream, Path>> of(MediaFile input, Codec video, Codec audio, Codec subtitle, @Nullable Path outputDir, BiFunction<MediaStream, Codec, String> streamNameFunction) {

        return new ConvertTask<>(ConvertTask::getOutputFiles, input, video, audio, subtitle, outputDir, streamNameFunction);
    }

    private final Function<ConvertTask<?>, T> resolver;

    private final MediaFile input;
    private final Codec     video;
    private final Codec     audio;
    private final Codec     subtitle;
    private       Path      outputDir;

    private final String                                 filename;
    private final BiFunction<MediaStream, Codec, String> streamNameFunction;

    private final Path                   outputFile;
    private final Map<MediaStream, Path> outputFiles;

    private ConvertTask(Function<ConvertTask<?>, T> resolver, MediaFile input, Codec video, Codec audio, Codec subtitle, @Nullable Path outputDir, String filename) {

        super(Binary.ffmpeg());
        this.resolver           = resolver;
        this.input              = input;
        this.video              = video;
        this.audio              = audio;
        this.subtitle           = subtitle;
        this.outputDir          = outputDir;
        this.filename           = filename;
        this.streamNameFunction = null;
        this.outputFile         = null;
        this.outputFiles        = null;
    }

    private ConvertTask(Function<ConvertTask<?>, T> resolver, MediaFile input, Codec video, Codec audio, Codec subtitle, @Nullable Path outputDir, BiFunction<MediaStream, Codec, String> streamNameFunction) {

        super(Binary.ffmpeg());
        this.resolver           = resolver;
        this.input              = input;
        this.video              = video;
        this.audio              = audio;
        this.subtitle           = subtitle;
        this.outputDir          = outputDir;
        this.filename           = null;
        this.streamNameFunction = streamNameFunction;
        this.outputFile         = null;
        this.outputFiles        = new HashMap<>();
    }

    private Path getOutputFile() {

        return this.outputFile;
    }

    private Map<MediaStream, Path> getOutputFiles() {

        return this.outputFiles;
    }

    /**
     * Map all the stream of the input files to its corresponding codec.
     *
     * @param ffmpeg
     *         The {@link Binary} into which the arguments will be appended.
     * @param onStreamAdded
     *         The {@link BiConsumer} called when a stream get added with a specific codec.
     */
    public void mapStreams(Binary ffmpeg, BiConsumer<MediaStream, Codec> onStreamAdded) {

        BiConsumer<MediaStream, Codec> codecMapper = ((stream, codec) -> {
            ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
            ffmpeg.addArguments("-c:%s".formatted(codec.getType().getChar()), codec.getLibName());
        });

        for (MediaStream stream : this.input.getStreams()) {
            switch (stream.getCodec().getType()) {
                case VIDEO -> {
                    if (this.video == null) continue;
                    codecMapper.andThen(onStreamAdded).accept(stream, this.video);
                }
                case AUDIO -> {
                    if (this.audio == null) continue;
                    codecMapper.andThen(onStreamAdded).accept(stream, this.audio);
                }
                case SUBTITLE -> {
                    if (this.subtitle == null) continue;
                    codecMapper.andThen(onStreamAdded).accept(stream, this.subtitle);
                }
            }
        }
    }

    @Override
    public void preprocess(Binary ffmpeg) throws IOException {

        if (this.outputDir == null) {
            this.outputDir = Path.of(".").toAbsolutePath().normalize();
        }

        ffmpeg.setBaseDir(this.outputDir);
        ffmpeg.addArguments("-i", this.input.getPath().toString());

        if (this.filename == null && this.streamNameFunction != null) {
            this.preprocessMultipleFile(ffmpeg);
            return;
        } else if (this.filename != null && this.streamNameFunction == null) {
            this.preprocessSingleFile(ffmpeg);
            return;
        }

        throw new IllegalStateException("Could not determine how to run ffmpeg");
    }

    private void preprocessSingleFile(Binary ffmpeg) {

        Path outputFile = this.outputDir.resolve(this.filename);

        if (!outputFile.startsWith(this.outputDir)) {
            throw new IllegalArgumentException("Invalid filename: " + this.filename);
        }

        this.mapStreams(
                ffmpeg, (stream, codec) -> {
                    if (codec.getType() == CodecType.VIDEO) {
                        ffmpeg.addArguments("-crf", 25);
                    }
                }
        );

        ffmpeg.addArgument(outputFile.toString());
    }

    private void preprocessMultipleFile(Binary ffmpeg) throws IOException {

        this.mapStreams(
                ffmpeg, (stream, codec) -> {
                    if (codec.getType() == CodecType.VIDEO) {
                        ffmpeg.addArguments("-crf", 25);
                    }

                    Codec  effectiveCodec = codec.isCopyCodec() ? stream.getCodec() : codec;
                    String filename       = this.streamNameFunction.apply(stream, effectiveCodec);

                    Path output = this.outputDir.resolve(filename).normalize();
                    if (!output.startsWith(this.outputDir)) {
                        throw new IllegalArgumentException("Illegal filename: " + filename);
                    }

                    ffmpeg.addArgument(output.getFileName().toString());
                    this.outputFiles.put(stream, output);
                }
        );

        // Ensure outputs do not exist or ffmpeg is going to make a whim
        for (Path outputFile : this.outputFiles.values()) {
            if (Files.isRegularFile(outputFile)) {
                Files.delete(outputFile);
                continue;
            }

            if (Files.exists(outputFile)) {
                throw new IllegalStateException("Could not delete " + outputFile);
            }
        }
    }

    @Override
    public T postprocess(int code) throws IOException {

        return this.resolver.apply(this);
    }

}
