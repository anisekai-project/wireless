package fr.anisekai.wireless.api.json.validation;

import fr.anisekai.wireless.api.json.AnisekaiArray;
import fr.anisekai.wireless.api.json.AnisekaiJson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

/**
 * Abstract base implementation of a {@link JsonRule} representing a validation rule applied to a specific key within a JSON
 * structure.
 * <p>
 * Each instance targets a particular JSON key and defines whether its presence is mandatory. Subclasses are expected to implement
 * the rule's specific logic, leveraging helper methods like {@link #retrieve(AnisekaiJson)}, {@link #asArray(Object, boolean)},
 * and {@link #asObject(Object, int)} for extraction and type validation.
 * <p>
 * If a required key is missing or its value is of an incompatible type, meaningful exceptions are thrown to aid in debugging.
 */
public abstract class Rule implements JsonRule {

    private final String  key;
    private final boolean required;

    /**
     * Create a new {@link Rule} targeting the specified JSON key with a required flag.
     *
     * @param key
     *         The key in the JSON object this rule applies to.
     * @param required
     *         Whether the key is mandatory. If {@code true}, a missing or {@code null} value will cause validation to fail.
     */
    public Rule(String key, boolean required) {

        this.key      = key;
        this.required = required;
    }

    protected Optional<Object> retrieve(AnisekaiJson source) {

        Optional<Object> optional = source.getOptional(this.getKey());

        if (this.isRequired() && optional.isEmpty()) {
            throw new IllegalArgumentException(String.format(
                    "JSON rule %s failed: The key was required but not found or null.",
                    this
            ));
        }

        return optional;
    }

    protected AnisekaiArray asArray(Object o, boolean allowEmpty) {

        AnisekaiArray array = switch (o) {
            case AnisekaiArray arr -> arr;
            case JSONArray arr -> new AnisekaiArray(arr);
            default -> throw new JSONException(String.format(
                    "JSON rule %s failed: Incompatible type '%s'.", this, o.getClass()
                                                                           .getSimpleName()
            ));
        };

        if (array.isEmpty() && !allowEmpty) {
            throw new JSONException(String.format("JSON rule %s failed: Empty array.", this));
        }

        return array;
    }

    protected AnisekaiJson asObject(Object o, int index) {

        return switch (o) {
            case AnisekaiJson obj -> obj;
            case JSONObject obj -> new AnisekaiJson(obj);
            default -> throw new JSONException(String.format("JSON rule %s failed: Not an object at index %s.", this, index));
        };
    }

    @Override
    public String getKey() {

        return this.key;
    }

    @Override
    public boolean isRequired() {

        return this.required;
    }

}
