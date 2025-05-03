package fr.anisekai.wireless.remote.keys;

import fr.anisekai.wireless.remote.interfaces.SelectionEntity;
import fr.anisekai.wireless.remote.interfaces.UserEntity;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * A composite key representing a voter's participation in a specific selection, identified by the selection ID and voter ID.
 *
 * @param selectionId
 *         The ID of the selection
 * @param voterId
 *         The ID of the voter (user)
 */
public record VoterKey(long selectionId, long voterId) implements Serializable {

    /**
     * Creates a new {@link VoterKey} instance from a {@link SelectionEntity} and a {@link UserEntity}.
     *
     * @param selection
     *         The selection in which the user voted
     * @param user
     *         The user who voted
     *
     * @return A new {@link VoterKey} instance
     *
     * @throws AssertionError
     *         Threw if either ID is {@code null}.
     */
    public static @NotNull VoterKey create(@NotNull SelectionEntity<?> selection, @NotNull UserEntity user) {

        assert selection.getId() != null;
        assert user.getId() != null;
        return new VoterKey(selection.getId(), user.getId());
    }

}
