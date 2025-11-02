package fr.anisekai.wireless.api.proxy;

import fr.anisekai.wireless.api.proxy.exceptions.ProxyCreationException;
import fr.anisekai.wireless.api.proxy.interfaces.Dirtyable;
import fr.anisekai.wireless.api.proxy.interfaces.ProxyPolicy;
import fr.anisekai.wireless.api.proxy.interfaces.State;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassProxyImpl<S> implements State<S> {

    private static final Map<Class<?>, Set<Property>> PROPERTIES_CACHE = new ConcurrentHashMap<>();

    private final S instance;
    private final S proxy;

    private final Map<Property, Object> originalState;
    private final Map<Property, Object> differentialState;
    private final Map<Method, Property> methodLookup;

    private final ProxyPolicy              policy;
    private final Map<Object, State<?>>    proxyContext;
    private final Map<Property, Dirtyable> subProxyStates;
    private final Map<Property, Object>    subProxyCache;

    public ClassProxyImpl(S instance, ProxyPolicy policy) throws ReflectiveOperationException {

        this(instance, policy, new IdentityHashMap<>());
    }

    @SuppressWarnings("unchecked")
    public ClassProxyImpl(S instance, ProxyPolicy policy, Map<Object, State<?>> proxyContext) throws ReflectiveOperationException {

        if (proxyContext.containsKey(instance)) {
            throw new IllegalStateException("Circular dependency detected for instance of " + instance.getClass()
                                                                                                      .getName());
        }

        this.instance       = instance;
        this.policy         = policy;
        this.proxyContext   = proxyContext;
        this.subProxyStates = new HashMap<>();
        this.subProxyCache  = new HashMap<>(); // <<< NEW: Initialize the cache

        Set<Property> properties = PROPERTIES_CACHE.computeIfAbsent(instance.getClass(), Property::computeProperties);
        this.originalState     = new HashMap<>();
        this.differentialState = new HashMap<>();
        this.methodLookup      = new HashMap<>();

        for (Property property : properties) {
            this.originalState.put(property, property.getGetter().invoke(instance));
            this.methodLookup.put(property.getGetter(), property);
            this.methodLookup.put(property.getSetter(), property);
        }

        this.proxy = (S) new ByteBuddy()
                .subclass(instance.getClass())
                .implement(State.class)
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(this))
                .make()
                .load(instance.getClass().getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
                .getLoaded()
                .getDeclaredConstructor()
                .newInstance();

        this.proxyContext.put(this.instance, this);
    }

    private static boolean strictlyEquals(Object newValue, Object originalValue) {

        boolean isEquals;
        if (newValue instanceof Collection || newValue instanceof Map || originalValue instanceof Collection || originalValue instanceof Map) {
            isEquals = (originalValue == newValue);
        } else {
            isEquals = Objects.equals(originalValue, newValue);
        }
        return isEquals;
    }

    @RuntimeType
    public Object intercept(@Origin Method method, @AllArguments Object[] args) throws Throwable {

        String methodName = method.getName();
        int    paramCount = method.getParameterCount();

        if (methodName.equals("hashCode") && paramCount == 0) {
            return System.identityHashCode(this.instance);
        }
        if (methodName.equals("equals") && paramCount == 1) {
            Object other = args[0];

            Object otherInstance = other;
            if (other instanceof State) {
                otherInstance = ((State<?>) other).getInstance();
            }

            return this.instance == otherInstance;
        }
        if (methodName.equals("toString") && paramCount == 0) {
            return this.instance.toString();
        }

        if (method.getDeclaringClass().equals(State.class) || method.getDeclaringClass().equals(Dirtyable.class)) {
            return method.invoke(this, args);
        }

        Property property = this.methodLookup.get(method);
        if (property == null) {
            return method.invoke(this.instance, args);
        }

        if (property.getGetter().equals(method)) {
            return this.handleGetter(property, args);
        }

        if (property.getSetter().equals(method)) {
            this.handleSetter(property, args);
            return null;
        }

        return method.invoke(this.instance, args);
    }

    private Object handleGetter(Property property, Object[] args) throws ReflectiveOperationException {

        if (this.subProxyCache.containsKey(property)) {
            return this.subProxyCache.get(property);
        }

        Object value            = property.getGetter().invoke(this.instance, args);
        Object subProxyInstance = null;

        if (this.policy.shouldProxy(property, value)) {
            State<?> subProxyState = this.proxyContext.computeIfAbsent(
                    value,
                    k -> ClassProxyFactory.create(value, this.policy)
            );
            this.subProxyStates.put(property, subProxyState);
            subProxyInstance = subProxyState.getProxy();
        } else if (this.policy.shouldProxyContainer(value)) {
            Class<?> getterReturnType = property.getGetter().getReturnType();
            if (!getterReturnType.isInterface()) {
                throw new ProxyCreationException(
                        "Cannot proxy container for property '" + property.getName() + "'. The getter's return type must be an interface (e.g., java.util.List).",
                        null
                );
            }

            Set<Class<?>> interfacesToProxy = new HashSet<>();
            interfacesToProxy.add(getterReturnType);
            Collections.addAll(
                    interfacesToProxy,
                    value.getClass().getInterfaces()
            );
            interfacesToProxy.add(Dirtyable.class);

            ContainerProxyHandler handler = new ContainerProxyHandler(property, value, this.policy, this.proxyContext);
            this.subProxyStates.put(property, handler);

            subProxyInstance = Proxy.newProxyInstance(
                    this.instance.getClass().getClassLoader(),
                    interfacesToProxy.toArray(new Class<?>[0]),
                    handler
            );
        }

        if (subProxyInstance != null) {
            this.subProxyCache.put(property, subProxyInstance);
            return subProxyInstance;
        }

        return value;
    }

    private void handleSetter(Property property, Object[] args) throws IllegalAccessException, InvocationTargetException {

        this.subProxyStates.remove(property);
        this.subProxyCache.remove(property);

        if (args.length == 0) {
            throw new IllegalStateException(String.format(
                    "Unable to proxy method '%s' of type '%s': No argument to use for setter.",
                    property.getSetter().getName(),
                    this.instance.getClass().getName()
            ));
        }

        Object newValue      = args[0];
        Object originalValue = this.originalState.get(property);

        if (strictlyEquals(newValue, originalValue)) {
            this.differentialState.remove(property);
        } else {
            this.differentialState.put(property, newValue);
        }

        property.getSetter().invoke(this.instance, newValue);
    }

    @Override
    public S getInstance() {return this.instance;}

    @Override
    public S getProxy() {return this.proxy;}

    @Override
    public boolean isDirty() {

        if (!this.differentialState.isEmpty()) {
            return true;
        }
        return this.subProxyStates.values().stream().anyMatch(Dirtyable::isDirty);
    }

    @Override
    public Map<Property, Object> getOriginalState() {

        //noinspection Java9CollectionFactory (Required, Map.copyOf does not allow null values)
        return Collections.unmodifiableMap(new HashMap<>(this.originalState));
    }


    @Override
    public Map<Property, Object> getDifferentialState() {

        Map<Property, Object> changes = new HashMap<>(this.differentialState);

        for (Map.Entry<Property, Dirtyable> entry : this.subProxyStates.entrySet()) {
            Property  property = entry.getKey();
            Dirtyable subState = entry.getValue();

            if (subState.isDirty() && !changes.containsKey(property)) {
                try {
                    Object currentValue = property.getGetter().invoke(this.instance);
                    changes.put(property, currentValue);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    throw new RuntimeException(
                            "Failed to retrieve current state for dirty property: " + property.getName(),
                            e
                    );
                }
            }
        }

        return Collections.unmodifiableMap(changes);
    }

    @Override
    public void revert() {

        this.subProxyStates.values().forEach(sub -> {
            if (sub instanceof State) {
                ((State<?>) sub).revert();
            }
        });

        for (Map.Entry<Property, Object> entry : this.getDifferentialState().entrySet()) {
            try {
                Property property      = entry.getKey();
                Object   originalValue = this.originalState.get(property);
                property.getSetter().invoke(this.instance, originalValue);
            } catch (Exception e) {
                throw new RuntimeException("Failed to revert property: " + entry.getKey().getName(), e);
            }
        }

        this.differentialState.clear();
        this.subProxyCache.clear();
        this.subProxyStates.clear();
    }

}
