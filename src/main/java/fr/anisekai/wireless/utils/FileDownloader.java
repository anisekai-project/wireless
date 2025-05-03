package fr.anisekai.wireless.utils;

import fr.alexpado.lib.rest.RestAction;
import fr.alexpado.lib.rest.enums.RequestMethod;
import fr.alexpado.lib.rest.interfaces.IRestResponse;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link RestAction} implementation for downloading raw binary data from a specified URL.
 * <p>
 * This class issues a simple HTTP GET request and returns the response body as a {@code byte[]} array.
 */
public class FileDownloader extends RestAction<byte[]> {

    private final String url;

    /**
     * Constructs a new {@link FileDownloader} targeting the given URL.
     *
     * @param url
     *         The URL from which to download data
     */
    public FileDownloader(String url) {

        this.url = url;
    }

    @Override
    public @NotNull RequestMethod getRequestMethod() {

        return RequestMethod.GET;
    }

    @Override
    public @NotNull String getRequestURL() {

        return this.url;
    }

    @Override
    public byte[] convert(IRestResponse response) {

        return response.getBody();
    }

}
