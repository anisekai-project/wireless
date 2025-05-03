package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.annotations.ExternallyBoundBy;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.enums.ExternalBindType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Interface representing the base structure for a user.
 */
public interface UserEntity extends Entity<Long> {

    /**
     * Retrieve this {@link UserEntity}'s username.
     *
     * @return This {@link UserEntity}'s username.
     */
    @ExternallyBoundBy({ExternalBindType.AUTH, ExternalBindType.INTERACTION})
    @NotNull String getUsername();

    /**
     * Define this {@link UserEntity}'s username.
     *
     * @param username
     *         This {@link UserEntity}'s username.
     */
    @ExternallyBoundBy({ExternalBindType.AUTH, ExternalBindType.INTERACTION})
    void setUsername(@NotNull String username);

    /**
     * Retrieve this {@link UserEntity}'s nickname.
     *
     * @return This {@link UserEntity}'s nickname.
     */
    @ExternallyBoundBy(ExternalBindType.INTERACTION)
    @Nullable String getNickname();

    /**
     * Define this {@link UserEntity}'s nickname.
     *
     * @param nickname
     *         This {@link UserEntity}'s nickname.
     */
    @ExternallyBoundBy(ExternalBindType.INTERACTION)
    void setNickname(@Nullable String nickname);

    /**
     * Retrieve this {@link UserEntity}'s avatar url.
     *
     * @return The {@link UserEntity}'s avatar
     */
    @ExternallyBoundBy(ExternalBindType.INTERACTION)
    @NotNull String getAvatarUrl();

    /**
     * Define this {@link UserEntity}'s avatar url.
     *
     * @param avatarUrl
     *         The {@link UserEntity}'s avatar url.
     */
    @ExternallyBoundBy(ExternalBindType.INTERACTION)
    void setAvatarUrl(@NotNull String avatarUrl);

    /**
     * Retrieve this {@link UserEntity}'s emote. An emote will be used at various places in the application to represent the
     * {@link UserEntity}.
     *
     * @return An unicode emote
     */
    @Nullable String getEmote();

    /**
     * Define this {@link UserEntity}'s emote. An emote will be used at various places in the application to represent the
     * {@link UserEntity}.
     *
     * @param emote
     *         An unicode emote.
     */
    void setEmote(@Nullable String emote);

    /**
     * Check if this {@link UserEntity} is active. An active {@link UserEntity} will be able to take part in seasonal selection
     * and their votes will be counted toward an {@link AnimeEntity} score.
     *
     * @return True if this {@link UserEntity} is active, false otherwise.
     */
    boolean isActive();

    /**
     * Check if this {@link UserEntity} is active. An active {@link UserEntity} will be able to take part in seasonal selection
     * and their votes will be counted toward an {@link AnimeEntity} score.
     *
     * @param active
     *         True if this {@link UserEntity} is active, false otherwise.
     */
    void setActive(boolean active);

    /**
     * Check if this {@link UserEntity} is an application administrator.
     *
     * @return True if this {@link UserEntity} is an application administrator, false otherwise.
     */
    boolean isAdministrator();

    /**
     * Define if this {@link UserEntity} is an application administrator.
     *
     * @param administrator
     *         True if this {@link UserEntity} is an application administrator, false otherwise.
     */
    void setAdministrator(boolean administrator);

    /**
     * Check if this {@link UserEntity} is a guest. A guest {@link UserEntity} will only have limited access to the website and
     * application, requiring manual approval from an application administrator.
     *
     * @return True if this {@link UserEntity} is a guest, false otherwise.
     */
    boolean isGuest();

    /**
     * Define if this {@link UserEntity} is a guest. A guest {@link UserEntity} will only have limited access to the website and
     * application, requiring manual approval from an application administrator.
     *
     * @param guest
     *         True if this {@link UserEntity} is a guest, false otherwise.
     */
    void setGuest(boolean guest);

    /**
     * Retrieve this {@link UserEntity}'s api key. The api key will allow the {@link UserEntity} to access some route of the
     * application REST API without needing to go through the oauth process.
     *
     * @return An API Key
     */
    @Nullable
    String getApiKey();

    /**
     * Define this {@link UserEntity}'s api key. The api key will allow the {@link UserEntity} to access some route of the
     * application REST API without needing to go through the oauth process.
     *
     * @param apiKey
     *         An API Key
     */
    void setApiKey(@Nullable String apiKey);

}
