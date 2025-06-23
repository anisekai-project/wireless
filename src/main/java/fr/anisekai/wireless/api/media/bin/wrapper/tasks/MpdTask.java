package fr.anisekai.wireless.api.media.bin.wrapper.tasks;

import fr.anisekai.wireless.api.media.MediaFile;
import fr.anisekai.wireless.api.media.MediaStream;
import fr.anisekai.wireless.api.media.bin.Binary;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommand;
import fr.anisekai.wireless.api.media.bin.wrapper.FFMpegCommandTask;
import fr.anisekai.wireless.utils.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Specific implementation {@link FFMpegCommand} allowing to convert a {@link MediaFile} into chunks with a DASH meta file.
 */
public class MpdTask extends FFMpegCommandTask<Path> {

    private final MediaFile input;
    private final Path      output;
    private final Path      mdp;

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
        this.input  = input;
        this.output = output;
        this.mdp    = mdp;
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
                    adaptationSets.add("id=%s,streams=%s".formatted(id, stream.getId()));
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

        return this.mdp;
    }

}
