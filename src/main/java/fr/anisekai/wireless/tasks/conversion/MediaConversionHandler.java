package fr.anisekai.wireless.tasks.conversion;

import com.github.f4b6a3.uuid.UuidCreator;
import fr.anisekai.media.MediaFile;
import fr.anisekai.media.MediaStream;
import fr.anisekai.media.bin.Binary;
import fr.anisekai.media.bin.FFMpeg;
import fr.anisekai.media.enums.Codec;
import fr.anisekai.media.enums.CodecType;
import fr.anisekai.media.enums.Disposition;
import fr.anisekai.media.interfaces.MediaStreamMapper;
import fr.anisekai.scheduler.tasking.interfaces.structure.TaskHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Converts an episode according to a {@link MediaConversionInput} while delegating transport and storage operations to
 * the client implementation.
 */
public abstract class MediaConversionHandler implements TaskHandler<MediaConversionInput, MediaConversionOutput> {

    /**
     * Creates a media conversion handler.
     */
    protected MediaConversionHandler() {

    }

    /**
     * Creates the persistent track descriptor for a converted media stream.
     *
     * @param stream
     *         Source stream metadata.
     * @param targetCodec
     *         Codec used in the converted output.
     *
     * @return The converted track descriptor.
     */
    public static MediaConversionOutput.Track toTrack(MediaStream stream, Codec targetCodec) {

        String name;
        if (stream.getMetadata().containsKey("title")) {
            name = stream.getMetadata().get("title");
        } else {
            name = "Track %s".formatted(stream.getId());
        }

        return new MediaConversionOutput.Track(
                UuidCreator.getTimeOrderedEpoch(),
                name,
                targetCodec,
                stream.getMetadata().get("language"),
                Disposition.toBits(stream.getDispositions())
        );
    }

    @Override
    public @NotNull MediaConversionOutput handle(@NotNull MediaConversionInput arguments) throws Exception {

        Objects.requireNonNull(arguments, "arguments");

        Path      source      = null;
        Path      destination = null;
        Throwable failure     = null;

        try {
            source = Objects.requireNonNull(this.fetchEpisode(arguments.episode()), "Fetched source path");
            validateSource(source, arguments.episode().hash());

            MediaFile   media = this.probe(source);
            TrackMapper mapper = new TrackMapper();

            destination = validateDestination(this.getStoragePath(arguments.episode(), source), source);
            Path output = this.convert(media, arguments.conversionOptions(), mapper, destination);

            validateOutput(output, destination);
            this.probe(destination);
            this.pushEpisode(arguments.episode(), destination);
            return new MediaConversionOutput(mapper.getTracks());
        } catch (Exception | Error e) {
            failure = e;
            throw e;
        } finally {
            try {
                this.cleanupEpisode(arguments.episode(), source, destination);
            } catch (Exception cleanupFailure) {
                if (failure == null) {
                    throw cleanupFailure;
                }
                failure.addSuppressed(cleanupFailure);
            }
        }
    }

    MediaFile probe(Path path) throws IOException, InterruptedException {

        return MediaFile.of(path);
    }

    protected Path convert(MediaFile media, MediaConversionInput.ConversionOptions options, TrackMapper mapper, Path destination) throws IOException, InterruptedException {

        return FFMpeg.convert(media)
                     .video(options.videoCodec())
                     .audio(options.audioCodec())
                     .subtitle(options.subtitlesCodec())
                     .streamMapper(mapper)
                     .file(destination)
                     .timeout(3, TimeUnit.HOURS)
                     .cancellable(this.cancelSignal())
                     .run();
    }

    /**
     * Cancellation signal consulted while converting. When it turns {@code true}, the running
     * ffmpeg process is destroyed and conversion aborts with an {@link InterruptedException}.
     * <p>
     * Defaults to never cancelled. Long-running clients (e.g. remote workers abandoning a task)
     * override this to wire their own abandonment flag.
     *
     * @return The cancellation signal for the conversion step.
     */
    @NotNull
    protected BooleanSupplier cancelSignal() {

        return () -> false;
    }

