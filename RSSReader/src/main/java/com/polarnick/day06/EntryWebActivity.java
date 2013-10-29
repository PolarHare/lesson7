package com.polarnick.day06;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;
import android.widget.TextView;
import com.polarnick.rss.FeedEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Date: 16.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class EntryWebActivity extends Activity {
    public static final String ENTRY_KEY = "entry";

    private static final String HTML_HEADER = "<html><head/><body>";
    private static final String HTML_FOOTER = "</body></html>";

    private final DateFormat todayDateFormatter = new SimpleDateFormat("HH:mm");
    private final DateFormat oldDateFormatter = new SimpleDateFormat("dd.MM.yyyy HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entry_view);


        FeedEntry entry = (FeedEntry) getIntent().getSerializableExtra(ENTRY_KEY);

        TextView title = (TextView) findViewById(R.id.entryTitleValue);
        title.setText(entry.getTitle());

        TextView date = (TextView) findViewById(R.id.entryDateValue);
        Calendar today = Calendar.getInstance();
        Calendar published = Calendar.getInstance();
        published.setTime(entry.getPublishedDate());
        if (today.get(Calendar.ERA) == published.get(Calendar.ERA)
                && today.get(Calendar.YEAR) == published.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == published.get(Calendar.DAY_OF_YEAR)) {
            date.setText(todayDateFormatter.format(entry.getPublishedDate()));
        } else {
            date.setText(oldDateFormatter.format(entry.getPublishedDate()));
        }

        WebView description = (WebView) findViewById(R.id.entryDescription);
        description.loadDataWithBaseURL(null, HTML_HEADER + entry.getDescription() + HTML_FOOTER, "text/html", "UTF-8", null);

        TextView link = (TextView) findViewById(R.id.linkToPage);
        link.setLinksClickable(true);
        link.setText(entry.getLink());
    }
}
