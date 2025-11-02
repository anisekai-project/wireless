package fr.anisekai.wireless.api.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Represents a complete JavaBean property, including its backing field, getter/setter methods, and name.
 */
public final class Property {

    private final Map<Class<? extends Annotation>, Optional<? extends Annotation>> annotationCache = new ConcurrentHashMap<>();

    private final Field  field;
    private final Method getter;
    private final Method setter;
    private final String name;

    /**
     * @param field
     *         The backing field for this property.
     * @param getter
     *         The public getter method.
     * @param setter
     *         The public setter method.
     * @param name
     *         The canonical name of the property (e.g., "name").
     */
    public Property(Field field, Method getter, Method setter, String name) {

        this.field  = field;
        this.getter = getter;
        this.setter = setter;
        this.name   = name;
    }

    /**
     * Introspects a class and returns a set of all its valid JavaBean properties. A valid property must have a public
     * getter, a public setter, and a corresponding backing field in the class hierarchy.
     *
     * @param clazz
     *         The class to analyze.
     *
     * @return A Set of {@link Property} records.
     */
    public static Set<Property> computeProperties(Class<?> clazz) {

        Map<String, List<Method>> methodsByProperty = Arrays
                .stream(clazz.getMethods())
                .filter(method -> !method.getDeclaringClass().equals(Object.class))
                .filter(Property::isPropertyMethod)
                .collect(Collectors.groupingBy(Property::getPropertyName));

        Set<Property> properties = new HashSet<>();
        for (Map.Entry<String, List<Method>> entry : methodsByProperty.entrySet()) {
            findValidPair(clazz, entry.getValue(), entry.getKey()).ifPresent(properties::add);
        }
        return properties;
    }

    private static Optional<Property> findValidPair(Class<?> originatingClass, Collection<Method> methods, String propertyName) {

        Optional<Method> getterOpt = methods.stream().filter(Property::isGetter).findFirst();
        if (getterOpt.isEmpty()) {
            return Optional.empty();
        }

        Method   getter       = getterOpt.get();
        Class<?> propertyType = getter.getReturnType();

        Optional<Method> setterOpt = methods
                .stream()
                .filter(Property::isSetter)
                .filter(setter -> setter.getParameterCount() == 1)
                .filter(setter -> setter.getParameterTypes()[0].equals(propertyType))
                .findFirst();

        return setterOpt.flatMap(
                method -> findFieldInHierarchy(originatingClass, propertyName)
                        .map(field -> new Property(field, getter, method, propertyName))
        );

    }

    private static Optional<Field> findFieldInHierarchy(Class<?> searchClass, String fieldName) {

        Class<?> currentClass = searchClass;
        while (currentClass != null && !currentClass.equals(Object.class)) {
            try {
                return Optional.of(currentClass.getDeclaredField(fieldName));
            } catch (NoSuchFieldException e) {
                currentClass = currentClass.getSuperclass();
            }
        }
        return Optional.empty();
    }

    private static <A extends Annotation> Optional<A> findAnnotationInHierarchy(Method method, Class<A> annotationClass) {

        return findAnnotationRecursive(method.getDeclaringClass(), method, annotationClass, new HashSet<>());
    }

    private static <A extends Annotation> Optional<A> findAnnotationRecursive(Class<?> searchClass, Method method, Class<A> annotationClass, Set<Class<?>> visitedInterfaces) {

        if (searchClass == null || searchClass.equals(Object.class)) {
            return Optional.empty();
        }
        try {
            Method currentMethod = searchClass.getDeclaredMethod(method.getName(), method.getParameterTypes());
            if (currentMethod.isAnnotationPresent(annotationClass)) {
                return Optional.of(currentMethod.getAnnotation(annotationClass));
            }
        } catch (NoSuchMethodException ignored) {
        }
        for (Class<?> implementedInterface : searchClass.getInterfaces()) {
            if (visitedInterfaces.add(implementedInterface)) {
                Optional<A> annotation = findAnnotationRecursive(
                        implementedInterface,
                        method,
                        annotationClass,
                        visitedInterfaces
                );
                if (annotation.isPresent()) {
                    return annotation;
                }
            }
        }
        return findAnnotationRecursive(searchClass.getSuperclass(), method, annotationClass, visitedInterfaces);
    }

    private static boolean isGetter(Method method) {

        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 0 || void.class.equals(method.getReturnType())) {
            return false;
        }
        String name = method.getName();
        return name.startsWith("get") || name.startsWith("is");
    }

    private static boolean isSetter(Method method) {

        if (Modifier.isStatic(method.getModifiers()) || method.getParameterCount() != 1) {
            return false;
        }
        return method.getName().startsWith("set");
    }

    private static boolean isPropertyMethod(Method method) {

        return isGetter(method) || isSetter(method);
    }

    private static String getPropertyName(Method method) {

        String methodName = method.getName();
        if (methodName.startsWith("get") || methodName.startsWith("set")) {
            return Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
        }
        if (methodName.startsWith("is")) {
            return Character.toLowerCase(methodName.charAt(2)) + methodName.substring(3);
        }
        throw new IllegalArgumentException("Method is not a valid property method: " + methodName);
    }

    public Field getField() {

        return this.field;
    }

    public Method getGetter() {

        return this.getter;
    }

    public Method getSetter() {

        return this.setter;
    }

    public String getName() {

        return this.name;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        Property property = (Property) o;
        return this.field.equals(property.field) &&
                this.getter.equals(property.getter) &&
                this.setter.equals(property.setter) &&
                this.name.equals(property.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(this.field, this.getter, this.setter, this.name);
    }

    @Override
    public String toString() {

        return "Property[name=%s, field=%s, getter=%s, setter=%s]".formatted(
                this.name,
                this.field.getName(),
                this.getter.getName(),
                this.setter.getName()
        );
    }

}
