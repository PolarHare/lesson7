package com.polarnick.day06;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import com.polarnick.rss.FeedEntry;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * Date: 15.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class FeedEntriesAdapter extends ArrayAdapter<FeedEntry> {
    private List<FeedEntry> entries;

    private final DateFormat dateFormatter = new SimpleDateFormat("HH:mm");
    private final DateFormat oldDateFormatter = new SimpleDateFormat("HH:mm\ndd.MM");

    public FeedEntriesAdapter(Context context, List<FeedEntry> entries) {
        super(context, R.layout.entry_item, entries);
        this.entries = entries;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View entryView = inflater.inflate(R.layout.entry_item, parent, false);

        TextView entryTitleText = (TextView) entryView.findViewById(R.id.entryTitle);
        TextView entryUpdatedDateText = (TextView) entryView.findViewById(R.id.entryDateUpdated);

        final FeedEntry entry = entries.get(index);

        entryTitleText.setText(entry.getTitle());

        Calendar today = Calendar.getInstance();
        Calendar published = Calendar.getInstance();
        published.setTime(entry.getPublishedDate());
        if (today.get(Calendar.ERA) == published.get(Calendar.ERA)
                && today.get(Calendar.YEAR) == published.get(Calendar.YEAR)
                && today.get(Calendar.DAY_OF_YEAR) == published.get(Calendar.DAY_OF_YEAR)) {
            entryUpdatedDateText.setText(dateFormatter.format(entry.getPublishedDate()));
        } else {
            entryUpdatedDateText.setText(oldDateFormatter.format(entry.getPublishedDate()));
        }

        return entryView;
    }

}
