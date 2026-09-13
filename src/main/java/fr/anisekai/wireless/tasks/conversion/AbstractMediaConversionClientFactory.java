package fr.anisekai.wireless.tasks.conversion;

import fr.anisekai.scheduler.commons.interfaces.ObjectSerializer;
import fr.anisekai.scheduler.tasking.interfaces.factories.ClientFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Base client factory for the shared media conversion task contract.
 */
public abstract class AbstractMediaConversionClientFactory implements ClientFactory<@NotNull MediaConversionInput, MediaConversionOutput> {

    private final ObjectSerializer<MediaConversionInput>  argumentSerializer;
    private final ObjectSerializer<MediaConversionOutput> resultSerializer;

    /**
     * Creates a media conversion client factory.
     *
     * @param argumentSerializer
     *         Serializer for conversion inputs.
     * @param resultSerializer
     *         Serializer for conversion outputs.
     */
    public AbstractMediaConversionClientFactory(ObjectSerializer<MediaConversionInput> argumentSerializer, ObjectSerializer<MediaConversionOutput> resultSerializer) {

        this.argumentSerializer = argumentSerializer;
        this.resultSerializer   = resultSerializer;
    }

    @Override
    public @NotNull String getName() {

        return "media:convert";
    }

    @Override
    public @NotNull ObjectSerializer<MediaConversionInput> getArgumentsSerializer() {

        return this.argumentSerializer;
    }

    @Override
    public @NotNull ObjectSerializer<MediaConversionOutput> getResultSerializer() {

        return this.resultSerializer;
    }

}
