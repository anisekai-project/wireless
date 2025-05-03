package fr.anisekai.wireless.api.json.validation;

import fr.anisekai.wireless.api.json.AnisekaiArray;
import fr.anisekai.wireless.api.json.AnisekaiJson;
import org.json.JSONException;

/**
 * Specific implementation of a {@link JsonRule} allowing to validate an {@link AnisekaiArray} containing only
 * {@link AnisekaiJson}.
 */
public class JsonArrayObjectRule extends Rule {

    private static final String RULE_FORMAT = "[[ array '%s' is %s and %s containing objects ]]";
    private static final String RULE_EX_MSG = "JSON rule %s failed at index %s: JSON Validation Failed.";

    private final boolean    allowEmpty;
    private final JsonRule[] rules;

    /**
     * Create a new {@link JsonArrayObjectRule} instance.
     *
     * @param key
     *         The key in the {@link AnisekaiJson} into which the {@link AnisekaiArray} is expected.
     * @param required
     *         Define if the {@link AnisekaiArray} must be defined.
     * @param allowEmpty
     *         Define if the {@link AnisekaiArray} can be empty.
     * @param rules
     *         Array of {@link JsonRule} that will be used to validate subsequent {@link AnisekaiJson} contained in the
     *         {@link AnisekaiArray}.
     */
    public JsonArrayObjectRule(String key, boolean required, boolean allowEmpty, JsonRule[] rules) {

        super(key, required);
        this.allowEmpty = allowEmpty;
        this.rules      = rules;
    }

    @Override
    public void validate(AnisekaiJson json) {

        this.retrieve(json).ifPresent(obj -> {
            AnisekaiArray array = this.asArray(obj, this.allowEmpty);

            for (int i = 0; i < array.size(); i++) {
                AnisekaiJson subJson = this.asObject(array.get(i), i);

                try {
                    subJson.validate(this.rules);
                } catch (JSONException e) {
                    throw new JSONException(String.format(RULE_EX_MSG, this, i), e);
                }
            }
        });
    }

    @Override
    public String toString() {

        String requireState = this.isRequired() ? "required" : "not required";
        String emptyState   = this.allowEmpty ? "allow empty" : "disallow empty";

        return String.format(RULE_FORMAT, this.getKey(), requireState, emptyState);
    }

}
