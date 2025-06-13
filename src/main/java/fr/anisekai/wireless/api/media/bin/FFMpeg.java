package fr.anisekai.wireless.api.media.bin;

import fr.anisekai.wireless.api.json.AnisekaiJson;
import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaMeta;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Utility class providing static methods to interact with ffmpeg and ffprobe binaries.
 * <p>
 * Supports probing media files for stream information, extracting streams into separate files, and combining multiple media
 * streams into a single output file.
 */
public final class FFMpeg {

    private FFMpeg() {}

    private static void controlCodecs(@Nullable Codec videoCodec, @Nullable Codec audioCodec, @Nullable Codec subsCodec) {

        if (videoCodec != null && videoCodec.getType() != CodecType.VIDEO) {
            throw new IllegalArgumentException("video codec must be of type video");
        }

        if (audioCodec != null && audioCodec.getType() != CodecType.AUDIO) {
            throw new IllegalArgumentException("audio codec must be of type audio");
        }

        if (subsCodec != null && subsCodec.getType() != CodecType.SUBTITLE) {
            throw new IllegalArgumentException("subtitles codec must be of type subtitles");
        }
    }

    /**
     * Probes the specified media file using ffprobe and returns detailed stream information as {@link AnisekaiJson}.
     *
     * @param file
     *         The media file to probe
     *
     * @return An {@link AnisekaiJson} representing the probe output
     *
     * @throws IOException
     *         Threw if an I/O error occurs during probing or reading the temporary file
     * @throws InterruptedException
     *         Threw if the probing process is interrupted
     * @throws IllegalStateException
     *         Threw if ffprobe fails to create the output file
     */

    public static AnisekaiJson probe(File file) throws IOException, InterruptedException {

        File   temp    = java.io.File.createTempFile("anisekai", null);
        Binary ffprobe = Binary.ffprobe();
        ffprobe.setBaseDir(file.getParentFile());

        ffprobe.addArgument("-show_streams");
        ffprobe.addArguments("-of", "json");
        ffprobe.addArguments("-i", file.getName());
        ffprobe.addArguments("-o", temp.getAbsolutePath());

        int code = ffprobe.execute(1, TimeUnit.MINUTES);
        if (!temp.exists()) {
            throw new IllegalStateException("ffprobe failed with code " + code);
        }

        AnisekaiJson json = new AnisekaiJson(Files.readString(temp.toPath(), StandardCharsets.UTF_8));
        temp.delete();
        return json;
    }

    /**
     * Extracts individual streams from the given media file into separate files using specified codecs.
     *
     * @param media
     *         The media file containing streams to extract
     * @param videoCodec
     *         The codec to use for video streams (must be of type VIDEO). Set to {@code null} to ignore video streams.
     * @param audioCodec
     *         The codec to use for audio streams (must be of type AUDIO). Set to {@code null} to ignore audio streams.
     * @param subsCodec
     *         The codec to use for subtitles streams (must be of type SUBTITLES). Set to {@code null} to ignore subtitles
     *         streams.
     * @param hourTimeout
     *         Maximum amount of hours to wait for the conversion to finish.
     *
     * @return A {@link List} of {@link File} representing the extracted streams
     *
     * @throws IOException
     *         Threw if an I/O error occurs during extraction
     * @throws InterruptedException
     *         Threw if the extraction process is interrupted
     * @throws IllegalArgumentException
     *         Threw if the provided codecs are not of the expected types
     * @throws IllegalStateException
     *         Threw if deletion of pre-existing output files fails or if ffmpeg execution fails
     */

