package fr.anisekai.wireless.remote.interfaces;

import fr.anisekai.wireless.api.json.AnisekaiJson;
import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.remote.enums.TaskStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

/**
 * Interface representing the base structure for a task.
 */
public interface TaskEntity extends Entity<Long> {

    /**
     * Retrieve this {@link TaskEntity}'s factory name.
     *
     * @return A factory name.
     */
    @NotNull String getFactoryName();

    /**
     * Define this {@link TaskEntity}'s factory name.
     *
     * @param factoryName
     *         A factory name.
     */
    void setFactoryName(@NotNull String factoryName);

    /**
     * Retrieve this {@link TaskEntity}'s name.
     *
     * @return A name.
     */
    @NotNull String getName();

    /**
     * Define this {@link TaskEntity}'s name.
     *
     * @param name
     *         A name.
     */
    void setName(@NotNull String name);

    /**
     * Retrieve this {@link TaskEntity}'s {@link TaskStatus}.
     *
     * @return A {@link TaskStatus}
     */
    @NotNull TaskStatus getStatus();

    /**
     * Define this {@link TaskEntity}'s {@link TaskStatus}.
     *
     * @param status
     *         A {@link TaskStatus}.
     */
    void setStatus(@NotNull TaskStatus status);

    /**
     * Retrieve this {@link TaskEntity}'s priority. Higher values will have a higher priority over other priorities.
     *
     * @return A priority level.
     */
    byte getPriority();

    /**
     * Retrieve this {@link TaskEntity}'s priority. Higher values will have a higher priority over other priorities.
     *
     * @param priority
     *         A priority level.
     */
    void setPriority(byte priority);

    /**
     * Retrieve this {@link TaskEntity}'s arguments required to run.
     *
     * @return An {@link AnisekaiJson}.
     */
    @NotNull AnisekaiJson getArguments();

    /**
     * Define this {@link TaskEntity}'s arguments required to run
     *
     * @param arguments
     *         An {@link AnisekaiJson}.
     */
    void setArguments(@NotNull AnisekaiJson arguments);

    /**
     * Retrieve this {@link TaskEntity}'s failure count.
     *
     * @return A failure count
     */
    byte getFailureCount();

    /**
     * Define this {@link TaskEntity}'s failure count.
     *
     * @param failureCount
     *         A failure count
     */
    void setFailureCount(byte failureCount);

    /**
     * Retrieve when this {@link TaskEntity} started its execution. Only available when this {@link TaskEntity}'s
     * {@link TaskStatus} is {@link TaskStatus#EXECUTING} or {@link TaskStatus#SUCCEEDED}.
     *
     * @return A {@link ZonedDateTime}.
     */
    @Nullable ZonedDateTime getStartedAt();

    /**
     * Define when this {@link TaskEntity} started its execution.
     *
     * @param startedAt
     *         A {@link ZonedDateTime}.
     */
    void setStartedAt(@Nullable ZonedDateTime startedAt);

    /**
     * Retrieve when this {@link TaskEntity} finished its execution. Only available when this {@link TaskEntity}'s
     * {@link TaskStatus} is set to {@link TaskStatus#SUCCEEDED}.
     *
     * @return A {@link ZonedDateTime}.
     */
    @Nullable ZonedDateTime getCompletedAt();

    /**
     * Define when this {@link TaskEntity} finished its execution.
     *
     * @param completedAt
     *         A {@link ZonedDateTime}.
     */
    void setCompletedAt(@Nullable ZonedDateTime completedAt);

}
