package com.polarnick.day06;

import android.os.AsyncTask;
import android.util.Log;
import com.google.common.base.Preconditions;
import com.polarnick.rss.Feed;
import com.polarnick.rss.RSSHandler;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.StringReader;

/**
 * Date: 15.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public abstract class FeedDownloadTask extends AsyncTask<String, Integer, Feed> {
    private Exception exception;
    private RSSHandler handler = new RSSHandler();

    public abstract void onSuccess(Feed feed);

    public abstract void onFailure(Exception exception);

    @Override
    protected void onPostExecute(Feed feed) {
        if (exception == null) {
            Preconditions.checkNotNull(feed);
            onSuccess(feed);
        } else {
            onFailure(exception);
        }
    }

    @Override
    protected Feed doInBackground(String... params) {
        Preconditions.checkArgument(params.length == 1);

        String url = params[0];
        exception = null;
        Feed feed = null;
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();

            HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(url));
            HttpEntity httpEntity = httpResponse.getEntity();

            String xml = EntityUtils.toString(httpEntity, "UTF-8");
            InputSource is = new InputSource(new StringReader(xml));
            parser.parse(is, handler);

            feed = handler.retrieveFeed();
            feed.sortEntriesByDatePublished();
        } catch (IOException e) {
            e.printStackTrace();
            exception = new Exception("Internet problems!", e);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
            exception = new Exception("Parsing exception!", e);
        } catch (SAXException e) {
            e.printStackTrace();
            exception = new Exception("RSS parsing exception!", e);
        } catch (Exception e) {
            Log.e(FeedDownloadTask.class.getName(), e.toString());
            exception = e;
        }
        return feed;
    }
}