    static String sha256(Path path) throws IOException {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (
                    InputStream input = Files.newInputStream(path);
                    DigestInputStream digestInput = new DigestInputStream(input, digest)
            ) {
                digestInput.transferTo(OutputStream.nullOutputStream());
            }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private static void validateSource(Path source, String expectedHash) throws IOException {

        if (!Files.isRegularFile(source) || !Files.isReadable(source)) {
            throw new IllegalArgumentException("Source must be a readable regular file: " + source);
        }

        if (!MessageDigest.isEqual(expectedHash.getBytes(), sha256(source).getBytes())) {
            throw new IllegalStateException("Source SHA-256 hash does not match: " + source);
        }
    }

    private static Path validateDestination(Path destination, Path source) {

        Objects.requireNonNull(destination, "Destination path");
        Path normalized = destination.toAbsolutePath().normalize();

        if (normalized.equals(source.toAbsolutePath().normalize())) {
            throw new IllegalArgumentException("Destination must differ from source");
        }
        if (Files.exists(normalized)) {
            throw new IllegalArgumentException("Destination already exists: " + normalized);
        }
        if (normalized.getParent() == null || !Files.isDirectory(normalized.getParent())) {
            throw new IllegalArgumentException("Destination parent must be an existing directory: " + normalized);
        }
        return normalized;
    }

    private static void validateOutput(Path output, Path destination) throws IOException {

        Objects.requireNonNull(output, "Conversion output path");
        if (!output.toAbsolutePath().normalize().equals(destination)) {
            throw new IllegalStateException("Conversion returned an unexpected output path: " + output);
        }
        if (!Files.isRegularFile(destination) || !Files.isReadable(destination) || Files.size(destination) == 0) {
            throw new IllegalStateException("Conversion did not create a readable, non-empty output: " + destination);
        }
    }

    /**
     * Retrieve the path to the episode to convert. This method can be used to download the required files.
     *
     * @param episode
     *         The episode to fetch.
     *
     * @return The path to the episode
     *
     * @throws Exception
     *         If the episode cannot be fetched.
     */
    public abstract Path fetchEpisode(MediaConversionInput.Episode episode) throws Exception;

    /**
     * Retrieve the path for the destination file (converted file). This can either lead to a temporary file or a
     * defined path.
     *
     * @param episode
     *         The episode to convert.
     * @param source
     *         The source path of the file that will be converted.
     *
     * @return The path to the converted file.
     *
     * @throws Exception
     *         If the destination cannot be prepared.
     */
    public abstract Path getStoragePath(MediaConversionInput.Episode episode, Path source) throws Exception;

    /**
     * Publish the episode to the destination folder. This can be either a move operation or an upload.
     *
     * @param episode
     *         The episode to push
     * @param path
     *         The path to the episode file to push
     *
     * @throws Exception
     *         If the converted episode cannot be published.
     */
    public abstract void pushEpisode(MediaConversionInput.Episode episode, Path path) throws Exception;

    /**
     * Clean up local conversion resources. This method is always called after handling starts, including when fetching,
     * probing, conversion, validation, or publishing fails. Either path can be {@code null} if it was not resolved.
     *
     * @param episode
     *         The episode being converted.
     * @param source
     *         The fetched source, or {@code null} if fetching failed.
     * @param destination
     *         The requested output, or {@code null} if destination resolution failed.
     *
     * @throws Exception
     *         If local resources cannot be cleaned up.
     */
    public void cleanupEpisode(MediaConversionInput.Episode episode, @Nullable Path source, @Nullable Path destination) throws Exception {

    }

    /**
     * Maps source streams to FFmpeg output arguments and records their converted track descriptors.
     */
    public static class TrackMapper implements MediaStreamMapper {

        private final AtomicInteger                     counter = new AtomicInteger(0);
        private final List<MediaConversionOutput.Track> tracks  = new ArrayList<>();

        /**
         * Creates an empty stream mapper.
         */
        public TrackMapper() {

        }

        /**
         * Returns an immutable snapshot of the mapped tracks.
         *
         * @return The mapped track descriptors.
         */
        public List<MediaConversionOutput.Track> getTracks() {

            return List.copyOf(this.tracks);
        }

        @Override
        public void map(Binary binary, MediaStream stream, Codec codec) {

            if (stream.getDispositions().contains(Disposition.ATTACHED_PIC)) {
                return;
            }

            MediaConversionOutput.Track track = toTrack(stream, codec);
            this.tracks.add(track);

            binary.addArguments(
                    "-map",
                    String.format("0:%s", stream.getId())
            );

            binary.addArguments(
                    String.format("-c:%s", codec.getType().getChar()),
                    codec.getLibName()
            );

            binary.addArguments(
                    String.format("-metadata:s:%s", this.counter.getAndIncrement()),
                    String.format("anisekai=%s", track.uuid())
            );

            if (codec.getType() == CodecType.VIDEO && !codec.isCopyCodec()) {
                binary.addArguments("-crf", 25);
                binary.addArguments("-vf", "format=yuv420p");
            } else if (codec.getType() == CodecType.AUDIO && !codec.isCopyCodec()) {
                binary.addArguments("-ac", "2");
            }
        }

    }

}
