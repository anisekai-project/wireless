package fr.anisekai.wireless.api.json.validation;

import fr.anisekai.wireless.api.json.AnisekaiJson;
import org.json.JSONException;

/**
 * Interface representing a validation rule for an {@link AnisekaiJson}.
 */
public interface JsonRule {

    /**
     * Validate the provided object against this {@link JsonRule}.
     *
     * @param json
     *         The object to validate.
     *
     * @throws JSONException
     *         Thrown if the current {@link JsonRule} failed.
     */
    void validate(AnisekaiJson json);

    /**
     * Get the key that should be used when checking an {@link AnisekaiJson}.
     *
     * @return A key
     */
    String getKey();

    /**
     * Check if the key of this rule must exist.
     *
     * @return True for enforce the check, false otherwise.
     */
    boolean isRequired();

}
