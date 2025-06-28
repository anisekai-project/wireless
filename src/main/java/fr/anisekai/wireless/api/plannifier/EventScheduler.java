package fr.anisekai.wireless.api.plannifier;

import fr.anisekai.wireless.api.plannifier.data.BookedPlanifiable;
import fr.anisekai.wireless.api.plannifier.data.CalibrationResult;
import fr.anisekai.wireless.api.plannifier.exceptions.DelayOverlapException;
import fr.anisekai.wireless.api.plannifier.exceptions.InvalidSchedulingDurationException;
import fr.anisekai.wireless.api.plannifier.exceptions.NotSchedulableException;
import fr.anisekai.wireless.api.plannifier.interfaces.ScheduleSpotData;
import fr.anisekai.wireless.api.plannifier.interfaces.Scheduler;
import fr.anisekai.wireless.api.plannifier.interfaces.SchedulerManager;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.Planifiable;
import fr.anisekai.wireless.api.plannifier.interfaces.entities.WatchTarget;
import fr.anisekai.wireless.utils.DateTimeUtils;
import fr.anisekai.wireless.utils.MapCollector;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Stream;

/**
 * Class allowing easy management of a schedule.
 *
 * @param <T>
 *         The watch target type. A watch target is some sort of container, representing a movie or series.
 * @param <I>
 *         The interface extending {@link Planifiable} to use for the proxy instance.
 * @param <E>
 *         The entity type. It is the type that will be scheduled.
 */
public class EventScheduler<T extends WatchTarget, I extends Planifiable<T>, E extends I> implements Scheduler<T, I, E> {

    /**
     * If two {@link Planifiable} are of the following duration apart, they will be merged once scheduled. The duration is done by
     * comparing one {@link Planifiable#getStartingAt()} with the endpoint of another {@link Planifiable}, being
     * {@link Planifiable#getStartingAt()} + {@link Planifiable#getDuration()}.
     */
    private static final Duration MERGE_MAGNET_LIMIT = Duration.ofMinutes(10);

    /**
     * Check if provided {@link ScheduleSpotData} overlap one another.
     *
     * @param one
     *         The first {@link ScheduleSpotData}
     * @param two
     *         The second {@link ScheduleSpotData}
     *
     * @return True if the {@link ScheduleSpotData} overlaps, false otherwise.
     */
    private boolean isOverlapping(ScheduleSpotData<T> one, ScheduleSpotData<T> two) {

        ZonedDateTime startingAt = one.getStartingAt();
        ZonedDateTime endingAt   = startingAt.plus(one.getDuration());

        ZonedDateTime itemStartingAt = two.getStartingAt();
        ZonedDateTime itemEndingAt   = itemStartingAt.plus(two.getDuration());

        return !startingAt.isAfter(itemEndingAt) && !startingAt.equals(itemEndingAt) && !endingAt.isBefore(
                itemStartingAt) && !endingAt.isEqual(itemStartingAt);
    }

    /**
     * Create a {@link Stream} of the current {@link EventScheduler} state, where every item will be filtered based on the return
     * value of {@link ScheduleSpotData#getStartingAt()}. If the returned value is before or equals to the provided
     * {@link ZonedDateTime}, the item will be kept.
     *
     * @param when
     *         The {@link ZonedDateTime} delimiting item filtering.
     *
     * @return A filtered {@link Stream} of the current state.
     */
    private Stream<E> findPreviousQuery(ZonedDateTime when) {

        return this.getState().stream().filter(item -> DateTimeUtils.isBeforeOrEquals(item.getStartingAt(), when));
    }

    /**
     * Create a {@link Stream} of the current {@link EventScheduler} state, where every item will be filtered based on the return
     * value of {@link ScheduleSpotData#getStartingAt()}. If the returned value is after or equals to the provided
     * {@link ZonedDateTime}, the item will be kept.
     *
     * @param when
     *         The {@link ZonedDateTime} delimiting item filtering.
     *
     * @return A filtered {@link Stream} of the current state.
     */
    private Stream<E> findAfterQuery(ZonedDateTime when) {

        return this.getState().stream().filter(item -> DateTimeUtils.isAfterOrEquals(item.getStartingAt(), when));
    }

    private final SchedulerManager<T, I, E> manager;
    private final Set<E>                    state;

