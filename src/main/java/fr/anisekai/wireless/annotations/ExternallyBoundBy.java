package fr.anisekai.wireless.annotations;

import fr.anisekai.wireless.remote.enums.ExternalBindType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Annotation used on methods or fields to warn the developer that it is bound to an external value and should be used carefully,
 * especially for setter / writing values, as it is most likely only meant to be modified in a specific flow.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface ExternallyBoundBy {

    /**
     * Allow to better describe how the field / method associated to this annotation might change.
     *
     * @return An array of {@link ExternalBindType}.
     */
    @SuppressWarnings("UnusedReturnValue") ExternalBindType[] value();

}
