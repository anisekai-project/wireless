package fr.anisekai.wireless.api.proxy;

import fr.anisekai.wireless.api.proxy.exceptions.ProxyAccessException;
import fr.anisekai.wireless.api.proxy.exceptions.ProxyCreationException;
import fr.anisekai.wireless.api.proxy.exceptions.ProxyInvocationException;
import fr.anisekai.wireless.api.proxy.interfaces.ProxyInterceptor;
import fr.anisekai.wireless.api.proxy.interfaces.ProxyPolicy;
import fr.anisekai.wireless.api.proxy.interfaces.State;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.This;
import net.bytebuddy.matcher.ElementMatchers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating state-aware proxy instances.
 * <p>
 * This is the main entry point for the proxying feature.
 */
public final class ClassProxyFactory {

    /**
     * A global, static cache for the generated proxy classes. This is the expensive part we want to do only once per
     * original class across the entire application.
     */
    private static final class ProxyClassCache {

        private static final Map<Class<?>, Class<?>> CACHE = new ConcurrentHashMap<>();

        private static Class<?> get(Class<?> originalClass) {

            return CACHE.computeIfAbsent(
                    originalClass, clazz -> {
                        try {
                            return new ByteBuddy()
                                    .subclass(clazz)
                                    .implement(State.class)
                                    .method(ElementMatchers.any())
                                    .intercept(MethodDelegation.to(StaticMasterInterceptor.class))
                                    .make()
                                    .load(clazz.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                                    .getLoaded();
                        } catch (Exception e) {
                            throw new ProxyCreationException(
                                    "Failed to generate proxy class for: " + clazz.getName(),
                                    e
                            );
                        }
                    }
            );
        }

    }

    private final Map<Object, ProxyInterceptor<?>> interceptors    = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<Object, Object>              proxyToInstance = Collections.synchronizedMap(new IdentityHashMap<>());
    private final Map<Object, State<?>>            instanceToState = Collections.synchronizedMap(new IdentityHashMap<>());
    private final ProxyPolicy                      policy;

    /**
     * Creates a new ProxyFactory with the default proxy policy.
     */
    public ClassProxyFactory() {

        this(ProxyPolicy.DEFAULT);
    }

    /**
     * Creates a new ProxyFactory with a custom proxy policy.
     *
     * @param policy
     *         The policy that determines which nested objects should also be proxied.
     */
    public ClassProxyFactory(ProxyPolicy policy) {

        this.policy = policy;
    }

    /**
     * Creates a new stateful proxy for the given object instance.
     *
     * @param instance
     *         The object to proxy. Must not be null.
     * @param <T>
     *         The type of the object.
     *
     * @return A {@link State} instance that wraps the original object and tracks its changes.
     *
     * @throws ProxyCreationException
     *         if the proxy cannot be created.
     */
    @SuppressWarnings("unchecked")
    public <T> State<T> create(T instance) {

        if (this.instanceToState.containsKey(instance)) {
            return (State<T>) this.instanceToState.get(instance);
        }

        try {
            Class<?> proxyClass = ProxyClassCache.get(instance.getClass());
            T        proxy      = (T) proxyClass.getDeclaredConstructor().newInstance();

            StaticMasterInterceptor.PROXY_OWNER_REGISTRY.put(proxy, this);

            ProxyInterceptor<T> interceptor = new ClassProxyImpl<>(
                    this,
                    instance,
                    proxy,
                    this.policy,
                    new IdentityHashMap<>(),
                    p -> {
                        this.interceptors.remove(p.getProxy());
                        this.proxyToInstance.remove(p.getProxy());
                        this.instanceToState.remove(p.getInstance());
                        StaticMasterInterceptor.PROXY_OWNER_REGISTRY.remove(p.getProxy());
                    }
            );

            this.interceptors.put(proxy, interceptor);
            this.proxyToInstance.put(proxy, instance);
            this.instanceToState.put(instance, interceptor);
            return interceptor;

        } catch (Exception e) {
            throw new ProxyCreationException("Failed to create proxy for class: " + instance.getClass().getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private <T> ProxyInterceptor<T> findInterceptor(T proxy) {

        return (ProxyInterceptor<T>) this.interceptors.get(proxy);
    }

    @NotNull
    private <T> ProxyInterceptor<T> getInterceptor(T proxy) {

        ProxyInterceptor<T> interceptor = this.findInterceptor(proxy);
        if (interceptor == null) {
            throw new ProxyAccessException("Orphan Proxy: Unable to find the proxy's interceptor.");
        }
        return interceptor;
    }

    @Nullable
    public <T> State<T> getState(T proxy) {

        return this.findInterceptor(proxy);
    }

    @NotNull
    public <T> State<T> getOrCreateState(T object) {

        State<T> state = this.getState(object);
        if (state == null) {
            return this.create(object);
        }
        return state;
    }

    /**
     * The instance-based interceptor method. This is called by the StaticMasterInterceptor after it has looked up the
     * correct factory instance.
     */
    @RuntimeType
    public Object intercept(@This Object self, @Origin Method method, @AllArguments Object[] inputArgs) {

        Object[] args = inputArgs == null ? new Object[0] : inputArgs;

        try {
            String methodName = method.getName();
            int    argCount   = method.getParameterCount();

            if (argCount == 0 && "hashCode".equals(methodName)) {
                return this.getInterceptor(self).getInstance().hashCode();
            }
            if (argCount == 0 && "toString".equals(methodName)) {
                return this.getInterceptor(self).getInstance().toString();
            }
            if (argCount == 1 && "equals".equals(methodName)) {
                Object selfInstance  = this.getInterceptor(self).getInstance();
                Object otherArg      = args[0];
                Object otherInstance = this.proxyToInstance.getOrDefault(otherArg, otherArg);
                return selfInstance.equals(otherInstance);
            }

            return this.getInterceptor(self).intercept(method, args);
        } catch (Exception e) {
            throw new ProxyInvocationException(
                    String.format("Unable to invoke '%s' on '%s'", method.getName(), self.getClass().getName()),
                    e
            );
        }
    }

    /**
     * Clears all caches and releases all proxy instances created by *this* factory. It also ensures that its proxies
     * are removed from the global registry.
     */
    public void close() {

        for (ProxyInterceptor<?> interceptor : List.copyOf(this.interceptors.values())) {
            interceptor.close();
        }
        this.interceptors.clear();
        this.proxyToInstance.clear();
        this.instanceToState.clear();
    }

}
