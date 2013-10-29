package com.polarnick.rss;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.InputSource;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.StringReader;
import java.util.List;

/**
 * Date: 13.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class RSSHandlerTest {
    private final RSSHandler handler = new RSSHandler();

    @Test
    public void testBashIm() throws Exception {
        testFeedByUrl("http://bash.im/rss/");
    }

    @Test
    public void testLentaRu() throws Exception {
        testFeedByUrl("http://lenta.ru/rss");
    }

    @Test
    public void testITHappensRu() throws Exception {
        testFeedByUrl("http://ithappens.ru/rss/");
    }

    public void testFeedByUrl(String url) throws Exception {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        SAXParser parser = factory.newSAXParser();

        HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(url));
        HttpEntity httpEntity = httpResponse.getEntity();

        String xml = EntityUtils.toString(httpEntity, "UTF-8");
        InputSource is = new InputSource(new StringReader(xml));
        parser.parse(is, handler);

        Feed feed = handler.retrieveFeed();
        Assert.assertNotNull(feed);

        checkStringNotEmpty(feed.getDescription());
        checkStringNotEmpty(feed.getTitle());

        List<FeedEntry> entries = feed.getEntries();
        Assert.assertNotNull(entries);
        Assert.assertTrue(entries.size() > 0);
        for (FeedEntry entry : entries) {
            checkStringNotEmpty(entry.getLink());
            checkStringNotEmpty(entry.getTitle());
            checkStringNotEmpty(entry.getDescription());
            Assert.assertNotNull(entry.getPublishedDate());
        }
    }

    private void checkStringNotEmpty(String value) {
        Assert.assertNotNull(value);
        Assert.assertFalse(value.isEmpty());
    }
}
