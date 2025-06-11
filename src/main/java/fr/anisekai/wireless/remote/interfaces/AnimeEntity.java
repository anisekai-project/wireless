package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;
import fr.anisekai.wireless.remote.enums.AnimeList;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Interface representing the base structure for an anime.
 *
 * @param <U>
 *         Type for the {@link UserEntity} implementation.
 */
public interface AnimeEntity<U extends UserEntity> extends Entity<Long>, WatchTarget {

    /**
     * Retrieve this {@link AnimeEntity}'s group.
     *
     * @return A group name
     */
    @NotNull String getGroup();

    /**
     * Define this {@link AnimeEntity}'s group.
     *
     * @param group
     *         A group name
     */
    void setGroup(@NotNull String group);

    /**
     * Retrieve this {@link AnimeEntity}'s watch order within its group.
     *
     * @return A group watch order.
     */
    byte getOrder();

    /**
     * Define this {@link AnimeEntity}'s watch order within its group.
     *
     * @param order
     *         A group watch order.
     */
    void setOrder(byte order);

    /**
     * Retrieve this {@link AnimeEntity}'s title.
     *
     * @return A title.
     */
    @NotNull String getTitle();

    /**
     * Define this {@link AnimeEntity}'s title.
     *
     * @param title
     *         A title.
     */
    void setTitle(@NotNull String title);

    /**
     * Retrieve this {@link AnimeEntity}'s {@link AnimeList}.
     *
     * @return An {@link AnimeList}.
     */
    @NotNull AnimeList getList();

    /**
     * Define this {@link AnimeEntity}'s {@link AnimeList}.
     *
     * @param list
     *         An {@link AnimeList}.
     */
    void setList(@NotNull AnimeList list);

    /**
     * Retrieve this {@link AnimeEntity}'s synopsis.
     *
     * @return A synopsis.
     */
    @Nullable String getSynopsis();

    /**
     * Define this {@link AnimeEntity}'s synopsis.
     *
     * @param synopsis
     *         A synopsis.
     */
    void setSynopsis(@Nullable String synopsis);

    /**
     * Retrieve this {@link AnimeEntity}'s tags.
     *
     * @return A {@link List} of tags.
     */
    @NotNull List<String> getTags();

    /**
     * Define this {@link AnimeEntity}'s tags.
     *
     * @param tags
     *         A {@link List} of tags.
     */
    void setTags(@NotNull List<String> tags);

    /**
     * Retrieve this {@link AnimeEntity}'s thumbnail url.
     *
     * @return A thumbnail url.
     */
    @Nullable String getThumbnailUrl();

    /**
     * Define this {@link AnimeEntity}'s thumbnail url.
     *
     * @param thumbnailUrl
     *         A thumbnail url.
     */
    void setThumbnailUrl(@Nullable String thumbnailUrl);

    /**
     * Retrieve this {@link AnimeEntity}'s nautiljon url.
     *
     * @return This {@link AnimeEntity}'s nautiljon url.
     */
    @NotNull String getUrl();

    /**
     * Define this {@link AnimeEntity}'s url.
     *
     * @param url
     *         This {@link AnimeEntity}'s url.
     */
    void setUrl(@Nullable String url);

    /**
     * Retrieve this {@link AnimeEntity}'s title regex which will allow to match RSS title for auto-download.
     *
     * @return A regex
     */
    @Nullable Pattern getTitleRegex();

    /**
     * Define this {@link AnimeEntity}'s title regex which will allow to match RSS title for auto-download.
     *
     * @param titleRegex
     *         A regex
     */
    void setTitleRegex(@Nullable Pattern titleRegex);

    /**
     * Retrieve the {@link UserEntity} which first imported this {@link AnimeEntity} into the application.
     *
     * @return A {@link UserEntity}.
     */
    @NotNull U getAddedBy();

    /**
     * Define the {@link UserEntity} which first imported this {@link AnimeEntity} into the application. If this is called after
     * the initial entity save, you are most likely doing this wrong and need to think about your life decisions.
     *
     * @param addedBy
     *         A {@link UserEntity}.
     */
    void setAddedBy(@NotNull U addedBy);

    /**
     * Retrieve this {@link AnimeEntity}'s anilist id.
     *
     * @return An anilist id.
     */
    @Nullable Long getAnilistId();

    /**
     * Define this {@link AnimeEntity}'s anilist id.
     *
     * @param anilistId
     *         An anilist id.
     */
    void setAnilistId(@Nullable Long anilistId);

    /**
     * Retrieve this {@link AnimeEntity}'s announcement message's id.
     *
     * @return A message id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    @Nullable Long getAnnouncementId();

    /**
     * Define this {@link AnimeEntity}'s announcement message's id.
     *
     * @param announcementId
     *         A message id.
     */
    @ExternallyBoundBy(ExternalBindType.DISCORD)
    void setAnnouncementId(@Nullable Long announcementId);

}
