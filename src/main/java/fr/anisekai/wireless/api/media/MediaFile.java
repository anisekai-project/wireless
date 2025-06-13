package fr.anisekai.wireless.api.media;

import fr.anisekai.wireless.api.json.AnisekaiArray;
import fr.anisekai.wireless.api.json.AnisekaiJson;
import fr.anisekai.wireless.api.media.bin.FFMpeg;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a multimedia file composed of various media streams such as audio, video, or subtitles.
 * <p>
 * This class wraps a physical {@link File} and the set of {@link MediaStream} tracks detected within it, parsed via ffmpeg.
 */
public final class MediaFile {

    /**
     * Parses a given file using ffmpeg and constructs a {@link MediaFile} from the detected streams.
     *
     * @param file
     *         The file to analyze.
     *
     * @return A {@link MediaFile} containing the parsed media streams.
     *
     * @throws IOException
     *         Threw if an I/O error occurs during probing or reading.
     * @throws InterruptedException
     *         Threw if the probing process is interrupted.
     */
    public static MediaFile of(File file) throws IOException, InterruptedException {

        AnisekaiJson     json    = FFMpeg.probe(file);
        Set<MediaStream> streams = new HashSet<>();

        AnisekaiArray streamArray = json.readArray("streams");
        for (int i = 0; i < streamArray.length(); i++) {
            AnisekaiJson streamData = streamArray.getAnisekaiJson(i);

            CodecType type = CodecType.from(streamData.getString("codec_type"));
            if (type == null) continue; // Unsupported type of stream, just skip it.
            Codec codec = Codec.from(streamData.getString("codec_name"));
            if (codec == null) {
                throw new UnsupportedEncodingException("Unsupported codec: " + streamData.getString("codec_name"));
            }

            MediaStream stream = new MediaStream(codec, streamData);
            streams.add(stream);
        }

        return new MediaFile(file, streams);
    }

    private final File             file;
    private final Set<MediaStream> streams;

    private MediaFile(File file, Set<MediaStream> streams) {

        this.file    = file;
        this.streams = Collections.unmodifiableSet(streams);
    }

    /**
     * Retrieve the physical file associated with this {@link MediaFile}.
     *
     * @return The underlying {@link File}.
     */
    public File getFile() {

        return this.file;
    }

    /**
     * Retrieve all media streams detected in the file.
     *
     * @return An unmodifiable {@link Set} of {@link MediaStream} objects.
     */
    public Set<MediaStream> getStreams() {

        return this.streams;
    }

    /**
     * Retrieve all media streams of the specified type (e.g., video, audio, subtitles).
     *
     * @param type
     *         The {@link CodecType} to filter by.
     *
     * @return A {@link Set} of streams matching the given type.
     */
    public Set<MediaStream> getStreams(CodecType type) {

        return this.streams.stream().filter(stream -> stream.getCodec().getType() == type).collect(Collectors.toSet());
    }

}
