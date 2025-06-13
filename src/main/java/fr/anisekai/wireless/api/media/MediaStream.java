package fr.anisekai.wireless.api.media;

import fr.anisekai.wireless.api.media.enums.Codec;
import fr.anisekai.wireless.api.media.enums.Disposition;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single media stream extracted from a container file.
 */
public class MediaStream {

    private final int                 id;
    private final Codec               codec;
    private final List<Disposition>   dispositions;
    private final Map<String, String> metadata;

    /**
     * Create a new instance of {@link MediaStream}.
     *
     * @param codec
     *         The {@link Codec} for this {@link MediaStream}.
     * @param json
     *         The {@link JSONObject} containing a {@link MediaStream} data.
     */
    public MediaStream(Codec codec, JSONObject json) {

        this.id           = json.getInt("index");
        this.codec        = codec;
        this.dispositions = new ArrayList<>();
        this.metadata     = new HashMap<>();

        JSONObject dispositionJson = json.getJSONObject("disposition");

        for (String key : dispositionJson.keySet()) {
            Disposition disposition = Disposition.from(key);
            if (disposition != null) {
                this.dispositions.add(disposition);
            }
        }

        JSONObject tagsJson = json.getJSONObject("tags");

        for (String key : tagsJson.keySet()) {
            this.metadata.put(key, tagsJson.getString(key));
        }

    }

    /**
     * Retrieve this {@link MediaStream}'s ID
     *
     * @return An id
     */
    public int getId() {

        return this.id;
    }

    /**
     * Retrieve this {@link MediaStream}'s ID
     *
     * @return A {@link Codec}
     */
    public Codec getCodec() {

        return this.codec;
    }

    /**
     * Retrieve this {@link MediaStream}'s {@link List} of {@link Disposition}s.
     *
     * @return A {@link List} of {@link Disposition}s.
     */
    public List<Disposition> getDispositions() {

        return this.dispositions;
    }

    /**
     * Retrieve this {@link MediaStream} metadata.
     *
     * @return A {@link Map}.
     */
    public Map<String, String> getMetadata() {

        return this.metadata;
    }

}
