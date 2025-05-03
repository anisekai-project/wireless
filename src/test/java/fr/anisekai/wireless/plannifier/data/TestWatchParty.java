package fr.anisekai.wireless.plannifier.data;

import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.Objects;

public class TestWatchParty implements Planifiable<TestWatchTarget> {

    private final long            id;
    private final TestWatchTarget target;
    private       long            firstEpisode;
    private       ZonedDateTime   startingAt;
    private       long            episodeCount;
    private       boolean         skipEnabled;

    private boolean deleted = false;

    public TestWatchParty(long id, TestWatchTarget target, long firstEpisode, ZonedDateTime startingAt, long episodeCount, boolean skipEnabled) {

        this.id           = id;
        this.target       = target;
        this.firstEpisode = firstEpisode;
        this.startingAt   = startingAt;
        this.episodeCount = episodeCount;
        this.skipEnabled  = skipEnabled;
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

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TestWatchParty that = (TestWatchParty) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {

        return "TestWatchParty{" +
                "id=" + this.id +
                ", target=" + this.target +
                ", firstEpisode=" + this.firstEpisode +
                ", startingAt=" + this.startingAt +
                ", episodeCount=" + this.episodeCount +
                ", skipEnabled=" + this.skipEnabled +
                '}';
    }

    public boolean isDeleted() {

        return this.deleted;
    }

    public void tagDeleted() {

        this.deleted = true;
    }

}
