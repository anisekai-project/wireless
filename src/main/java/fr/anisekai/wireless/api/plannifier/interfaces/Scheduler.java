package fr.anisekai.wireless.api.plannifier.interfaces;

import fr.anisekai.wireless.api.plannifier.data.CalibrationResult;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Defines a generic scheduler capable of managing and orchestrating {@link Planifiable} entities over time for a given
 * {@link WatchTarget} type. A {@link  Scheduler} provides both querying and modification capabilities over a scheduled state,
 * including temporal navigation, validation, insertion, and recalibration of planned events.
 *
 * @param <T>
 *         The type of {@link WatchTarget} being scheduled.
 * @param <I>
 *         The intermediate type extending {@link Planifiable}, used for type-safe operations.
 * @param <E>
 *         The final entity type being scheduled, extending {@code I}.
 *
 * @see Planifiable
 * @see ScheduleSpotData
 * @see SchedulerManager
 */
public interface Scheduler<T extends WatchTarget, I extends Planifiable<T>, E extends I> {

    /**
     * Retrieve the {@link SchedulerManager} that will handle entity management with applicative services.
     *
     * @return A {@link SchedulerManager}
     */
    SchedulerManager<T, I, E> getManager();

    // <editor-fold desc="State Queries">

    /**
     * Retrieve the current immutable state of scheduled entities managed by this {@link Scheduler}.
     *
     * @return A state
     */
    Set<E> getState();

    /**
     * Check in the current state for a {@link Planifiable} starting right before the provided {@link ZonedDateTime}.
     *
     * @param when
     *         {@link ZonedDateTime} filtering all {@link Planifiable} possible in the state.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findPrevious(ZonedDateTime when);

    /**
     * Check in the current state for a {@link Planifiable} starting right after the provided {@link ZonedDateTime}.
     *
     * @param when
     *         {@link ZonedDateTime} filtering all {@link Planifiable} possible in the state.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findNext(ZonedDateTime when);

    /**
     * Check in the current state for a {@link Planifiable} starting right before the provided {@link ZonedDateTime} while
     * matching the provided {@link WatchTarget}.
     *
     * @param when
     *         {@link ZonedDateTime} filtering all {@link Planifiable} possible in the state.
     * @param target
     *         {@link WatchTarget} further filtering possible {@link Planifiable}.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findPrevious(ZonedDateTime when, T target);

    /**
     * Check in the current state for a {@link Planifiable} starting right after the provided {@link ZonedDateTime} while matching
     * the provided {@link WatchTarget}.
     *
     * @param when
     *         {@link ZonedDateTime} filtering all planifiable possible in the state.
     * @param target
     *         {@link WatchTarget} further filtering possible {@link Planifiable}.
     *
     * @return An optional {@link Planifiable}.
     */
    Optional<E> findNext(ZonedDateTime when, T target);

    /**
     * Check whether the given {@link ScheduleSpotData} can be scheduled without overlapping existing state or violating
     * constraints.
     *
     * @param spot
     *         {@link ScheduleSpotData} to validate for schedulability.
     *
     * @return True if the provided {@link ScheduleSpotData} can be scheduled, false otherwise.
     */
    boolean canSchedule(ScheduleSpotData<T> spot);

    /**
     * Schedule the provided {@link ScheduleSpotData} within this {@link Scheduler}. This will automatically update its internal
     * state.
     *
     * @param spot
     *         {@link ScheduleSpotData} to use as source for scheduling data.
     *
     * @return The scheduled entity.
     */
    E schedule(ScheduleSpotData<T> spot);

    // </editor-fold>

    // <editor-fold desc="State Actions">

    /**
     * Delay by the provided amount every {@link Planifiable} being in the interval. The interval is defined by a starting
     * {@link ZonedDateTime} and a duration.
     *
     * @param from
     *         {@link ZonedDateTime} defining the start of the interval
     * @param interval
     *         {@link Duration} defining the length of the interval
     * @param delay
     *         {@link Duration} defining the length of the delay to apply to every matching {@link Planifiable}.
     *
     * @return All updated entities.
     */
    List<E> delay(ZonedDateTime from, Duration interval, Duration delay);

    /**
     * Reprocesses all scheduled entities to ensure episode counts and durations are consistent. This process may trim or adjust
     * entries that are misaligned or redundant.
     * <p>
     * Existing events will not be merged.
     *
     * @return A {@link CalibrationResult} summarizing the number of updates and deletions performed.
     */
    CalibrationResult calibrate();

    // </editor-fold>

}
