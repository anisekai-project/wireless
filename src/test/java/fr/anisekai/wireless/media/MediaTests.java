package fr.anisekai.wireless.media;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaMeta;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.FFMpeg;
import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.CodecType;
import org.junit.jupiter.api.*;

import java.io.File;
import java.util.Map;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Media (ffmpeg)")
@Tags({@Tag("slow-test"), @Tag("ffmpeg")})
public class MediaTests {

    public static final String TEST_DATA_DIR  = "test-data";
    public static final String TEST_DATA_MPD  = "data";
    public static final String TEST_DATA_FILE = "video.mkv";

    public static final String TEST_DATA_RES_VIDEO   = "0.mp4";
    public static final String TEST_DATA_RES_AUDIO_1 = "1.ogg";
    public static final String TEST_DATA_RES_AUDIO_2 = "2.ogg";
    public static final String TEST_DATA_EXT_AUDIO_1 = "1.m4a";
    public static final String TEST_DATA_EXT_AUDIO_2 = "2.m4a";
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
    }

    @Order(2)
    @Test
    @DisplayName("ffmpeg | Codec passthrough")
    public void testPassthrough() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, false);
        File audio1 = getTestFile(TEST_DATA_EXT_AUDIO_1, false);
        File audio2 = getTestFile(TEST_DATA_EXT_AUDIO_2, false);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, false);

        File      target = getTestFile(TEST_DATA_FILE, true);
        MediaFile media  = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));

        Map<MediaStream, File> files = Assertions.assertDoesNotThrow(() -> FFMpeg.explode(
                media,
                Codec.VIDEO_COPY,
                Codec.AUDIO_COPY,
                Codec.SUBTITLES_COPY,
                1
        ));

        Assertions.assertEquals(5, files.size(), "File count mismatch");

        Assertions.assertTrue(video.exists(), "Video file mismatch");
        Assertions.assertTrue(audio1.exists(), "Audio(1) file mismatch");
        Assertions.assertTrue(audio2.exists(), "Audio(2) file mismatch");
        Assertions.assertTrue(subs1.exists(), "Subs(1) file mismatch");
        Assertions.assertTrue(subs2.exists(), "Subs(2) file mismatch");
    }

    @Order(3)
    @Test
    @DisplayName("ffmpeg | Create MPD")
    public void testMPD() {

        File target    = getTestFile(TEST_DATA_FILE, true);
        File outputDir = getTestFile(TEST_DATA_MPD, true);
        File mpd       = new File(outputDir, "meta.mpd");

        MediaFile media = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));

        File file = Assertions.assertDoesNotThrow(() -> FFMpeg.createMpd(
                media,
                outputDir
        ));

        Assertions.assertTrue(file.exists(), "MPD metadata file does not exist");
        Assertions.assertTrue(mpd.exists(), "MPD metadata not at the right output");
    }

    @Order(4)
    @Test
    @DisplayName("ffmpeg | MKV to split converted files")
    public void testExplode() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, false);
        File audio1 = getTestFile(TEST_DATA_RES_AUDIO_1, false);
        File audio2 = getTestFile(TEST_DATA_RES_AUDIO_2, false);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, false);

        File      target = getTestFile(TEST_DATA_FILE, true);
        MediaFile media  = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));
        Map<MediaStream, File> files = Assertions.assertDoesNotThrow(() -> FFMpeg.explode(
                media,
                Codec.H264,
                Codec.VORBIS,
                Codec.SUBTITLES_COPY,
                1
        ));

        Assertions.assertEquals(5, files.size(), "File count mismatch");
        Assertions.assertTrue(video.exists(), "Video file mismatch");
        Assertions.assertTrue(audio1.exists(), "Audio(1) file mismatch");
        Assertions.assertTrue(audio2.exists(), "Audio(2) file mismatch");
        Assertions.assertTrue(subs1.exists(), "Subs(1) file mismatch");
        Assertions.assertTrue(subs2.exists(), "Subs(2) file mismatch");
    }

    @Order(5)
    @Test
    @DisplayName("ffmpeg | Split files to MKV")
    public void testCombine() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, false);
        File audio1 = getTestFile(TEST_DATA_RES_AUDIO_1, false);
        File audio2 = getTestFile(TEST_DATA_RES_AUDIO_2, false);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, false);

        MediaMeta videoMeta  = new MediaMeta(video, CodecType.VIDEO, null, null);
        MediaMeta audio1Meta = new MediaMeta(audio1, CodecType.AUDIO, "Français", "fre");
        MediaMeta audio2Meta = new MediaMeta(audio2, CodecType.AUDIO, "日本語", "jpn");
        MediaMeta subs1Meta  = new MediaMeta(subs1, CodecType.SUBTITLE, "Français (Forced)", "fre");
        MediaMeta subs2Meta  = new MediaMeta(subs2, CodecType.SUBTITLE, "Français", "fre");

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

    @Order(6)
    @Test
    @DisplayName("ffmpeg | Codec passthrough + ignore")
    public void testPassthroughWithIgnore() {

        File video  = getTestFile(TEST_DATA_RES_VIDEO, false);
        File audio1 = getTestFile(TEST_DATA_EXT_AUDIO_1, false);
        File audio2 = getTestFile(TEST_DATA_EXT_AUDIO_2, false);
        File subs1  = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2  = getTestFile(TEST_DATA_RES_SUBS_2, false);

        File      target = getTestFile(TEST_DATA_FILE, true);
        MediaFile media  = Assertions.assertDoesNotThrow(() -> MediaFile.of(target));

        Map<MediaStream, File> files = Assertions.assertDoesNotThrow(() -> FFMpeg.explode(
                media,
                null,
                Codec.AUDIO_COPY,
                null,
                1
        ));

        Assertions.assertEquals(2, files.size(), "File count mismatch");

        Assertions.assertFalse(video.exists(), "Video file mismatch");
        Assertions.assertTrue(audio1.exists(), "Audio(1) file mismatch");
        Assertions.assertTrue(audio2.exists(), "Audio(2) file mismatch");
        Assertions.assertFalse(subs1.exists(), "Subs(1) file mismatch");
        Assertions.assertFalse(subs2.exists(), "Subs(2) file mismatch");
    }

    @AfterAll
    public static void onTestFinished() {

        File video     = getTestFile(TEST_DATA_RES_VIDEO, false);
        File resAudio1 = getTestFile(TEST_DATA_RES_AUDIO_1, false);
        File resAudio2 = getTestFile(TEST_DATA_RES_AUDIO_2, false);
        File extAudio1 = getTestFile(TEST_DATA_EXT_AUDIO_1, false);
        File extAudio2 = getTestFile(TEST_DATA_EXT_AUDIO_2, false);
        File subs1     = getTestFile(TEST_DATA_RES_SUBS_1, false);
        File subs2     = getTestFile(TEST_DATA_RES_SUBS_2, false);
        File mpd       = getTestFile(TEST_DATA_MPD, false);

        if (video.exists()) video.delete();
        if (resAudio1.exists()) resAudio1.delete();
        if (resAudio2.exists()) resAudio2.delete();
        if (extAudio1.exists()) extAudio1.delete();
        if (extAudio2.exists()) extAudio2.delete();
        if (subs1.exists()) subs1.delete();
        if (subs2.exists()) subs2.delete();

        if (mpd.exists()) {
            for (File file : mpd.listFiles()) {
                file.delete();
            }
        }
    }

}
