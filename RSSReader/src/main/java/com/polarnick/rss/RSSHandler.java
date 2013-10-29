package com.polarnick.rss;

import com.google.common.base.Preconditions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Stack;

/**
 * Date: 21.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class RSSHandler extends DefaultHandler {

    private static final String RSS_TAG = "rss";
    private static final String FEED_TAG = "channel";
    private static final DateFormat dateFormat = new SimpleDateFormat("E, d MMM y HH:mm:ss Z", Locale.ENGLISH);

    private Stack<String> stack;
    private Stack<State> stateStack;
    private Feed feed;
    private FeedEntry entry;
    private StringBuilder characters;

    @Override
    public void startDocument() throws SAXException {
        stack = new Stack<String>();
        stateStack = new Stack<State>();
    }

    @Override
    public void endDocument() throws SAXException {
        stack = null;
        stateStack = null;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        stack.push(qName);
        characters = new StringBuilder();
        if (stateStack.isEmpty()) {
            Preconditions.checkState(qName.equals(RSS_TAG));
            stateStack.push(State.IN_RSS);
        } else if (stateStack.peek() == State.IN_RSS) {
            Preconditions.checkState(qName.equals(FEED_TAG));
            stateStack.push(State.IN_FEED);

            feed = new Feed();
        } else if (stateStack.peek() == State.IN_FEED && qName.equals(Feed.ENTRY_TAG)) {
            stateStack.push(State.IN_ENTRY);

            entry = new FeedEntry();
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        characters.append(ch, start, length);
    }

    private Date parseDate(String date) throws SAXException {
        try {
            return dateFormat.parse(date);
        } catch (ParseException e) {
            throw new SAXException("Date format is incorrect!", e);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        Preconditions.checkState(qName.equals(stack.pop()));
        if (stateStack.peek() == State.IN_RSS) {
            Preconditions.checkState(qName.equals(RSS_TAG));
            stateStack.pop();
        } else if (stateStack.peek() == State.IN_FEED && qName.equals(FEED_TAG)) {
            stateStack.pop();
        } else if (stateStack.peek() == State.IN_ENTRY && qName.equals(Feed.ENTRY_TAG)) {
            stateStack.pop();

            feed.addEntry(entry);
            entry = null;
        } else if (stateStack.peek() == State.IN_FEED) {
            if (Feed.DESCRIPTION_TAG.equals(qName)) {
                feed.setDescription(characters.toString());
            } else if (Feed.TITLE_TAG.equals(qName)) {
                feed.setTitle(characters.toString());
            }
        } else if (stateStack.peek() == State.IN_ENTRY) {
            if (FeedEntry.LINK_TAG.equals(qName)) {
                entry.setLink(characters.toString());
            } else if (FeedEntry.TITLE_TAG.equals(qName)) {
                entry.setTitle(characters.toString());
            } else if (FeedEntry.PUBLICATION_DATE_TAG.equals(qName)) {
                final Date publishedDate = parseDate(characters.toString());
                entry.setPublishedDate(publishedDate);
            } else if (FeedEntry.DESCRIPTION_TAG.equals(qName)) {
                entry.setDescription(characters.toString());
            }
        }
    }

    /**
     * @return feed and delete it from this RSSHandler
     */
    public Feed retrieveFeed() {
        Feed result = feed;
        feed = null;
        return result;
    }

    private static enum State {
        IN_RSS,
        IN_FEED,
        IN_ENTRY
    }

}
