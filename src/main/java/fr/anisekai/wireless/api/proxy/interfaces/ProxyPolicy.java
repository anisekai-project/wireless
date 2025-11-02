package fr.anisekai.wireless.api.proxy.interfaces;

import fr.anisekai.wireless.api.proxy.Property;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProxyPolicy {

    ProxyPolicy DEFAULT = new ProxyPolicy() {
        @Override
        public boolean shouldProxy(Property property, Object object) {

            if (object == null) {
                return false;
            }

            Class<?> type = object.getClass();

            if (type.isPrimitive() || type.isEnum() || type.isArray()) {
                return false;
            }

            if (this.shouldProxyContainer(object)) {
                return false;
            }

            String pkg = type.getPackage().getName();

            if (pkg.startsWith("java.lang") || pkg.startsWith("java.time") || pkg.startsWith("java.math")) {
                return false;
            }
            return !pkg.startsWith("java.util");
        }
    };

    /**
     * Check if the provided object value from the {@link Property} is proxyable as a stateful object.
     *
     * @param property
     *         The {@link Property} from which the object value has been retrieved.
     * @param object
     *         The possibly {@code null} object value.
     *
     * @return {@code true} if the value should be proxied using a class proxy, {@code false} otherwise.
     */
    boolean shouldProxy(Property property, Object object);

    /**
     * Check if the provided object value is a container that should be proxied for change tracking.
     *
     * @param object
     *         The possibly {@code null} object value.
     *
     * @return {@code true} if the value is a container to be proxied, {@code false} otherwise.
     */
    default boolean shouldProxyContainer(Object object) {

        return object instanceof List || object instanceof Map || object instanceof Set;
    }

}