    /**
     * Create a new instance of {@link Scheduler} using the provided {@link SchedulerManager}.
     *
     * @param manager
     *         {@link SchedulerManager} that this {@link Scheduler} will use when using CRUD operations.
     * @param items
     *         Default collection of {@link Planifiable} that will populate the state.
     */
    public EventScheduler(SchedulerManager<T, I, E> manager, Collection<E> items) {

        this.manager = manager;
        this.state   = new HashSet<>(items);
    }

    @Override
    public SchedulerManager<T, I, E> getManager() {

        return this.manager;
    }

    @Override
    public Set<E> getState() {

        return Collections.unmodifiableSet(this.state);
    }

    @Override
    public Optional<E> findPrevious(ZonedDateTime when) {

        return this.findPreviousQuery(when).max(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findNext(ZonedDateTime when) {

        return this.findAfterQuery(when).min(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findPrevious(ZonedDateTime when, T target) {

        return this.findPreviousQuery(when)
                   .filter(item -> item.getWatchTarget().equals(target))
                   .max(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public Optional<E> findNext(ZonedDateTime when, T target) {

        return this.findAfterQuery(when)
                   .filter(item -> item.getWatchTarget().equals(target))
                   .min(Comparator.comparing(Planifiable::getStartingAt));
    }

    @Override
    public boolean canSchedule(ScheduleSpotData<T> spot) {

        Duration duration = spot.getDuration();

        if (duration.isNegative() || duration.isZero()) {
            throw new InvalidSchedulingDurationException();
        }

        boolean prevOverlap = this.findPrevious(spot.getStartingAt())
                                  .map(item -> this.isOverlapping(spot, item))
                                  .orElse(false);

        boolean nextOverlap = this.findNext(spot.getStartingAt())
                                  .map(item -> this.isOverlapping(spot, item))
                                  .orElse(false);

        return !prevOverlap && !nextOverlap;
    }

    @Override
    public E schedule(ScheduleSpotData<T> spot) {

        if (!this.canSchedule(spot)) {
            throw new NotSchedulableException();
        }

        Optional<E> optPrev       = this.findPrevious(spot.getStartingAt());
        Optional<E> optNext       = this.findNext(spot.getStartingAt());
        Optional<E> optTargetPrev = this.findPrevious(spot.getStartingAt(), spot.getWatchTarget());

        boolean isPrevCombinable = optPrev.map(item -> this.mayMerge(item, spot)).orElse(false);
        boolean isNextCombinable = optNext.map(item -> this.mayMerge(spot, item)).orElse(false);

        if (isPrevCombinable && isNextCombinable) { // Dual way merge
            E prev = optPrev.get();
            E next = optNext.get();

            int newCount = prev.getEpisodeCount() + spot.getEpisodeCount() + next.getEpisodeCount();

            E updated = this.getManager().update(prev, item -> item.setEpisodeCount(newCount));

            // Copy to keep internal state updated.
            prev.setEpisodeCount(newCount);

            this.getManager().delete(next);
            this.state.remove(next); // This allows not destroying current instance.
            return updated;
        }

        if (isPrevCombinable) {

            E    prev     = optPrev.get();
            int newCount = prev.getEpisodeCount() + spot.getEpisodeCount();

            E updated = this.getManager().update(prev, item -> item.setEpisodeCount(newCount));

            // Copy to keep internal state updated.
            prev.setEpisodeCount(newCount);
            return updated;
        }

        if (isNextCombinable) {

            E    next     = optNext.get();
            int newCount = next.getEpisodeCount() + spot.getEpisodeCount();
            int firstEpisode = optTargetPrev
                    .map(item -> item.getFirstEpisode() + item.getEpisodeCount())
                    .orElseGet(() -> spot.getWatchTarget().getWatched() + 1);

            E updated = this.getManager().update(
                    next, item -> {
                        item.setFirstEpisode(firstEpisode);
                        item.setEpisodeCount(newCount);
                        item.setStartingAt(spot.getStartingAt());
                    }
            );

            // Copy to keep internal state updated.
            next.setFirstEpisode(firstEpisode);
            next.setEpisodeCount(newCount);
            next.setStartingAt(spot.getStartingAt());

            return updated;
        }


        Planifiable<T> planifiable;
        if (optTargetPrev.isPresent()) {
            E prev = optTargetPrev.get();
            planifiable = new BookedPlanifiable<>(spot, prev.getFirstEpisode() + prev.getEpisodeCount());
        } else {
            planifiable = new BookedPlanifiable<>(spot);
        }

        E entity = this.getManager().create(planifiable);
        this.state.add(entity);
        return entity;
    }

    @Override
    public List<E> delay(ZonedDateTime from, Duration interval, Duration delay) {

        ZonedDateTime to = from.plus(interval);

        List<E> events = this.getState()
                             .stream()
                             .filter(item -> DateTimeUtils.isAfterOrEquals(item.getStartingAt(), from))
                             .filter(item -> DateTimeUtils.isBeforeOrEquals(item.getEndingAt(), to))
                             .toList();

        // Creating a temporary state excluding events to delay to check for overlaps
        List<E> temporaryState = this.getState().stream().filter(item -> !events.contains(item)).toList();

        // Check if nothing overlaps the temporary state with the delay
        if (events.stream()
                  .map(item -> (Planifiable<T>) new BookedPlanifiable<>(item))
                  .peek(item -> item.setStartingAt(item.getStartingAt().plus(delay)))
                  .anyMatch(item -> temporaryState.stream().anyMatch(state -> this.isOverlapping(item, state)))) {

            throw new DelayOverlapException("One of the event cannot be delayed without conflict.");
        }

        // Apply the modification for real now
        List<E> updated = this.getManager().updateAll(events, item -> item.setStartingAt(item.getStartingAt().plus(delay)));
        // And update the internal state to keep track
        events.forEach(item -> item.setStartingAt(item.getStartingAt().plus(delay)));

        return updated;
    }

    @Override
    public CalibrationResult calibrate() {

        int updateCount = 0;
        int deleteCount = 0;

        // Store the max possible episode for each target
        Map<T, Integer> targetMaxEpisode = this.getState()
                                            .stream()
                                            .map(ScheduleSpotData::getWatchTarget)
                                            .distinct()
                                            .collect(new MapCollector<>(WatchTarget::getTotal));

        // Store the progress for each target
        Map<T, Integer> targetProgression = this.getState()
                                             .stream()
                                             .map(ScheduleSpotData::getWatchTarget)
                                             .distinct()
                                             .collect(new MapCollector<>(WatchTarget::getWatched));

        List<E> sorted = this.getState()
                             .stream()
                             .sorted(Comparator.comparing(ScheduleSpotData::getStartingAt))
                             .toList();

        for (E event : sorted) {

            int maxEpisode  = targetMaxEpisode.get(event.getWatchTarget());
            int progression = targetProgression.get(event.getWatchTarget());

            if (maxEpisode < 0) { // Support for "estimate" amount of episode, which are represented by negative number.
                maxEpisode = maxEpisode * -1;
            }

            boolean correctFirstEpisode = event.getFirstEpisode() == progression + 1;
            boolean correctEpisodeCount = (event.getFirstEpisode() + event.getEpisodeCount()) - 1 <= maxEpisode;

            int fixedFirstEpisode = progression + 1;
            int fixedEpisodeCount = Math.min(maxEpisode - progression, event.getEpisodeCount());

            // Don't keep overflowing events
            if (fixedFirstEpisode > maxEpisode) {
                this.getManager().delete(event);
                this.state.remove(event);
                deleteCount++;
                continue;
            }

            // If we require at least one thing to be updated, start the update
            if (!correctEpisodeCount || !correctFirstEpisode) {

                this.getManager().update(
                        event, item -> {

                            item.setFirstEpisode(fixedFirstEpisode);
                            item.setEpisodeCount(fixedEpisodeCount);
                        }
                );

                event.setFirstEpisode(fixedFirstEpisode);
                event.setEpisodeCount(fixedEpisodeCount);

                updateCount++;
            }

            // Keep track of our movement throughout the schedule
            targetProgression.put(event.getWatchTarget(), fixedFirstEpisode + fixedEpisodeCount - 1);
        }

        return new CalibrationResult(updateCount, deleteCount);
    }

    /**
     * Check if the two provided {@link ScheduleSpotData} can be merged. This is where the rule of merging should be decided
     * (timing, content, etc...)
     *
     * @param element
     *         The first {@link ScheduleSpotData}
     * @param planifiable
     *         The second {@link ScheduleSpotData}
     *
     * @return True if both event can be merged, false otherwise.
     */
    private boolean mayMerge(ScheduleSpotData<T> element, ScheduleSpotData<T> planifiable) {

        long breakTime  = Duration.between(element.getEndingAt(), planifiable.getStartingAt()).toSeconds();
        long magnetTime = MERGE_MAGNET_LIMIT.toSeconds();

        boolean isWithinMagnetTime = breakTime <= magnetTime;
        boolean isSameGroup        = Objects.equals(element.getWatchTarget(), planifiable.getWatchTarget());

        return isWithinMagnetTime && isSameGroup;
    }

}
