package fr.anisekai.wireless.api.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Lightweight Jackson implementation of {@link ObjectSerializer}. Does not depend on Spring, only on Jackson Databind.
 *
 * @param <T>
 *         The serialized object type.
 */
public final class JacksonObjectSerializer<T> implements ObjectSerializer<T> {

    private final ObjectMapper mapper;
    private final Class<T>     targetClass;

    /**
     * Create a new {@link JacksonObjectSerializer} instance.
     *
     * @param mapper
     *         The {@link ObjectMapper} to use.
     * @param targetClass
     *         The target class to serialize / deserialize.
     */
    public JacksonObjectSerializer(@NotNull ObjectMapper mapper, @NotNull Class<T> targetClass) {

        this.mapper      = mapper;
        this.targetClass = targetClass;
    }

    @Override
    public String serialize(@NotNull T container) {

        try {
            return this.mapper.writeValueAsString(container);
        } catch (IOException e) {
            throw new RuntimeException("Serialization failed for " + this.targetClass.getName(), e);
        }
    }

    @Override
    public T deserialize(@NotNull String raw) {

        try {
            return this.mapper.readValue(raw, this.targetClass);
        } catch (IOException e) {
            throw new RuntimeException("Deserialization failed for " + this.targetClass.getName(), e);
        }
    }

}
