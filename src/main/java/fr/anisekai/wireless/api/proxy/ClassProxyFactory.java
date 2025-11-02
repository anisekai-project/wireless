package fr.anisekai.wireless.api.proxy;

import fr.anisekai.wireless.api.proxy.exceptions.ProxyCreationException;
import fr.anisekai.wireless.api.proxy.interfaces.ProxyPolicy;
import fr.anisekai.wireless.api.proxy.interfaces.State;

/**
 * Factory for creating state-aware proxy instances.
 * <p>
 * This is the main entry point for the proxying feature.
 */
public final class ClassProxyFactory {

    private ClassProxyFactory() {

    }

    /**
     * Creates a new stateful proxy for the given object instance, using the default proxy policy.
     *
     * @param instance
     *         The object to proxy.
     * @param <T>
     *         The type of the object.
     *
     * @return A {@link State} instance that wraps the original object.
     *
     * @throws ProxyCreationException
     *         if the proxy cannot be created due to reflection errors.
     *
     */
    public static <T> State<T> create(T instance) {

        return create(instance, ProxyPolicy.DEFAULT);
    }

    /**
     * Creates a new stateful proxy for the given object instance with a custom proxy policy.
     *
     * @param instance
     *         The object to proxy.
     * @param policy
     *         The policy that determines which nested objects should also be proxied.
     * @param <T>
     *         The type of the object.
     *
     * @return A {@link State} instance that wraps the original object.
     *
     * @throws ProxyCreationException
     *         if the proxy cannot be created due to reflection errors.
     */
    public static <T> State<T> create(T instance, ProxyPolicy policy) {

        try {
            return new ClassProxyImpl<>(instance, policy);
        } catch (Exception e) {
            throw new ProxyCreationException("Failed to create proxy for class: " + instance.getClass().getName(), e);
        }
    }

}
