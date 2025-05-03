package fr.anisekai.wireless.api.persistence.interfaces;

import org.jetbrains.annotations.ApiStatus;

/**
 * Interface representing an event associated to an {@link Entity}.
 *
 * @param <T>
 *         Type of the {@link Entity}.
 */
public interface EntityEvent<T extends Entity<?>> {

    /**
     * Retrieve the {@link Entity} associated to this {@link EntityEvent}.
     *
     * @return An {@link Entity}.
     */
    T getEntity();

    /**
     * Define the {@link Entity} associated to this {@link EntityEvent}
     * <p>
     * <b>Important:</b> You should <em>almost never</em> call this method directly. Refer to
     * {@link EventProxy#updateEventsEntity(Entity)} for the correct and safe way to update the entity across all related events.
     * <p>
     * Direct usage is strongly discouraged unless you are absolutely certain of the implications. Misuse can lead to inconsistent
     * state and event handling issues.
     *
     * @param entity
     *         The up-to-date {@link Entity} instance.
     */
    @ApiStatus.Internal
    void setEntity(T entity);

}
