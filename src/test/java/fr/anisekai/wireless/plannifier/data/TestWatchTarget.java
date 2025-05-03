package fr.anisekai.wireless.plannifier.data;

import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;

import java.util.Objects;

public class TestWatchTarget implements WatchTarget {

    private final long id;
    private       long watched;
    private       long total;
    private       long episodeDuration = TestData.EPISODE_DURATION_MINUTES;

    public TestWatchTarget(long id, long watched, long total) {

        this.id      = id;
        this.watched = watched;
        this.total   = total;
    }

    @Override
    public long getWatched() {

        return this.watched;
    }

    @Override
    public void setWatched(long watched) {

        this.watched = watched;
    }

    @Override
    public long getTotal() {

        return this.total;
    }

    @Override
    public void setTotal(long total) {

        this.total = total;
    }

    @Override
    public long getEpisodeDuration() {

        return this.episodeDuration;
    }

    @Override
    public void setEpisodeDuration(long episodeDuration) {

        this.episodeDuration = episodeDuration;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || this.getClass() != o.getClass()) return false;
        TestWatchTarget that = (TestWatchTarget) o;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {

        return Objects.hashCode(this.id);
    }

    @Override
    public String toString() {

        return "TestWatchTarget{" +
                "id=" + this.id +
                ", episodeWatched=" + this.watched +
                ", episodeCount=" + this.total +
                ", episodeDuration=" + this.episodeDuration +
                '}';
    }

}
