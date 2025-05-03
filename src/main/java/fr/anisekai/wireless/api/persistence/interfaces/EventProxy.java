package fr.anisekai.wireless.api.persistence.interfaces;

import java.lang.reflect.InvocationHandler;
import java.util.Collection;
import java.util.List;

/**
 * Interface representing a class capable of managing {@link Entity} to produce automatically some {@link EntityEvent}.
 *
 * @param <I>
 *         Raw interface of the {@link Entity} to proxy.
 * @param <E>
 *         Implementation class of the {@link Entity} to proxy.
 */
public interface EventProxy<I extends Entity<?>, E extends I> extends InvocationHandler {

    /**
     * Retrieve the instance which has been / will be proxied by this {@link EventProxy}.
     *
     * @return An {@link Entity} instance.
     */
    E getEntity();

    /**
     * Create a proxy of the {@link Entity} defined in this {@link EventProxy} and returns it.
     *
     * @return A proxy instance of an {@link Entity}
     */
    I startProxy();

    /**
     * Retrieve a {@link Collection} of {@link EntityUpdatedEvent} created while the proxy created by {@link #startProxy()} has
     * been used.
     *
     * @return A {@link Collection} of {@link EntityUpdatedEvent}.
     */
    List<EntityUpdatedEvent<E, ?>> getEvents();

    /**
     * Create the event instance of {@link EntityUpdatedEvent} matching the provided {@link Class}.
     *
     * @param eventType
     *         Class of the {@link EntityUpdatedEvent} to instantiate.
     * @param entity
     *         The {@link Entity} to associate with the event.
     * @param oldValue
     *         The previous value of the {@link Entity} changed property.
     * @param newValue
     *         The next (current) value of the {@link Entity} changed property.
     *
     * @return A new instance of the {@link EntityUpdatedEvent}.
     *
     * @throws Exception
     *         Thrown when the event could not be instantiated.
     */
    EntityUpdatedEvent<E, ?> createEvent(Class<? extends EntityUpdatedEvent<?, ?>> eventType, E entity, Object oldValue, Object newValue) throws Exception;

    /**
     * Replaces the {@link Entity} instance in all tracked {@link EntityEvent}s with the provided one. This is useful when an
     * {@link Entity} has been modified and needs to be reloaded or replaced within the context of active events.
     * <p>
     * <b>Important:</b> This method internally uses the public-facing {@link EntityEvent#setEntity(Entity)} method. Direct usage
     * of the aforementioned method outside of this context is strongly discouraged, as it can lead to inconsistencies or
     * confusion during event processing. Always call this method <b>before</b> dispatching any {@link EntityEvent}, and
     * <b>never</b> after.
     *
     * @param entity
     *         The new entity instance to apply to all tracked {@link EntityEvent}s.
     */
    default void updateEventsEntity(E entity) {

        this.getEvents().forEach(e -> e.setEntity(entity));
    }

}
