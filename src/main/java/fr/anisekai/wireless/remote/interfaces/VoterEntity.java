package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.keys.VoterKey;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * Interface representing the base structure for a voter.
 *
 * @param <S>
 *         Type for the {@link SelectionEntity} implementation.
 * @param <U>
 *         Type for the {@link UserEntity} implementation.
 * @param <A>
 *         Type for the {@link AnimeEntity} implementation.
 */
public interface VoterEntity<S extends SelectionEntity<A>, U extends UserEntity, A extends AnimeEntity<U>> extends Entity<VoterKey> {

    /**
     * Retrieve the {@link SelectionEntity} to which this {@link VoterEntity} belongs.
     *
     * @return A {@link SelectionEntity}.
     */
    @NotNull S getSelection();

    /**
     * Define the {@link SelectionEntity} to which this {@link VoterEntity} belongs.
     *
     * @param selection
     *         A {@link SelectionEntity}
     */
    void setSelection(@NotNull S selection);

    /**
     * Retrieve this {@link VoterEntity}'s identity.
     *
     * @return A {@link UserEntity}
     */
    @NotNull U getUser();

    /**
     * Define this {@link VoterEntity}'s identity.
     *
     * @param user
     *         A {@link UserEntity}.
     */
    void setUser(@NotNull U user);

    /**
     * Retrieve this {@link VoterEntity}'s maximum vote amount.
     *
     * @return A vote amount.
     */
    short getAmount();

    /**
     * Define this {@link VoterEntity}'s maximum vote amount.
     *
     * @param amount
     *         A vote amount.
     */
    void setAmount(short amount);

    /**
     * Retrieve this {@link VoterEntity}'s voted {@link AnimeEntity}.
     *
     * @return A {@link Set} of {@link AnimeEntity}.
     */
    @NotNull Set<A> getVotes();

    /**
     * Define this {@link VoterEntity}'s voted {@link AnimeEntity}.
     *
     * @param votes
     *         A {@link Set} of {@link AnimeEntity}.
     */
    void setVotes(@NotNull Set<A> votes);

    /**
     * Retrieve this {@link Entity} primary key.
     *
     * @return The primary key.
     */
    @Override
    default VoterKey getId() {

        return VoterKey.create(this.getSelection(), this.getUser());
    }

}
