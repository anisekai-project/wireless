package fr.anisekai.wireless.api.persistence;

import fr.anisekai.wireless.api.persistence.interfaces.Entity;
import fr.anisekai.wireless.api.persistence.interfaces.EntityEvent;
import fr.anisekai.wireless.api.persistence.interfaces.EntityUpdatedEvent;
import fr.anisekai.wireless.api.persistence.interfaces.EventProxy;
import fr.anisekai.wireless.utils.ReflectionUtils;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

/**
 * Default implementation of the {@link EventProxy} interface, allowing to generate {@link EntityEvent} without hassle.
 *
 * @param <I>
 *         Interface extending {@link Entity} used for the proxy.
 * @param <E>
 *         Class implementing {@link I} representing the final {@link Entity}.
 */
public abstract class EventProxyImpl<I extends Entity<?>, E extends I> implements EventProxy<I, E> {

    private final E                   entity;
    private final Map<Method, Method> methodCache;

    private final Map<Method, EntityUpdatedEvent<E, ?>> events;
    private final Map<Method, Object>                   valueCache;

    /**
     * Create an {@link EventProxy} of the provided {@link Entity}.
     *
     * @param instance
     *         The {@link Entity} that will be proxied.
     */
    public EventProxyImpl(E instance) {

        this.entity      = instance;
        this.methodCache = new HashMap<>();
        this.events      = new HashMap<>();
        this.valueCache  = new HashMap<>();


        Method[] methods = instance.getClass().getMethods();

        for (Method method : methods) {
            Method annotatedMethod = ReflectionUtils.findNearestWithAnnotation(method, TriggerEvent.class);
            if (annotatedMethod != null) {

                if (!annotatedMethod.getName().startsWith("set")) {
                    throw new IllegalArgumentException(String.format(
                            "Cannot build EventProxy on %s: The method %s is not a setter but is annotated with @TriggerEvent",
                            instance.getClass().getName(),
                            annotatedMethod.getName()
                    ));
                }

                List<String> names = getPossibleGetterNames(method);
                Optional<Method> getter = Arrays.stream(instance.getClass().getMethods())
                                                .filter(m -> names.contains(m.getName()))
                                                .findFirst();

                if (getter.isEmpty()) {
                    throw new IllegalArgumentException(String.format(
                            "Cannot build EventProxy on %s: The method %s do not have a getter counterpart.",
                            instance.getClass().getName(),
                            method.getName()
                    ));
                }

                this.methodCache.put(annotatedMethod, getter.get());
            }
        }
    }

    /**
     * Retrieve a {@link List} of {@link Method} names that could match a setter for the provided getter.
     *
     * @param proxiedMethod
     *         The {@link Method} to which a setter should be found.
     *
     * @return A {@link Collection} of {@link Method} names.
     */
    @NotNull
    private static List<String> getPossibleGetterNames(Method proxiedMethod) {

        String prop = proxiedMethod.getName().substring(3);

        return Arrays.asList(
                String.format("get%s", prop),
                String.format("is%s", prop),
                String.format("has%s", prop)
        );
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if (method.isAnnotationPresent(TriggerEvent.class)) {

            if (!this.valueCache.containsKey(method)) {
                Method getter = this.methodCache.get(method);
                Object value  = getter.invoke(this.entity);
                this.valueCache.put(method, value);
            }

            Object previous = this.valueCache.get(method);
            Object next     = args[0];

            if (Objects.equals(previous, next)) {
                // If they are the same, just rollback events
                this.events.remove(method);

            } else {
                TriggerEvent trigger = method.getAnnotation(TriggerEvent.class);

                try {
                    //noinspection DataFlowIssue — We are safe here, due to method.isAnnotationPresent
                    this.events.put(method, this.createEvent(trigger.value(), this.entity, previous, next));
                } catch (Exception e) {
                    throw new IllegalStateException(String.format(
                            "Could not create event '%s'",
                            trigger.value().getName()
                    ), e);
                }
            }
        }

        return method.invoke(this.entity, args);
    }

    @Override
    public E getEntity() {

        return this.entity;
    }

    @Override
    public I startProxy() {

        return (I) Proxy.newProxyInstance(
                this.entity.getClass().getClassLoader(),
                this.entity.getClass().getInterfaces(),
                this
        );
    }

    @Override
    public List<EntityUpdatedEvent<E, ?>> getEvents() {

        return new ArrayList<>(this.events.values());
    }

}
