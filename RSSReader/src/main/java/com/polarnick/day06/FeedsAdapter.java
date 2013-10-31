package com.polarnick.day06;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Date: 15.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class FeedsAdapter extends ArrayAdapter<FeedsSQLiteOpenHelper.FeedEntry> {
    private final FeedsSQLiteOpenHelper sqlHelper;
    private final List<FeedsSQLiteOpenHelper.FeedEntry> feeds;
    private final View.OnClickListener setDefaultFeedButtonListener;
    private final View.OnClickListener deleteFeedButtonListener;

    public FeedsAdapter(Context context, List<FeedsSQLiteOpenHelper.FeedEntry> feeds) {
        super(context, R.layout.feed_item, feeds);
        this.sqlHelper = FeedsSQLiteOpenHelper.getInstance(null);
        this.feeds = feeds;
        this.setDefaultFeedButtonListener = new SetDefaultButtonlistener();
        this.deleteFeedButtonListener = new DeleteFeedButtonListener();
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.feed_item, parent, false);

        final FeedsSQLiteOpenHelper.FeedEntry feed = feeds.get(index);

        ((TextView) view.findViewById(R.id.feedNameText)).setText(feed.getName());
        ((TextView) view.findViewById(R.id.feedUrlText)).setText(feed.getUrl());
        view.findViewById(R.id.setDefaultFeedButton).setEnabled(!feed.isDefault());
        view.findViewById(R.id.setDefaultFeedButton).setTag(feed);
        view.findViewById(R.id.setDefaultFeedButton).setOnClickListener(setDefaultFeedButtonListener);
        view.findViewById(R.id.deleteFeedButton).setTag(feed);
        view.findViewById(R.id.deleteFeedButton).setOnClickListener(deleteFeedButtonListener);

        return view;
    }

    private class SetDefaultButtonlistener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            FeedsSQLiteOpenHelper.FeedEntry feed = (FeedsSQLiteOpenHelper.FeedEntry) v.getTag();
            for (FeedsSQLiteOpenHelper.FeedEntry otherFeed : feeds) {
                if (otherFeed.isDefault()) {
                    otherFeed.setDefault(false);
                    sqlHelper.insertEntry(otherFeed);
                }
            }
            feed.setDefault(true);
            sqlHelper.insertEntry(feed);
            notifyDataSetChanged();
        }
    }

    private class DeleteFeedButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            FeedsSQLiteOpenHelper.FeedEntry feed = (FeedsSQLiteOpenHelper.FeedEntry) v.getTag();
            FeedsSQLiteOpenHelper.FeedEntry newDefault = sqlHelper.deleteEntry(feed);
            if (newDefault != null) {
                for (FeedsSQLiteOpenHelper.FeedEntry otherFeed : feeds) {
                    if (otherFeed.getName().equals(newDefault.getName())) {
                        otherFeed.setDefault(true);
                    }
                }
            }
            feeds.remove(feed);
            notifyDataSetChanged();
        }
    }

}