    public static Map<MediaStream, File> explode(MediaFile media, @Nullable Codec videoCodec, @Nullable Codec audioCodec, @Nullable Codec subsCodec, int hourTimeout) throws IOException, InterruptedException {

        controlCodecs(videoCodec, audioCodec, subsCodec);

        File                   parent      = media.getFile().getParentFile();
        Map<MediaStream, File> outputFiles = new HashMap<>();

        Binary ffmpeg = Binary.ffmpeg();
        ffmpeg.setBaseDir(parent);

        ffmpeg.addArguments("-i", media.getFile().getName());

        for (MediaStream stream : media.getStreams()) {

            switch (stream.getCodec().getType()) {
                case VIDEO:
                    if (videoCodec == null) continue;

                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:v", videoCodec.getLibName());
                    ffmpeg.addArguments("-crf", 25);

                    String videoExt = videoCodec.isCopyCodec() ? stream.getCodec().getExtension() : videoCodec.getExtension();
                    String videoName = "%s.%s".formatted(stream.getId(), videoExt);
                    File videoOutput = new File(parent, videoName);

                    ffmpeg.addArgument(videoOutput.getName());
                    outputFiles.put(stream, videoOutput);
                    break;
                case AUDIO:
                    if (audioCodec == null) continue;

                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:a", audioCodec.getLibName());

                    String audioExt = audioCodec.isCopyCodec() ? stream.getCodec().getExtension() : audioCodec.getExtension();
                    String audioName = "%s.%s".formatted(stream.getId(), audioExt);
                    File audioOutput = new File(parent, audioName);

                    ffmpeg.addArgument(audioOutput.getName());
                    outputFiles.put(stream, audioOutput);
                    break;
                case SUBTITLE:
                    if (subsCodec == null) continue;

                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:s", subsCodec.getLibName());

                    String subsExt = subsCodec.isCopyCodec() ? stream.getCodec().getExtension() : subsCodec.getExtension();
                    String subsName = "%s.%s".formatted(stream.getId(), subsExt);
                    File subsOutput = new File(parent, subsName);

                    ffmpeg.addArgument(subsOutput.getName());
                    outputFiles.put(stream, subsOutput);
                    break;
            }

        }

        // Ensure outputs do not exist or ffmpeg is going to make a whim
        for (File outputFile : outputFiles.values()) {
            if (outputFile.exists()) {
                if (!outputFile.delete()) {
                    throw new IllegalStateException("Could not delete " + outputFile.getAbsolutePath());
                }
            }
        }

        int code = ffmpeg.execute(hourTimeout, TimeUnit.HOURS);
        if (!outputFiles.values().stream().allMatch(java.io.File::exists)) {
            throw new IllegalStateException("ffmpeg(convert) failed with code " + code);
        }
        return outputFiles;
    }

    /**
     * Convert individual streams from the given media file into another files using specified codecs.
     *
     * @param media
     *         The media file containing streams to convert
     * @param videoCodec
     *         The codec to use for video streams (must be of type VIDEO). Set to {@code null} to ignore video streams.
     * @param audioCodec
     *         The codec to use for audio streams (must be of type AUDIO). Set to {@code null} to ignore audio streams.
     * @param subsCodec
     *         The codec to use for subtitles streams (must be of type SUBTITLES). Set to {@code null} to ignore subtitles
     *         streams.
     * @param hourTimeout
     *         Maximum amount of hours to wait for the conversion to finish.
     *
     * @return A {@link File} representing the converted file
     *
     * @throws IOException
     *         Threw if an I/O error occurs during extraction
     * @throws InterruptedException
     *         Threw if the extraction process is interrupted
     * @throws IllegalArgumentException
     *         Threw if the provided codecs are not of the expected types
     * @throws IllegalStateException
     *         Threw if deletion of pre-existing output files fails or if ffmpeg execution fails
     */
    public static File convert(MediaFile media, @Nullable Codec videoCodec, @Nullable Codec audioCodec, @Nullable Codec subsCodec, int hourTimeout) throws IOException, InterruptedException {

        File output = File.createTempFile("anisekai", ".mkv");
        convert(media, videoCodec, audioCodec, subsCodec, output, hourTimeout);
        return output;
    }

    /**
     * Convert individual streams from the given media file into another files using specified codecs.
     *
     * @param media
     *         The media file containing streams to convert
     * @param videoCodec
     *         The codec to use for video streams (must be of type VIDEO). Set to {@code null} to ignore video streams.
     * @param audioCodec
     *         The codec to use for audio streams (must be of type AUDIO). Set to {@code null} to ignore audio streams.
     * @param subsCodec
     *         The codec to use for subtitles streams (must be of type SUBTITLES). Set to {@code null} to ignore subtitles
     *         streams.
     * @param output
     *         The output {@link File} of the conversion.
     * @param hourTimeout
     *         Maximum amount of hours to wait for the conversion to finish.
     *
     * @throws IOException
     *         Threw if an I/O error occurs during extraction
     * @throws InterruptedException
     *         Threw if the extraction process is interrupted
     * @throws IllegalArgumentException
     *         Threw if the provided codecs are not of the expected types
     * @throws IllegalStateException
     *         Threw if deletion of pre-existing output files fails or if ffmpeg execution fails
     */
    public static void convert(MediaFile media, @Nullable Codec videoCodec, @Nullable Codec audioCodec, @Nullable Codec subsCodec, File output, int hourTimeout) throws IOException, InterruptedException {

        controlCodecs(videoCodec, audioCodec, subsCodec);

        File parent = media.getFile().getParentFile();

        Binary ffmpeg = Binary.ffmpeg();
        ffmpeg.setBaseDir(parent);

        ffmpeg.addArguments("-i", media.getFile().getName());

        for (MediaStream stream : media.getStreams()) {

            switch (stream.getCodec().getType()) {
                case VIDEO:
                    if (videoCodec == null) continue;
                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:v", videoCodec.getLibName());
                    ffmpeg.addArguments("-crf", 25);
                    break;
                case AUDIO:
                    if (audioCodec == null) continue;

                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:a", audioCodec.getLibName());
                    break;
                case SUBTITLE:
                    if (subsCodec == null) continue;
                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    ffmpeg.addArguments("-c:s", subsCodec.getLibName());
                    break;
            }
        }

        ffmpeg.addArguments(output.getAbsolutePath());

        int code = ffmpeg.execute(hourTimeout, TimeUnit.HOURS);

        if (!output.exists()) {
            throw new IllegalStateException("ffmpeg(convert) failed with code " + code);
        }
    }

