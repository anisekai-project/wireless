package fr.anisekai.wireless.api.json;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class adding few features on top of {@link JSONArray}.
 */
public class AnisekaiArray extends JSONArray {

    /**
     * Create an empty {@link AnisekaiArray} instance.
     */
    public AnisekaiArray() {

        super();
    }

    /**
     * Create a new {@link AnisekaiArray} instance from the provided string.
     *
     * @param source
     *         A string representation of a {@link JSONArray}.
     *
     * @throws JSONException
     *         If there is a syntax error.
     */
    public AnisekaiArray(String source) throws JSONException {

        super(source);
    }

    /**
     * Create a new {@link AnisekaiArray} instance from an existing {@link JSONArray} instance.
     *
     * @param array
     *         The {@link JSONArray} to transform into {@link AnisekaiArray}.
     */
    public AnisekaiArray(JSONArray array) {

        super(array.toString());
    }

    /**
     * Retrieve the {@link AnisekaiJson} instance at the provided index in this {@link AnisekaiArray}.
     *
     * @param index
     *         The index in {@link AnisekaiArray} from which the {@link AnisekaiJson} should be retrieved.
     *
     * @return A new {@link AnisekaiJson} instance.
     */
    public AnisekaiJson getAnisekaiJson(int index) {

        return new AnisekaiJson(this.getJSONObject(index));
    }

    /**
     * Retrieve the {@link AnisekaiArray} instance at the provided index in this {@link AnisekaiArray}.
     *
     * @param index
     *         The index in {@link AnisekaiArray} from which the {@link AnisekaiArray} should be retrieved.
     *
     * @return A new {@link AnisekaiArray} instance.
     */
    public AnisekaiArray getAnisekaiArray(int index) {

        return new AnisekaiArray(this.getJSONArray(index));
    }

    /**
     * Loop through this {@link AnisekaiArray} for each {@link AnisekaiJson}.
     *
     * @param action
     *         A {@link Consumer} accepting a {@link AnisekaiJson} instance.
     */
    public void forEachJson(Consumer<AnisekaiJson> action) {

        for (int i = 0; i < this.length(); i++) {
            action.accept(this.getAnisekaiJson(i));
        }
    }

    /**
     * Loop through this {@link AnisekaiArray} for each {@link AnisekaiArray}.
     *
     * @param action
     *         A {@link Consumer} accepting a {@link AnisekaiArray} instance.
     */
    public void forEachArray(Consumer<AnisekaiArray> action) {

        for (int i = 0; i < this.length(); i++) {
            action.accept(this.getAnisekaiArray(i));
        }
    }

    /**
     * Alias method for {@link JSONArray#length()}. Merely used for the IntelliJ IDEA PostFix completion `.fori`.
     *
     * @return The total element count in this {@link AnisekaiArray}.
     */
    public int size() {

        return this.length();
    }

    /**
     * Map this {@link AnisekaiArray}'s elements to a {@link List} of {@link T} using the provided {@link Function}
     *
     * @param mappingFunction
     *         The {@link Function} converting an {@link AnisekaiJson} to {@link T}
     * @param <T>
     *         Type of the output.
     *
     * @return A {@link List} of {@link T}.
     */
    public <T> List<T> map(Function<AnisekaiJson, T> mappingFunction) {

        List<T> result = new ArrayList<>();
        this.forEachJson(json -> result.add(mappingFunction.apply(json)));
        return result;
    }

}
