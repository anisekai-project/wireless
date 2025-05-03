package fr.anisekai.wireless.api.persistence.interfaces;

/**
 * Functional interface responsible for creating {@link EntityUpdatedEvent} instances.
 *
 * @param <E>
 *         The type of {@link Entity} being tracked
 * @param <V>
 *         The type of value being compared (e.g., a property of the {@link Entity})
 * @param <C>
 *         The specific event type that extends {@link EntityUpdatedEvent}
 */
public interface EventCreator<E extends Entity<?>, V, C extends EntityUpdatedEvent<E, V>> {

    /**
     * Creates a new {@link EntityUpdatedEvent} instance.
     *
     * @param eventType
     *         The class type of the event to be created
     * @param entity
     *         The {@link Entity} instance being updated
     * @param previous
     *         The previous value before the update
     * @param next
     *         The new value after the update
     *
     * @return the created {@link EntityUpdatedEvent} instance
     */
    C create(Class<C> eventType, E entity, V previous, V next);

}
