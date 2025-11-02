package fr.anisekai.wireless.api.proxy.interfaces;


import fr.anisekai.wireless.api.proxy.Property;

import java.util.Map;

/**
 * Defines the contract for a state-aware proxy.
 * <p>
 * This interface provides methods to inspect the state of the proxied object, such as tracking changes (dirty state)
 * and accessing the original and differential states.
 *
 * @param <T>
 *         The type of the proxied object.
 */
public interface State<T> extends Dirtyable {

    /**
     * Returns the underlying, original instance that is being proxied.
     *
     * @return The original object instance.
     */
    T getInstance();

    /**
     * Returns the proxy instance. You should use this instance to trigger state changes.
     *
     * @return The proxy object.
     */
    T getProxy();

    /**
     * Retrieves the initial state of the object when it was first proxied.
     *
     * @return An unmodifiable map of the original property states.
     */
    Map<Property, Object> getOriginalState();

    /**
     * Retrieves only the properties that have changed since the object was proxied.
     *
     * @return An unmodifiable map containing only the modified properties and their new values.
     */
    Map<Property, Object> getDifferentialState();

    /**
     * Revert the instance to its original state when the {@link State} has been created.s
     */
    void revert();

}
