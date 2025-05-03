package fr.anisekai.wireless.api.plannifier.data;

import fr.anisekai.wireless.api.plannifier.interfaces.ScheduleSpotData;
import fr.anisekai.wireless.api.plannifier.interfaces.SchedulerManager;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

/**
 * Class representing a {@link Planifiable} that has not been commited to a {@link SchedulerManager} yet.
 *
 * @param <T>
 *         Type of the {@link WatchTarget}.
 */
public class BookedPlanifiable<T extends WatchTarget> implements Planifiable<T> {

    private final T             target;
    private       ZonedDateTime startingAt;
    private       long          firstEpisode;
    private       long          episodeCount;
    private       boolean       skipEnabled;

    /**
     * Create a {@link BookedPlanifiable} based on the provided {@link ScheduleSpotData}.
     *
     * @param scheduleSpotData
     *         A {@link ScheduleSpotData}.
     */
    public BookedPlanifiable(ScheduleSpotData<T> scheduleSpotData) {

        this(scheduleSpotData, scheduleSpotData.getWatchTarget().getWatched() + 1);
    }

    /**
     * Create a {@link BookedPlanifiable} based on the provided {@link ScheduleSpotData} and first episode.
     *
     * @param scheduleSpotData
     *         A {@link ScheduleSpotData}.
     * @param firstEpisode
     *         The first episode number that would be broadcasted by this {@link BookedPlanifiable} once commited to the
     *         {@link SchedulerManager}.
     */
    public BookedPlanifiable(ScheduleSpotData<T> scheduleSpotData, long firstEpisode) {

        this.target       = scheduleSpotData.getWatchTarget();
        this.startingAt   = scheduleSpotData.getStartingAt();
        this.firstEpisode = firstEpisode;
        this.episodeCount = scheduleSpotData.getEpisodeCount();
        this.skipEnabled  = scheduleSpotData.isSkipEnabled();
    }

    @Override
    public long getFirstEpisode() {

        return this.firstEpisode;
    }

    @Override
    public void setFirstEpisode(long firstEpisode) {

        this.firstEpisode = firstEpisode;
    }

    @Override
    public @NotNull T getWatchTarget() {

        return this.target;
    }

    @Override
    public void setWatchTarget(@NotNull T watchTarget) {

        throw new UnsupportedOperationException("You cannot change the WatchTarget of a BookedPlanifiable");
    }

    @Override
    public @NotNull ZonedDateTime getStartingAt() {

        return this.startingAt;
    }

    @Override
    public void setStartingAt(@NotNull ZonedDateTime time) {

        this.startingAt = time;
    }

    @Override
    public long getEpisodeCount() {

        return this.episodeCount;
    }

    @Override
    public void setEpisodeCount(long episodeCount) {

        this.episodeCount = episodeCount;
    }

    @Override
    public boolean isSkipEnabled() {

        return this.skipEnabled;
    }

    @Override
    public void setSkipEnabled(boolean skipEnabled) {

        this.skipEnabled = skipEnabled;
    }

}
