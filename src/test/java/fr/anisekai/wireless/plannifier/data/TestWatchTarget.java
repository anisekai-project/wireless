package fr.anisekai.wireless.plannifier.data;

import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;

import java.util.Objects;

public class TestWatchTarget implements WatchTarget {

    private final int id;
    private       int watched;
    private       int total;
    private       int episodeDuration = TestData.EPISODE_DURATION_MINUTES;

    public TestWatchTarget(int id, int watched, int total) {

        this.id      = id;
        this.watched = watched;
        this.total   = total;
    }

    @Override
    public int getWatched() {

        return this.watched;
    }

    @Override
    public void setWatched(int watched) {

        this.watched = watched;
    }

    @Override
    public int getTotal() {

        return this.total;
    }

    @Override
    public void setTotal(int total) {

        this.total = total;
    }

    @Override
    public int getEpisodeDuration() {

        return this.episodeDuration;
    }

    @Override
    public void setEpisodeDuration(int episodeDuration) {

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
