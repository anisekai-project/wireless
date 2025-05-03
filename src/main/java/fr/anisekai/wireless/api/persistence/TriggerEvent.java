package fr.anisekai.wireless.api.persistence;

import fr.anisekai.wireless.api.persistence.interfaces.EntityUpdatedEvent;
import fr.anisekai.wireless.api.persistence.interfaces.EventProxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * Annotation used to mark a setter in an interface as a setter which should create an {@link EntityUpdatedEvent} when called
 * through a proxy using an {@link EventProxy}.
 * <p>
 * This can only be used on setters and on {@link Method} within an interface.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TriggerEvent {

    /**
     * {@link EntityUpdatedEvent} to instantiate when the {@link Method} is called through a proxy.
     *
     * @return The {@link EntityUpdatedEvent} class to instantiate.
     */
    Class<? extends EntityUpdatedEvent<?, ?>> value();

}
