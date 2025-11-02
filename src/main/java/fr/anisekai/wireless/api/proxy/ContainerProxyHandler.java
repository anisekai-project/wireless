package fr.anisekai.wireless.api.proxy;

import fr.anisekai.wireless.api.proxy.interfaces.Dirtyable;
import fr.anisekai.wireless.api.proxy.interfaces.ProxyPolicy;
import fr.anisekai.wireless.api.proxy.interfaces.State;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;

public class ContainerProxyHandler implements InvocationHandler, Dirtyable {

    private static final Collection<String> MUTATOR_METHODS = new HashSet<>(Arrays.asList(
            "add", "remove", "put", "clear", "addAll", "removeAll", "putAll", "retainAll",
            "removeIf", "replaceAll", "compute", "computeIfAbsent", "computeIfPresent", "merge", "set"
    ));

    private final Property               property;
    private final Object                 originalContainer;
    private final ProxyPolicy            policy;
    private final Map<Object, State<?>>  proxyContext;
    private final Map<Object, Dirtyable> elementProxies = new IdentityHashMap<>();

    private boolean isDirty = false;

    @SuppressWarnings("ChainOfInstanceofChecks")
    public ContainerProxyHandler(Property property, Object originalContainer, ProxyPolicy policy, Map<Object, State<?>> proxyContext) {

        this.property = property;

        this.originalContainer = originalContainer;
        this.policy            = policy;
        this.proxyContext      = proxyContext;

        if (originalContainer instanceof Collection) {
            ((Iterable<?>) originalContainer).forEach(this::proxyElementIfNeeded);
        } else if (originalContainer instanceof Map) {
            ((Map<?, ?>) originalContainer).values().forEach(this::proxyElementIfNeeded);
        }
    }

    @Override
    @SuppressWarnings("ChainOfInstanceofChecks")
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        String methodName = method.getName();
        if (method.getParameterCount() == 0) {
            switch (methodName) {
                case "isDirty" -> {
                    return this.isDirty();
                }
                case "hashCode" -> {
                    return this.originalContainer.hashCode();
                }
                case "toString" -> {
                    return this.originalContainer.toString();
                }
            }
        }
        if (methodName.equals("equals") && method.getParameterCount() == 1) {
            Object other = args[0];
            // Unwrap the other object if it's also a proxy
            if (Proxy.isProxyClass(other.getClass())) {
                InvocationHandler handler = Proxy.getInvocationHandler(other);
                if (handler instanceof ContainerProxyHandler) {
                    other = ((ContainerProxyHandler) handler).originalContainer;
                }
            }
            return this.originalContainer.equals(other);
        }

        if (MUTATOR_METHODS.contains(method.getName())) {
            this.isDirty = true;
            // When adding new elements, we must proxy them.
            if (args != null) {
                for (Object arg : args) {
                    if (arg instanceof Collection) {
                        ((Iterable<?>) arg).forEach(this::proxyElementIfNeeded);
                    } else if (arg instanceof Map) {
                        ((Map<?, ?>) arg).values().forEach(this::proxyElementIfNeeded);
                    } else {
                        this.proxyElementIfNeeded(arg);
                    }
                }
            }
        }

        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof State) {
                    args[i] = ((State<?>) args[i]).getInstance();
                } else if (Proxy.isProxyClass(args[i].getClass())) {
                    InvocationHandler handler = Proxy.getInvocationHandler(args[i]);
                    if (handler instanceof ContainerProxyHandler) {
                        args[i] = ((ContainerProxyHandler) handler).originalContainer;
                    }
                }
            }
        }

        Object result = method.invoke(this.originalContainer, args);

        if (this.elementProxies.containsKey(result)) {
            Dirtyable elementState = this.elementProxies.get(result);
            if (elementState instanceof State) {
                return ((State<?>) elementState).getProxy();
            }
        }

        if (result instanceof Iterator) {
            return this.proxyIterator((Iterator<?>) result);
        }

        return result;
    }

    private void proxyElementIfNeeded(Object element) {

        if (this.policy.shouldProxy(this.property, element) && !this.elementProxies.containsKey(element)) {
            State<?> elementState = this.proxyContext.get(element);
            if (elementState == null) {
                elementState = ClassProxyFactory.create(element, this.policy);
            }
            this.elementProxies.put(element, elementState);
        }
    }

    @Override
    public boolean isDirty() {

        if (this.isDirty) {
            return true;
        }
        return this.elementProxies.values().stream().anyMatch(Dirtyable::isDirty);
    }


    private Iterator<?> proxyIterator(Iterator<?> original) {

        return new Iterator<>() {
            public boolean hasNext() {return original.hasNext();}

            public Object next() {

                Object element = original.next();
                return ContainerProxyHandler.this.elementProxies.containsKey(element) ? ((State<?>) ContainerProxyHandler.this.elementProxies.get(
                        element)).getProxy() : element;
            }

            public void remove() {

                ContainerProxyHandler.this.isDirty = true;
                original.remove();
            }
        };
    }

    @SuppressWarnings("unchecked")
    private <K> ListIterator<K> proxyListIterator(ListIterator<K> original) {

        return new ListIterator<K>() {
            public boolean hasNext() {return original.hasNext();}


            public K next() {

                K element = (K) original.next();
                return ContainerProxyHandler.this.elementProxies.containsKey(element) ?
                        ((State<K>) ContainerProxyHandler.this.elementProxies.get(element)).getProxy() :
                        element;
            }

            public boolean hasPrevious() {return original.hasPrevious();}

            public K previous() {

                K element = (K) original.previous();
                return ContainerProxyHandler.this.elementProxies.containsKey(element) ?
                        ((State<K>) ContainerProxyHandler.this.elementProxies.get(element)).getProxy() :
                        element;
            }

            public int nextIndex() {return original.nextIndex();}

            public int previousIndex() {return original.previousIndex();}

            public void remove() {

                ContainerProxyHandler.this.isDirty = true;
                original.remove();
            }

            public void set(K e) {

                ContainerProxyHandler.this.isDirty = true;
                original.set(e);
            }

            public void add(K e) {

                ContainerProxyHandler.this.isDirty = true;
                original.add(e);
            }
        };
    }

}
