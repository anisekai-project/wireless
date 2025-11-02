package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.interfaces.Entity;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;
import java.util.UUID;

public interface WorkerEntity<T extends TaskEntity> extends Entity<UUID> {

    /**
     * Retrieve the {@link TaskEntity} on which this {@link WorkerEntity} is currently working.
     *
     * @return A {@link TaskEntity}.
     */
    @Nullable T getTask();

    /**
     * Define the {@link TaskEntity} on which this {@link WorkerEntity} is currently working.
     *
     * @param task
     *         A {@link TaskEntity}.
     */
    void setTask(@Nullable T task);

    /**
     * Retrieve the {@link ZonedDateTime} when the {@link WorkerEntity} said hello for the last time :)
     *
     * @return A {@link ZonedDateTime}.
     */
    ZonedDateTime getLastHeartbeat();

    /**
     * Define the {@link ZonedDateTime} when the {@link WorkerEntity} said hello for the last time :)
     *
     * @param heartbeat
     *         A {@link ZonedDateTime}.
     */
    void setLastHeartbeat(ZonedDateTime heartbeat);

}
