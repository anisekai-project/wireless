package fr.anisekai.wireless.plannifier.data;

import fr.anisekai.wireless.api.plannifier.interfaces.ScheduleSpotData;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class TestSpot implements ScheduleSpotData<TestWatchTarget> {

    private final TestWatchTarget target;
    private final ZonedDateTime   startingAt;
    private final long            episodeCount;
    private final boolean         skipEnabled;

    public TestSpot(TestWatchTarget target, ZonedDateTime startingAt, long episodeCount) {

        this.target       = target;
        this.startingAt   = startingAt;
        this.episodeCount = episodeCount;
        this.skipEnabled  = true;
    }

    @Override
    public @NotNull TestWatchTarget getWatchTarget() {

        return this.target;
    }

    @Override
    public void setWatchTarget(@NotNull TestWatchTarget watchTarget) {

    }

    @Override
    public @NotNull ZonedDateTime getStartingAt() {

        return this.startingAt;
    }

    @Override
    public void setStartingAt(@NotNull ZonedDateTime time) {

    }

    @Override
    public long getEpisodeCount() {

        return this.episodeCount;
    }

    @Override
    public void setEpisodeCount(long episodeCount) {

    }

    @Override
    public boolean isSkipEnabled() {

        return this.skipEnabled;
    }

    @Override
    public void setSkipEnabled(boolean skipEnabled) {

    }

}
