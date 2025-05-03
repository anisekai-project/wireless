package fr.anisekai.wireless.api.persistence.interfaces;

import fr.anisekai.wireless.api.persistence.TriggerEvent;

import java.lang.reflect.Method;

/**
 * Interface representing a specific {@link EntityEvent} related to one of the {@link Method} annotated with
 * {@link TriggerEvent}.
 *
 * @param <E>
 *         Type of the {@link Entity}.
 * @param <T>
 *         Type of the updated value.
 */
public interface EntityUpdatedEvent<E extends Entity<?>, T> extends EntityEvent<E> {

    /**
     * Retrieve the value before the update occurred.
     *
     * @return The old value.
     */
    T getOldValue();

    /**
     * Retrieve the value after the update occurred.
     *
     * @return The new value.
     */
    T getNewValue();

}