    /**
     * Combines multiple media streams into a single container file using stream copy. Metadata such as track titles and language
     * are preserved or set if available.
     *
     * @param streams
     *         The media streams to combine
     *
     * @return A {@link File} representing the combined output file
     *
     * @throws IOException
     *         Threw if an I/O error occurs during processing or output file deletion
     * @throws InterruptedException
     *         Threw if the combining process is interrupted
     * @throws IllegalStateException
     *         Threw if ffmpeg fails to produce the output file
     */
    public static File combine(MediaMeta... streams) throws IOException, InterruptedException {

        File            output        = java.io.File.createTempFile("anisekai", ".mkv");
        Binary          ffmpeg        = Binary.ffmpeg();
        List<MediaMeta> mediaMetaList = Arrays.asList(streams);

        MediaMeta first            = mediaMetaList.getFirst();
        File      workingDirectory = first.getFile().getParentFile();
        Path      workingPath      = workingDirectory.toPath();

        ffmpeg.setBaseDir(workingDirectory);

        Map<CodecType, Integer> streamTypes = new HashMap<>();
        streamTypes.put(CodecType.VIDEO, 1);
        streamTypes.put(CodecType.AUDIO, 1);
        streamTypes.put(CodecType.SUBTITLE, 1);

        for (int i = 0; i < mediaMetaList.size(); i++) {
            MediaMeta mediaMeta = mediaMetaList.get(i);
            Path      mediaPath = workingPath.relativize(mediaMeta.getFile().toPath());
            CodecType type      = mediaMeta.getCodecType();

            streamTypes.compute(type, (k, trackTypeNum) -> trackTypeNum + 1);

            ffmpeg.addArguments("-i", mediaPath.toString());
            ffmpeg.addHoldArguments("-map", "%s:%s".formatted(i, type.getChar()));

            for (String key : mediaMeta.getMetadata().keySet()) {
                String value = mediaMeta.getMetadata().get(key);
                ffmpeg.addHoldArguments("-metadata:s:%s".formatted(i), "%s=%s".formatted(key, value));
            }
        }

        ffmpeg.commitHoldArguments();

        ffmpeg.addArguments("-c", "copy");
        ffmpeg.addArguments(output.getAbsolutePath());

        if (output.exists()) {
            if (!output.delete()) {
                throw new IOException("Failed to delete output file: " + output.getAbsolutePath());
            }
        }

        int code = ffmpeg.execute(1, TimeUnit.MINUTES);
        if (!output.exists()) {
            throw new IllegalStateException("ffmpeg(combine) failed with code " + code);
        }

        return output;
    }

    /**
     * Create an MPD meta file for the specified tracks.
     *
     * @param media
     *         The {@link MediaFile} for which the MPD metadata should be created.
     * @param output
     *         Directory into which the MPD and all the chunks will be generated.
     *
     * @return The MPD file.
     *
     * @throws IOException
     *         Threw if an I/O error occurs during processing or output file deletion
     * @throws InterruptedException
     *         Threw if the generation process is interrupted
     * @throws IllegalStateException
     *         Threw if ffmpeg fails to produce the output file
     */
    public static File createMpd(MediaFile media, File output) throws IOException, InterruptedException {

        File   mpd    = new File(output, "meta.mpd");
        Binary ffmpeg = Binary.ffmpeg();

        File parent = media.getFile().getParentFile();

        ffmpeg.setBaseDir(parent);

        ffmpeg.addArguments("-i", media.getFile().getName());

        List<String> adaptationSets = new ArrayList<>();

        int id = 0;
        for (MediaStream stream : media.getStreams()) {
            switch (stream.getCodec().getType()) {
                case VIDEO, AUDIO:
                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    adaptationSets.add("id=%s,streams=%s".formatted(id, stream.getId()));
                    id++;
                    break;
            }
        }

        ffmpeg.addArguments("-c", "copy");
        ffmpeg.addArguments("-adaptation_sets", String.join(" ", adaptationSets));
        ffmpeg.addArguments(mpd.getAbsolutePath());

        // Ensure output is empty or ffmpeg is going to make a whim
        for (File outputFile : output.listFiles()) {
            if (!outputFile.delete()) {
                throw new IllegalStateException("Could not delete " + outputFile.getAbsolutePath());
            }
        }

        int code = ffmpeg.execute(1, TimeUnit.HOURS);
        if (!mpd.exists()) {
            throw new IllegalStateException("ffmpeg(convert) failed with code " + code);
        }
        return mpd;
    }

}
