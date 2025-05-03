package fr.anisekai.wireless.media;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaMeta;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.FFMpeg;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.List;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Media (ffmpeg)")
@Tags({@Tag("slow-test"), @Tag("ffmpeg")})
public class MediaTests {

    public static final String TEST_DATA_DIR  = "test-data";
    public static final String TEST_DATA_FILE = "video.mkv";

    public static final String TEST_DATA_RES_VIDEO   = "0.mp4";
    public static final String TEST_DATA_RES_AUDIO_1 = "1.ogg";
    public static final String TEST_DATA_RES_AUDIO_2 = "2.ogg";
    public static final String TEST_DATA_RES_SUBS_1  = "3.ass";
    public static final String TEST_DATA_RES_SUBS_2  = "4.ass";

    private static File getTestFile(String name, boolean require) {

        File dataDir = new File(TEST_DATA_DIR);
        if (!dataDir.exists()) throw new RuntimeException("Unable to locate test data directory");
        File file = new File(dataDir, name);
        if (!file.exists() && require) throw new RuntimeException("Unable to locate test data file " + name);
        return file;
    }

    @Order(1)
    @Test
    @DisplayName("ffprobe | Read MKV Data")
    public void testProbeFile() {

        File      target = getTestFile(TEST_DATA_FILE, true);
        MediaFile media  = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));

        Assertions.assertEquals(5, media.getStreams().size(), "Media stream count mismatch");
        Assertions.assertEquals(1, media.getStreams(CodecType.VIDEO).size(), "Video stream count mismatch");
        Assertions.assertEquals(2, media.getStreams(CodecType.AUDIO).size(), "Audio stream count mismatch");
        Assertions.assertEquals(2, media.getStreams(CodecType.SUBTITLE).size(), "Audio stream count mismatch");
        Assertions.assertDoesNotThrow(() -> MediaFile.of(target));
    }

    @Order(10)
    @Test
    @DisplayName("ffmpeg | MKV to split converted files")
    public void testExplode() {

        File       target = getTestFile(TEST_DATA_FILE, true);
        MediaFile  media  = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));
        List<File> files  = Assertions.assertDoesNotThrow(() -> FFMpeg.explode(media, Codec.H264, Codec.VORBIS));

        Assertions.assertEquals(5, files.size(), "File count mismatch");
        Assertions.assertTrue(files.get(0).exists(), "File (0) does not exist");
        Assertions.assertTrue(files.get(1).exists(), "File (1) does not exist");
        Assertions.assertTrue(files.get(2).exists(), "File (2) does not exist");
        Assertions.assertTrue(files.get(3).exists(), "File (3) does not exist");
        Assertions.assertTrue(files.get(4).exists(), "File (4) does not exist");

        for (MediaStream stream : media.getStreams()) {
            File file = switch (stream.codec().getType()) {
                case VIDEO -> stream.asFile(target.getParentFile(), Codec.H264);
                case AUDIO -> stream.asFile(target.getParentFile(), Codec.VORBIS);
                case SUBTITLE -> stream.asFile(target.getParentFile());
            };

            Assertions.assertTrue(
                    file.exists(),
                    "File for stream %s does not exist (%s)".formatted(stream.index(), file.getAbsolutePath())
            );
        }
    }

    @Order(20)
    @Test
    @DisplayName("ffmpeg | Split files to MKV")
    public void testCombine() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, true);
        File audio1 = getTestFile(TEST_DATA_RES_AUDIO_1, true);
        File audio2 = getTestFile(TEST_DATA_RES_AUDIO_2, true);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, true);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, true);

        MediaMeta videoMeta  = new MediaMeta(video, null, null);
        MediaMeta audio1Meta = new MediaMeta(audio1, "Français", "fre");
        MediaMeta audio2Meta = new MediaMeta(audio2, "日本語", "jpn");
        MediaMeta subs1Meta  = new MediaMeta(subs1, "Français (Forced)", "fre");
        MediaMeta subs2Meta  = new MediaMeta(subs2, "Français", "fre");

        File output = Assertions.assertDoesNotThrow(() -> FFMpeg.combine(
                videoMeta,
                audio2Meta,
                audio1Meta,
                subs2Meta,
                subs1Meta
        ));
        MediaFile media = Assertions.assertDoesNotThrow(() -> MediaFile.of(output));

        Assertions.assertEquals(5, media.getStreams().size(), "Media stream count mismatch");
        Assertions.assertEquals(1, media.getStreams(CodecType.VIDEO).size(), "Video stream count mismatch");
        Assertions.assertEquals(2, media.getStreams(CodecType.AUDIO).size(), "Audio stream count mismatch");
        Assertions.assertEquals(2, media.getStreams(CodecType.SUBTITLE).size(), "Audio stream count mismatch");
    }

    @AfterAll
    public static void onTestFinished() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, false);
        File audio1 = getTestFile(TEST_DATA_RES_AUDIO_1, false);
        File audio2 = getTestFile(TEST_DATA_RES_AUDIO_2, false);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, false);

        if (video.exists()) video.delete();
        if (audio1.exists()) audio1.delete();
        if (audio2.exists()) audio2.delete();
        if (subs1.exists()) subs1.delete();
        if (subs2.exists()) subs2.delete();
    }

}
