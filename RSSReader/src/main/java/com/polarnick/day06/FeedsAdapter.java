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
    private FeedsSQLiteOpenHelper sqlHelper = FeedsSQLiteOpenHelper.getInstance(null);
    private List<FeedsSQLiteOpenHelper.FeedEntry> feeds;

    public FeedsAdapter(Context context, List<FeedsSQLiteOpenHelper.FeedEntry> feeds) {
        super(context, R.layout.feed_item, feeds);
        this.feeds = feeds;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.feed_item, parent, false);

        final FeedsSQLiteOpenHelper.FeedEntry feed = feeds.get(index);

        ((TextView) view.findViewById(R.id.feedNameText)).setText(feed.getName());
        ((TextView) view.findViewById(R.id.feedUrlText)).setText(feed.getUrl());
        view.findViewById(R.id.setDefaultFeedButton).setEnabled(!feed.isDefault());
        view.findViewById(R.id.setDefaultFeedButton).setOnClickListener(new SetDefaultButtonlistener(feed));
        view.findViewById(R.id.deleteFeedButton).setOnClickListener(new DeleteFeedButtonListener(feed));

        return view;
    }

    private class SetDefaultButtonlistener implements View.OnClickListener {

        private final FeedsSQLiteOpenHelper.FeedEntry feed;

        private SetDefaultButtonlistener(FeedsSQLiteOpenHelper.FeedEntry feed) {
            this.feed = feed;
        }

        @Override
        public void onClick(View v) {
            for (FeedsSQLiteOpenHelper.FeedEntry feed : feeds) {
                if (feed.isDefault()) {
                    feed.setDefault(false);
                    sqlHelper.insertEntry(feed);
                }
            }
            feed.setDefault(true);
            sqlHelper.insertEntry(feed);
            notifyDataSetChanged();
        }
    }

    private class DeleteFeedButtonListener implements View.OnClickListener {

        private final FeedsSQLiteOpenHelper.FeedEntry feed;

        private DeleteFeedButtonListener(FeedsSQLiteOpenHelper.FeedEntry feed) {
            this.feed = feed;
        }

        @Override
        public void onClick(View v) {
            FeedsSQLiteOpenHelper.FeedEntry newDefault = sqlHelper.deleteEntry(feed);
            if (newDefault != null) {
                for (FeedsSQLiteOpenHelper.FeedEntry feed : feeds) {
                    if (feed.getName().equals(newDefault.getName())) {
                        feed.setDefault(true);
                    }
                }
            }
            feeds.remove(feed);
            notifyDataSetChanged();
        }
    }

}
