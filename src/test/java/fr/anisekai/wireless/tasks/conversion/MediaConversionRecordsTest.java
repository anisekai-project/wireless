package fr.anisekai.wireless.tasks.conversion;

import fr.anisekai.media.enums.Codec;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaConversionRecordsTest {

    private static final String HASH = "A".repeat(64);

    @Test
    void validatesAndNormalizesEpisodeSourceData() {

        UUID id = UUID.randomUUID();
        MediaConversionInput.Episode episode = new MediaConversionInput.Episode(
                id,
                new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "batch/episode.mkv"),
                HASH
        );

        assertEquals(id, episode.id());
        assertEquals(MediaConversionInput.Store.IMPORTS, episode.source().store());
        assertEquals("batch/episode.mkv", episode.source().reference());
        assertEquals(HASH.toLowerCase(), episode.hash());
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Episode(
                        id,
                        new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "../episode.mkv"),
                        HASH
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Episode(
                        id,
                        new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "/episode.mkv"),
                        HASH
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Episode(
                        id,
                        new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "a/b/c.mkv"),
                        HASH
                )
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Episode(
                        id,
                        new MediaConversionInput.Source(MediaConversionInput.Store.IMPORTS, "episode.mkv"),
                        "not-a-hash"
                )
        );
    }

    @Test
    void validatesDownloadSourceReferences() {

        UUID torrentId = UUID.randomUUID();
        MediaConversionInput.Source source = new MediaConversionInput.Source(
                MediaConversionInput.Store.DOWNLOADS,
                torrentId + "/3"
        );

        assertEquals(MediaConversionInput.Store.DOWNLOADS, source.store());
        assertEquals(torrentId + "/3", source.reference());
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Source(MediaConversionInput.Store.DOWNLOADS, "episode.mkv")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Source(MediaConversionInput.Store.DOWNLOADS, "not-a-uuid/3")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Source(MediaConversionInput.Store.DOWNLOADS, torrentId + "/-1")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.Source(MediaConversionInput.Store.DOWNLOADS, torrentId + "/a/b")
        );
    }

    @Test
    void validatesConversionCodecTypes() {

        assertThrows(
                IllegalArgumentException.class,
                () -> new MediaConversionInput.ConversionOptions(Codec.H264, Codec.H264, Codec.SUBTITLE_COPY)
        );
    }

    @Test
    void outputDefensivelyCopiesTracks() {

        List<MediaConversionOutput.Track> tracks = new ArrayList<>();
        tracks.add(new MediaConversionOutput.Track(UUID.randomUUID(), "Video", Codec.H264, null, 0));

        MediaConversionOutput output = new MediaConversionOutput(tracks);
        tracks.clear();

        assertEquals(1, output.tracks().size());
        assertThrows(UnsupportedOperationException.class, () -> output.tracks().clear());
    }

}
