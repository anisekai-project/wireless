package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.services.Transmission;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing the base structure for a torrent.
 */
public interface TorrentEntity extends Entity<String> {

    /**
     * Retrieve this {@link TorrentEntity}'s name.
     *
     * @return A name.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    @NotNull String getName();

    /**
     * Define this {@link TorrentEntity}'s name.
     *
     * @param name
     *         A name.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setName(@NotNull String name);

    /**
     * Retrieve this {@link TorrentEntity}'s {@link Transmission.TorrentStatus}.
     *
     * @return A {@link Transmission.TorrentStatus}.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    @NotNull Transmission.TorrentStatus getStatus();

    /**
     * Define this {@link TorrentEntity}'s {@link Transmission.TorrentStatus}.
     *
     * @param status
     *         A {@link Transmission.TorrentStatus}.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setStatus(@NotNull Transmission.TorrentStatus status);

    /**
     * Retrieve this {@link TorrentEntity}'s download progress.
     *
     * @return A download progress.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    double getProgress();

    /**
     * Define this {@link TorrentEntity}'s download progress.
     *
     * @param progress
     *         A download progress.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setProgress(double progress);

    /**
     * Retrieve this {@link TorrentEntity}'s source link.
     *
     * @return A link
     */
    @NotNull String getLink();

    /**
     * Define this {@link TorrentEntity}'s source link.
     *
     * @param link
     *         A link.
     */
    void setLink(@NotNull String link);

    /**
     * Retrieve this {@link TorrentEntity}'s download directory.
     *
     * @return A download directory.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    @NotNull String getDownloadDirectory();

    /**
     * Define this {@link TorrentEntity}'s download directory.
     *
     * @param downloadDirectory
     *         A download directory.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setDownloadDirectory(@NotNull String downloadDirectory);

}
