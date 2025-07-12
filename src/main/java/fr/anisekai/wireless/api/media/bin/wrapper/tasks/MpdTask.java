package fr.anisekai.wireless.api.media.bin.wrapper.tasks;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.Binary;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommand;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommandTask;
import fr.anisekai.wireless.utils.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Specific implementation {@link FFMpegCommand} allowing to convert a {@link MediaFile} into chunks with a DASH meta file.
 */
public class MpdTask extends FFMpegCommandTask<Path> {

    private final MediaFile         input;
    private final Path              output;
    private final Path              mdp;
    private final List<MediaStream> adaptionSetsMedia;

    /**
     * Create a new {@link MpdTask} instance.
     *
     * @param input
     *         The {@link MediaFile} to convert.
     * @param output
     *         The {@link Path} to use as output directory
     * @param mdp
     *         The {@link Path} to use for the meta file.
     */
    public MpdTask(MediaFile input, Path output, Path mdp) {

        super(Binary.ffmpeg());
        this.input             = input;
        this.output            = output;
        this.mdp               = mdp;
        this.adaptionSetsMedia = new ArrayList<>();
    }

    @Override
    public void preprocess(Binary ffmpeg) throws IOException {

        ffmpeg.setBaseDir(this.output);
        ffmpeg.addArguments("-i", this.input.getPath().toString());

        Collection<String> adaptationSets = new ArrayList<>();

        int id = 0;
        for (MediaStream stream : this.input.getStreams()) {
            switch (stream.getCodec().getType()) {
                case VIDEO, AUDIO:
                    ffmpeg.addArguments("-map", "0:%s".formatted(stream.getId()));
                    adaptationSets.add("id=%s,streams=%s".formatted(id, id));
                    this.adaptionSetsMedia.add(stream);
                    id++;
                    break;
            }
        }

        ffmpeg.addArguments("-c", "copy");
        ffmpeg.addArguments("-adaptation_sets", String.join(" ", adaptationSets));
        ffmpeg.addArguments(this.mdp.toString());

        // Ensure output is empty or ffmpeg is going to make a whim
        FileUtils.delete(this.output);
        FileUtils.ensureDirectory(this.output);
    }

    @Override
    public Path postprocess(int code) throws IOException {

        Pattern pattern = Pattern.compile("<AdaptationSet id=\"(?<idx>\\d+)\"");

        Path temp = Files.createTempFile("patched-", ".mpd");

        try (
                BufferedReader reader = Files.newBufferedReader(this.mdp);
                BufferedWriter writer = Files.newBufferedWriter(temp, StandardOpenOption.TRUNCATE_EXISTING)
        ) {
            String line;

            //noinspection NestedAssignment
            while ((line = reader.readLine()) != null) {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find()) {
                    int idx = Integer.parseInt(matcher.group("idx"));

                    if (idx >= 0 && idx < this.adaptionSetsMedia.size()) {
                        MediaStream stream = this.adaptionSetsMedia.get(idx);

                        if (stream.getMetadata().containsKey("title")) {
                            String label = stream.getMetadata().get("title")
                                                 .replace("&", "&amp;")
                                                 .replace("\"", "&quot;")
                                                 .replace("<", "&lt;")
                                                 .replace(">", "&gt;");

                            line = line.replaceFirst(
                                    "id=\"%d\"".formatted(idx),
                                    "id=\"%d\" label=\"%s\"".formatted(idx, label)
                            );
                        }
                    }
                }

                writer.write(line);
                writer.newLine();
            }
        }

        // Atomically replace original MPD
        Files.move(temp, this.mdp, StandardCopyOption.REPLACE_EXISTING);

        return this.mdp;
    }

}
