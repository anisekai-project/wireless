package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import fr.anisekai.wireless.remote.keys.TorrentKey;
import fr.anisekai.wireless.remote.keys.VoterKey;
import org.jetbrains.annotations.NotNull;

/**
 * Interface representing the base structure for a torrent file.
 *
 * @param <T>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface TorrentFileEntity<T extends TorrentEntity> extends Entity<TorrentKey> {

    /**
     * Retrieve to which {@link TorrentEntity} this {@link TorrentFileEntity} belongs.
     *
     * @return A {@link TorrentEntity}.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    @NotNull T getTorrent();

    /**
     * Define to which {@link TorrentEntity} this {@link TorrentFileEntity} belongs.
     *
     * @param torrent
     *         A {@link TorrentEntity}
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setTorrent(@NotNull T torrent);

    /**
     * Retrieve this {@link TorrentFileEntity}'s index within the {@link TorrentEntity} content.
     *
     * @return An index.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    int getIndex();

    /**
     * Define this {@link TorrentFileEntity}'s index within the {@link TorrentEntity} content.
     *
     * @param index
     *         An index.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setIndex(int index);

    /**
     * Retrieve this {@link TorrentFileEntity}'s name.
     *
     * @return A name.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    @NotNull String getName();

    /**
     * Define this {@link TorrentFileEntity}'s name.
     *
     * @param name
     *         A name.
     */
    @ExternallyBoundBy(ExternalBindType.TRANSMISSION)
    void setName(@NotNull String name);


    /**
     * Retrieve this {@link Entity} primary key.
     *
     * @return The primary key.
     */
    @Override
    default TorrentKey getId() {

        return TorrentKey.create(this.getTorrent(), this.getIndex());
    }

}
