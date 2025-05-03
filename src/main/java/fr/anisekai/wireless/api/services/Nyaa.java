package fr.anisekai.wireless.api.services;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import org.jdom.Element;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for fetching and parsing RSS feeds from Nyaa.si or any other compatible backend.
 */
public final class Nyaa {

    private Nyaa() {}

    /**
     * Represents a single RSS entry from the Nyaa feed.
     *
     * @param title
     *         The title of the torrent entry.
     * @param link
     *         A unique link (often a GUID or URI).
     * @param torrent
     *         The direct torrent download link.
     * @param hash
     *         The torrent's info hash (can be {@code null} if not present).
     */
    public record Entry(String title, String link, String torrent, String hash) {

    }

    /**
     * Fetches and parses an RSS feed from the given URI.
     *
     * @param uri
     *         The URI of the RSS feed.
     *
     * @return A list of parsed {@link Entry} objects.
     *
     * @throws IOException
     *         If an I/O error occurs during retrieval.
     * @throws FeedException
     *         If the feed is malformed or cannot be parsed.
     */
    public static List<Entry> fetch(URI uri) throws IOException, FeedException {

        SyndFeedInput       input   = new SyndFeedInput();
        SyndFeed            feed    = input.build(new XmlReader(uri.toURL()));
        Iterable<SyndEntry> entries = (List<SyndEntry>) feed.getEntries();

        List<Entry> items = new ArrayList<>();

        for (SyndEntry entry : entries) {

            String title   = entry.getTitle();
            String link    = entry.getUri();
            String torrent = entry.getLink();
            String hash    = null;

            for (Element tag : (Iterable<Element>) entry.getForeignMarkup()) {
                if (tag.getName().equals("infoHash")) {
                    hash = tag.getText();
                }
            }

            items.add(new Entry(title, link, torrent, hash));
        }

        return items;
    }

}
