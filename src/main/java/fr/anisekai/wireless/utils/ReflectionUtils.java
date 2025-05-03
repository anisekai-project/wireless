package fr.anisekai.wireless.utils;

import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * Utility class providing helper methods for working with Java Reflection, especially around method signature comparison and
 * annotation resolution in interface hierarchies.
 * <p>
 * This class is primarily used to locate methods annotated with a specific {@link Annotation} even when the annotation exists
 * only on an interface method and not on the implementation. It supports accurate method matching by comparing method signatures
 * (name, return type, and parameter types).
 */
public final class ReflectionUtils {

    private ReflectionUtils() {}

    /**
     * Searches for the nearest {@link Method} annotated with the specified {@link Annotation} in the given method's declaring
     * class or its interfaces.
     * <p>
     * If the annotation is not present on the {@link Method} itself, this method will recursively inspect all interfaces
     * implemented by the declaring class to find a matching {@link Method} (based on signature) that is annotated.
     *
     * @param method
     *         The {@link Method} on which the {@link Annotation} might appear.
     * @param annotationClass
     *         The requested {@link Annotation}.
     * @param <T>
     *         The type of the {@link Annotation}.
     *
     * @return The found {@link Annotation}, or {@code null} otherwise.
     */
    @Nullable
    public static <T extends Annotation> Method findNearestWithAnnotation(Method method, Class<T> annotationClass) {

        if (method.isAnnotationPresent(annotationClass)) {
            return method;
        }

        for (Class<?> anInterface : method.getDeclaringClass().getInterfaces()) {
            Method queryMethod = extractNearestAnnotation(anInterface, method, annotationClass);
            if (queryMethod != null) return queryMethod;
        }

        return null;
    }

    /**
     * Searches the given {@code lookup} {@link Class} for a {@link Method} that matches the signature of the provided
     * {@link Method}, and returns it if it is annotated with the specified annotation.
     * <p>
     * The comparison uses {@link #haveIdenticalSignatures(Method, Method)} to determine equivalence.
     *
     * @param lookup
     *         Lookup {@link Class} where every {@link Method} will be checked for a match.
     * @param method
     *         A {@link Method} against which every other {@link Method} will be checked for signature.
     * @param annotationClass
     *         The {@link Annotation} to find.
     * @param <T>
     *         The type of the {@link Annotation}.
     *
     * @return The found {@link Annotation}, or {@code null} otherwise.
     */
    @Nullable
    public static <T extends Annotation> Method extractNearestAnnotation(Class<?> lookup, Method method, Class<T> annotationClass) {

        for (Method lookupMethod : lookup.getMethods()) {
            if (haveIdenticalSignatures(lookupMethod, method) && lookupMethod.isAnnotationPresent(annotationClass)) {
                return lookupMethod;
            }
        }

        return null;
    }

    /**
     * Determines if two methods have the same signature by comparing their names, return types, and parameter types. This won't
     * check the method body.
     *
     * @param a
     *         The first method
     * @param b
     *         The second method
     *
     * @return {@code true} if the method signatures are identical, {@code false} otherwise
     */
    public static boolean haveIdenticalSignatures(Method a, Method b) {

        if (!a.getName().equals(b.getName())) return false;
        if (!a.getReturnType().equals(b.getReturnType())) return false;
        if (a.getParameterCount() != b.getParameterCount()) return false;
        for (int i = 0; i < a.getParameterTypes().length; i++) {
            if (!a.getParameterTypes()[i].equals(b.getParameterTypes()[i])) return false;
        }
        // Safe to assume that they are the same.
        return true;
    }

}
