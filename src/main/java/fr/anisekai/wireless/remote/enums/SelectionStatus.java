package fr.anisekai.wireless.remote.enums;

import fr.anisekai.wireless.remote.interfaces.AnimeEntity;
import fr.anisekai.wireless.remote.interfaces.SelectionEntity;
import fr.anisekai.wireless.remote.interfaces.UserEntity;

/**
 * Represents the status of a {@link SelectionEntity}, indicating whether it is open for voting or has been closed, either
 * manually or automatically.
 */
public enum SelectionStatus {

    /**
     * The {@link SelectionEntity} is opened and accepting votes.
     */
    OPEN(false),

    /**
     * The {@link SelectionEntity} has been manually closed by an {@link UserEntity} with application administrator privileges.
     */
    CLOSED(true),

    /**
     * The {@link SelectionEntity} has been automatically closed because the number of {@link AnimeEntity} entries is less than or
     * equal to the number of votes required, rendering the voting process unnecessary.
     */
    AUTO_CLOSED(true);


    private final boolean closed;

    SelectionStatus(boolean closed) {

        this.closed = closed;
    }

    /**
     * Indicates whether the current status represents a closed state.
     *
     * @return True if the selection is closed, false otherwise
     */
    public boolean isClosed() {

        return this.closed;
    }
}
