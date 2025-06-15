package fr.anisekai.wireless.remote.enums;

import fr.anisekai.wireless.remote.interfaces.AnimeEntity;
import fr.anisekai.wireless.remote.interfaces.EpisodeEntity;
import fr.anisekai.wireless.remote.interfaces.WatchlistEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Enum representing to which {@link WatchlistEntity} an {@link AnimeEntity} belongs.
 */
public enum AnimeList {

    /**
     * The {@link AnimeEntity} has been completed.
     */
    WATCHED("✅"),

    /**
     * The {@link AnimeEntity} is being watched with all of its {@link EpisodeEntity} available.
     */
    WATCHING("👀", Property.PROGRESS, Property.WATCHABLE, Property.SHOW),

    /**
     * The {@link AnimeEntity} is being released on a weekly schedule.
     */
    SIMULCAST("\uD83D\uDD58", Property.PROGRESS, Property.WATCHABLE, Property.SHOW),

    /**
     * The {@link AnimeEntity} is being watched while being released on a weekly schedule.
     */
    SIMULCAST_AVAILABLE("✨", Property.WATCHABLE, Property.SHOW),

    /**
     * The {@link AnimeEntity} has all its {@link EpisodeEntity} downloaded and are available.
     */
    DOWNLOADED("\uD83D\uDCD7", Property.WATCHABLE),

    /**
     * The {@link AnimeEntity} has still some of its {@link EpisodeEntity} being processed. This can be used only if all episodes
     * are released officially.
     */
    DOWNLOADING("\uD83D\uDCD8"),

    /**
     * The {@link AnimeEntity} has been added to the database, but no {@link EpisodeEntity} is available yet.
     */
    NOT_DOWNLOADED("\uD83D\uDCD5"),

    /**
     * The {@link AnimeEntity} has been added to the database, but no source to download any {@link EpisodeEntity} has been
     * found.
     */
    NO_SOURCE("\uD83D\uDCD9"),

    /**
     * The {@link AnimeEntity} is not yet released.
     */
    UNAVAILABLE("\uD83D\uDD16", Property.SHOW),

    /**
     * The {@link AnimeEntity} has been dropped.
     */
    CANCELLED("\uD83D\uDED1");

    /**
     * Special properties used to describe each {@link AnimeList} options.
     */
    public enum Property {
        /**
         * Represents an {@link AnimeList} option that can trigger watch progression.
         */
        PROGRESS,
        /**
         * Represents an {@link AnimeList} option that contains {@link AnimeEntity} that can be watched.
         */
        WATCHABLE,
        /**
         * Represents an {@link AnimeList} option that can be displayed on Discord through a {@link WatchlistEntity}.
         */
        SHOW
    }

    private final String               icon;
    private final Collection<Property> properties;

    /**
     * Initialize a new {@link AnimeList} option with the provided icon and properties.
     *
     * @param icon
     *         The icon representing this {@link AnimeList}, most of the time an emoji.
     * @param properties
     *         An array of {@link Property} associated to this option.
     */
    AnimeList(String icon, Property... properties) {

        this.icon       = icon;
        this.properties = Arrays.asList(properties);
    }

    /**
     * Try to find a matching {@link AnimeList} from the provided {@link String}.
     *
     * @param value
     *         The {@link String} value to match.
     *
     * @return The matched {@link AnimeList}, or {@link AnimeList#UNAVAILABLE} if none matched.
     */
    public static AnimeList from(String value) {

        String upperValue = value.toUpperCase();
        try {
            return AnimeList.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            return AnimeList.UNAVAILABLE;
        }
    }

    /**
     * Check if this {@link AnimeList} contains the provided {@link Property}.
     *
     * @param property
     *         The {@link Property} to check for.
     *
     * @return True if the {@link Property} was found, false otherwise.
     */
    public boolean hasProperty(Property property) {

        return this.properties.contains(property);
    }

    /**
     * Retrieve this {@link AnimeList}'s icon.
     *
     * @return An emoji.
     */
    public String getIcon() {

        return this.icon;
    }

    /**
     * Retrieve all {@link AnimeList} having all provided {@link Property}.
     *
     * @param properties
     *         An array of {@link Property}.
     *
     * @return A {@link Collection} of {@link AnimeList}.
     */
    public static Collection<AnimeList> collect(Property... properties) {

        Collection<AnimeList> result              = new ArrayList<>();
        Collection<Property>  requestedProperties = Arrays.asList(properties);

        for (AnimeList value : values()) {
            if (value.properties.containsAll(requestedProperties)) {
                result.add(value);
            }
        }

        return result;
    }
}
