package fr.anisekai.wireless.tasks.conversion;

import fr.anisekai.media.MediaFile;
import fr.anisekai.media.MediaStream;
import fr.anisekai.media.bin.Binary;
import fr.anisekai.media.enums.Codec;
import fr.anisekai.media.enums.Disposition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class MediaConversionHandlerTest {

    @TempDir
    Path temporaryDirectory;

    @Test
    void mapperReportsTargetCodecAndEmbedsUuidV7Metadata() {

        MediaStream stream = stream(2, Codec.FLAC, EnumSet.noneOf(Disposition.class));
        Binary binary = mock(Binary.class);
        MediaConversionHandler.TrackMapper mapper = new MediaConversionHandler.TrackMapper();

        mapper.map(binary, stream, Codec.AAC);

        MediaConversionOutput.Track track = mapper.getTracks().getFirst();
        assertEquals(Codec.AAC, track.codec());
        assertEquals(7, track.uuid().version());
        verify(binary).addArguments("-map", "0:2");
        verify(binary).addArguments("-c:a", "aac");
        verify(binary).addArguments("-metadata:s:0", "anisekai=" + track.uuid());
        verify(binary).addArguments("-ac", "2");
    }

    @Test
    void mapperDoesNotAddTranscodingOptionsToCopyCodecs() {

        Binary videoBinary = mock(Binary.class);
        new MediaConversionHandler.TrackMapper().map(
                videoBinary,
                stream(0, Codec.H264, EnumSet.noneOf(Disposition.class)),
                Codec.VIDEO_COPY
        );

        verify(videoBinary, never()).addArguments("-crf", 25);
        verify(videoBinary, never()).addArguments("-vf", "format=yuv420p");

        Binary audioBinary = mock(Binary.class);
        new MediaConversionHandler.TrackMapper().map(
                audioBinary,
                stream(1, Codec.AAC, EnumSet.noneOf(Disposition.class)),
                Codec.AUDIO_COPY
        );

        verify(audioBinary, never()).addArguments("-ac", "2");
    }

    @Test
    void mapperSkipsAttachedPicturesButKeepsLegitimateImageVideo() {

        MediaConversionHandler.TrackMapper mapper = new MediaConversionHandler.TrackMapper();
        Binary attachedPictureBinary = mock(Binary.class);
        mapper.map(attachedPictureBinary, stream(0, Codec.MJPEG, EnumSet.of(Disposition.ATTACHED_PIC)), Codec.H264);

        assertTrue(mapper.getTracks().isEmpty());
        verifyNoInteractions(attachedPictureBinary);

        Binary videoBinary = mock(Binary.class);
        mapper.map(videoBinary, stream(1, Codec.MJPEG, EnumSet.noneOf(Disposition.class)), Codec.H264);

        assertEquals(1, mapper.getTracks().size());
        verify(videoBinary).addArguments("-map", "0:1");
    }

    @Test
    void rejectsHashMismatchBeforeProbeAndAlwaysCleansUp() throws Exception {

        Path source = Files.writeString(this.temporaryDirectory.resolve("source.mkv"), "source");
        TestHandler handler = new TestHandler(source, this.temporaryDirectory.resolve("output.mkv"));
        MediaConversionInput input = input("0".repeat(64));

        assertThrows(IllegalStateException.class, () -> handler.handle(input));
        assertFalse(handler.probed);
        assertTrue(handler.cleaned);
        assertEquals(source, handler.cleanedSource);
    }

    @Test
    void validatesAndProbesOutputBeforePushThenCleansUp() throws Exception {

        Path source = Files.writeString(this.temporaryDirectory.resolve("source.mkv"), "source");
        Path destination = this.temporaryDirectory.resolve("output.mkv");
        TestHandler handler = new TestHandler(source, destination);

        MediaConversionOutput output = handler.handle(input(MediaConversionHandler.sha256(source)));

        assertTrue(handler.probed);
        assertTrue(handler.outputProbed);
        assertTrue(handler.pushed);
        assertTrue(handler.cleaned);
        assertEquals(destination.toAbsolutePath(), handler.cleanedDestination);
        assertTrue(output.tracks().isEmpty());
    }

    private static MediaConversionInput input(String hash) {

        return new MediaConversionInput(
                new MediaConversionInput.Episode(
                        UUID.randomUUID(),
                        new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "source.mkv"),
                        hash
                ),
                new MediaConversionInput.ConversionOptions(Codec.AAC, Codec.H264, Codec.SUBTITLE_COPY)
        );
    }

    private static MediaStream stream(int id, Codec sourceCodec, EnumSet<Disposition> dispositions) {

        MediaStream stream = mock(MediaStream.class);
        when(stream.getId()).thenReturn(id);
        when(stream.getCodec()).thenReturn(sourceCodec);
        when(stream.getDispositions()).thenReturn(dispositions);
        when(stream.getMetadata()).thenReturn(Map.of("title", "Track", "language", "eng"));
        return stream;
    }

    private static final class TestHandler extends MediaConversionHandler {

        private final Path source;
        private final Path destination;
        private boolean probed;
        private boolean outputProbed;
        private boolean pushed;
        private boolean cleaned;
        private Path cleanedSource;
        private Path cleanedDestination;

        private TestHandler(Path source, Path destination) {

            this.source = source;
            this.destination = destination;
        }

        @Override
        public Path fetchEpisode(MediaConversionInput.Episode episode) {

            return this.source;
        }

        @Override
        public Path getStoragePath(MediaConversionInput.Episode episode, Path source) {

            return this.destination;
        }

        @Override
        MediaFile probe(Path path) {

            if (path.equals(this.source)) {
                this.probed = true;
            } else if (path.equals(this.destination.toAbsolutePath())) {
                this.outputProbed = true;
            }
            return mock(MediaFile.class);
        }

        @Override
        Path convert(MediaFile media, MediaConversionInput.ConversionOptions options, TrackMapper mapper, Path destination) throws IOException {

            Files.writeString(destination, "converted");
            return destination;
        }

        @Override
        public void pushEpisode(MediaConversionInput.Episode episode, Path path) {

            this.pushed = true;
        }

        @Override
        public void cleanupEpisode(MediaConversionInput.Episode episode, Path source, Path destination) {

            this.cleaned = true;
            this.cleanedSource = source;
            this.cleanedDestination = destination;
        }

    }

}
