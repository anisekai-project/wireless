package fr.anisekai.wireless.api.media.bin;

import fr.anisekai.wireless.api.json.AnisekaiJson;
import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaMeta;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;

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

        File   temp    = File.createTempFile("anisekai", null);
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
     * Extracts individual streams from the given media file into separate files using specified video and audio codecs. Subtitles
     * streams are copied without re-encoding.
     *
     * @param media
     *         The media file containing streams to extract
     * @param videoCodec
     *         The codec to use for video streams (must be of type VIDEO)
     * @param audioCodec
     *         The codec to use for audio streams (must be of type AUDIO)
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

    public static Map<MediaStream, File> explode(MediaFile media, Codec videoCodec, Codec audioCodec) throws IOException, InterruptedException {

        if (videoCodec.getType() != CodecType.VIDEO) throw new IllegalArgumentException("video codec must be of type video");
        if (audioCodec.getType() != CodecType.AUDIO) throw new IllegalArgumentException("audio codec must be of type audio");

        File                   parent      = media.getFile().getParentFile();
        Map<MediaStream, File> outputFiles = new HashMap<>();

        Binary ffmpeg = Binary.ffmpeg();
        ffmpeg.setBaseDir(parent);

        ffmpeg.addArguments("-i", media.getFile().getName());

        for (MediaStream stream : media.getStreams()) {
            ffmpeg.addArguments("-map", "0:%s".formatted(stream.index()));

            switch (stream.codec().getType()) {
                case VIDEO:
                    ffmpeg.addArguments("-c:v", videoCodec.getLibName());
                    ffmpeg.addArguments("-crf", 25);
                    File videoOutput = stream.asFile(parent, videoCodec);
                    ffmpeg.addArgument(videoOutput.getName());
                    outputFiles.put(stream, videoOutput);
                    break;
                case AUDIO:
                    ffmpeg.addArguments("-c:a", audioCodec.getLibName());
                    File audioOutput = stream.asFile(parent, audioCodec);
                    ffmpeg.addArgument(audioOutput.getName());
                    outputFiles.put(stream, audioOutput);
                    break;
                case SUBTITLE:
                    ffmpeg.addArguments("-c:s", "copy");
                    File subtitleOutput = stream.asFile(parent);
                    ffmpeg.addArgument(subtitleOutput.getName());
                    outputFiles.put(stream, subtitleOutput);
                    break;
            }

        }

        for (File outputFile : outputFiles.values()) {
            if (outputFile.exists()) {
                if (!outputFile.delete()) {
                    throw new IllegalStateException("Could not delete " + outputFile.getAbsolutePath());
                }
            }
        }

        int code = ffmpeg.execute(1, TimeUnit.HOURS);
        if (!outputFiles.values().stream().allMatch(File::exists)) {
            throw new IllegalStateException("ffmpeg(convert) failed with code " + code);
        }
        return outputFiles;
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

        File            output        = File.createTempFile("anisekai", ".mkv");
        Binary          ffmpeg        = Binary.ffmpeg();
        List<MediaMeta> mediaMetaList = Arrays.asList(streams);

        MediaMeta first            = mediaMetaList.getFirst();
        File      workingDirectory = first.getFile().getParentFile();
        Path      workingPath      = workingDirectory.toPath();

        ffmpeg.setBaseDir(workingDirectory);

        Map<Character, Integer> streamTypes = new HashMap<>();
        streamTypes.put('v', 1);
        streamTypes.put('a', 1);
        streamTypes.put('s', 1);

        for (int i = 0; i < mediaMetaList.size(); i++) {
            MediaMeta mediaMeta    = mediaMetaList.get(i);
            Path      mediaPath    = workingPath.relativize(mediaMeta.getFile().toPath());
            char      typeChar     = mediaMeta.getCodec().getType().getTypeChar();
            int       trackTypeNum = streamTypes.get(typeChar);

            streamTypes.put(typeChar, trackTypeNum + 1);

            ffmpeg.addArguments("-i", mediaPath.toString());

            ffmpeg.addHoldArguments("-map", "%s:%s".formatted(i, typeChar));

            if (mediaMeta.getName() != null) {
                ffmpeg.addHoldArguments("-metadata:s:%s".formatted(i), "title=%s".formatted(mediaMeta.getName()));
            } else {
                ffmpeg.addHoldArguments("-metadata:s:%s".formatted(i), "title=Track %s".formatted(trackTypeNum));
            }

            if (mediaMeta.getLanguage() != null) {
                ffmpeg.addHoldArguments("-metadata:s:%s".formatted(i), "language=%s".formatted(mediaMeta.getLanguage()));
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

}
