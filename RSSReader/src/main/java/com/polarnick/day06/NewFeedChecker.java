package com.polarnick.day06;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import com.polarnick.rss.Feed;
import com.polarnick.rss.FeedEntry;
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
 * Date: 23.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class NewFeedChecker extends IntentService {
    public static final String FEED_KEY = "feed";
    public static final String RECEIVER_KEY = "receiver";

    private static final int PERIOD_OF_REFRESH = 5 * 60 * 1000;//5 minutes

    public NewFeedChecker() {
        this("NewFeedCheckerIntentService");
    }

    public NewFeedChecker(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ResultReceiver receiver = intent.getParcelableExtra(RECEIVER_KEY);
        Feed lastDownloadedFeed = (Feed) intent.getSerializableExtra(FEED_KEY);

        Feed actualFeed = null;
        String url = lastDownloadedFeed.getUrl();
        try {
            ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if (mWifi.isConnected()) {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser parser = factory.newSAXParser();

                HttpResponse httpResponse = new DefaultHttpClient().execute(new HttpGet(url));
                HttpEntity httpEntity = httpResponse.getEntity();

                String xml = EntityUtils.toString(httpEntity, "UTF-8");
                InputSource is = new InputSource(new StringReader(xml));
                RSSHandler handler = new RSSHandler();
                parser.parse(is, handler);

                actualFeed = handler.retrieveFeed();
                actualFeed.sortEntriesByDatePublished();
                actualFeed.setUrl(url);
            }
        } catch (IOException e) {
            Log.w(NewFeedChecker.class.getName(), e.toString());
        } catch (ParserConfigurationException e) {
            Log.w(NewFeedChecker.class.getName(), e.toString());
        } catch (SAXException e) {
            Log.w(NewFeedChecker.class.getName(), e.toString());
        }

        boolean moreActual = false;
        if (actualFeed != null) {
            final FeedEntry newestEntry = actualFeed.getEntries().get(0);
            final FeedEntry freshestEntry = lastDownloadedFeed.getEntries().get(0);
            moreActual = newestEntry.getPublishedDate().compareTo(freshestEntry.getPublishedDate()) > 0;
        }

        Bundle bundle = new Bundle();
        bundle.putBoolean(UberResultReceiver.Receiver.IS_FRESHER_KEY, moreActual);
        if (moreActual) {
            intent.putExtra(FEED_KEY, actualFeed);
            bundle.putSerializable(UberResultReceiver.Receiver.FEED_KEY, actualFeed);
        }
        receiver.send(0, bundle);

        scheduleNextUpdate(intent);
    }

    private void scheduleNextUpdate(Intent intent) {
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + PERIOD_OF_REFRESH, pendingIntent);
    }
}
