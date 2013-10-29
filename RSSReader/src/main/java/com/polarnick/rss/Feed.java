package com.polarnick.rss;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Date: 13.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class Feed implements Serializable {

    public static final String TITLE_TAG = "title";
    public static final String DESCRIPTION_TAG = "description";
    public static final String ENTRY_TAG = "item";

    private String url;
    private String title;
    private String description;
    private final List<FeedEntry> entries = new ArrayList<FeedEntry>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void addEntry(FeedEntry entry) {
        entries.add(entry);
    }

    public List<FeedEntry> getEntries() {
        return entries;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void sortEntriesByDatePublished() {
        Collections.sort(entries, new Comparator<FeedEntry>() {
            @Override
            public int compare(FeedEntry entry1, FeedEntry entry2) {
                return entry2.getPublishedDate().compareTo(entry1.getPublishedDate());
            }
        });
    }
}
