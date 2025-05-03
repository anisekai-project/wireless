package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.enums.AnimeSeason;
import fr.anisekai.wireless.remote.enums.SelectionStatus;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Interface representing the base structure for a selection.
 *
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface SelectionEntity<A extends AnimeEntity<?>> extends Entity<Long> {

    /**
     * Retrieve this {@link SelectionEntity}'s season.
     *
     * @return An {@link AnimeSeason}.
     */
    @NotNull AnimeSeason getSeason();

    /**
     * Define this {@link SelectionEntity}'s season.
     *
     * @param season
     *         An {@link AnimeSeason}.
     */
    void setSeason(@NotNull AnimeSeason season);

    /**
     * Retrieve this {@link SelectionEntity}'s year.
     *
     * @return A year
     */
    int getYear();

    /**
     * Define this {@link SelectionEntity}'s year
     *
     * @param year
     *         A year
     */
    void setYear(int year);

    /**
     * Retrieve this {@link SelectionEntity}'s {@link SelectionStatus}.
     *
     * @return A {@link SelectionStatus}.
     */
    @NotNull SelectionStatus getStatus();

    /**
     * Define this {@link SelectionEntity}'s {@link SelectionStatus}.
     *
     * @param status
     *         A {@link SelectionStatus}.
     */
    void setStatus(@NotNull SelectionStatus status);

    /**
     * Retrieve this {@link SelectionEntity}'s votable {@link AnimeEntity}.
     *
     * @return A {@link Set} of {@link AnimeEntity}
     */
    @NotNull Set<A> getAnimes();

    /**
     * Define this {@link SelectionEntity}'s votable {@link AnimeEntity}.
     *
     * @param animes
     *         A {@link Set} of {@link AnimeEntity}.
     */
    void setAnimes(@NotNull Set<A> animes);

}
