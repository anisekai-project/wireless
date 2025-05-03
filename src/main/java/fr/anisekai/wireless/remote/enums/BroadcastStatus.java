package fr.anisekai.wireless.remote.enums;

import fr.anisekai.wireless.remote.interfaces.BroadcastEntity;

/**
 * Enum representing a {@link BroadcastEntity} schedule state.
 */
public enum BroadcastStatus {

    /**
     * The {@link BroadcastEntity} is only saved in the database and has not been scheduled on Discord.
     */
    UNSCHEDULED(false),

    /**
     * The {@link BroadcastEntity} has been scheduled on Discord.
     */
    SCHEDULED(true),

    /**
     * The {@link BroadcastEntity} is currently active (being broadcasted)
     */
    ACTIVE(true),

    /**
     * The {@link BroadcastEntity} has been broadcasted.
     */
    COMPLETED(false),

    /**
     * The {@link BroadcastEntity} has been canceled.
     */
    CANCELED(false);

    private final boolean discordCancelable;

    BroadcastStatus(boolean discordCancelable) {

        this.discordCancelable = discordCancelable;
    }

    /**
     * Check if tDiscord would accept a cancel query on the current {@link BroadcastStatus}. This is completely different from
     * knowing if a {@link BroadcastEntity} is cancelable or not, as it is completely dependent on the implementation.
     *
     * @return True if it requires a Discord request to be completely canceled, false otherwise.
     */
    public boolean isDiscordCancelable() {

        return this.discordCancelable;
    }
}
