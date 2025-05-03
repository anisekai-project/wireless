package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing a single setting entry
 */
public interface SettingEntity extends Entity<String> {

    /**
     * Retrieve this {@link SettingEntity}'s value. Every setting is stored as a string, and it is the responsibility of the
     * caller to check and convert the content.
     *
     * @return A string value
     */
    @Nullable String getValue();

    /**
     * Define this {@link SettingEntity}'s value. Every setting is stored as a string, and it is the responsibility of the caller
     * to check and convert the content.
     *
     * @param value
     *         A string value
     */
    void setValue(@Nullable String value);

}
