package com.polarnick.day06;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.*;
import com.polarnick.rss.Feed;
import com.polarnick.rss.FeedEntry;

import java.lang.ref.WeakReference;

/**
 * Date: 16.09.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class FeedActivity extends ListActivity implements UberResultReceiver.Receiver {

    private String currentFeedURL;
    private Feed currentFeed;
    private UberResultReceiver receiver;
    private Menu menu;
    private FeedsSQLiteOpenHelper sqlHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entries_list_view);
        ImageButton refreshButton = (ImageButton) findViewById(R.id.refreshButton);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadFeed();
            }
        });
        receiver = new UberResultReceiver(new Handler());
        receiver.setReceiver(this);
        sqlHelper = FeedsSQLiteOpenHelper.getInstance(this);
        initFeedURLByDefaultValue();
        loadFeed();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (menu != null) {
            updateMenuFeedList();
        }
    }

    private void initFeedURLByDefaultValue() {
        for (FeedsSQLiteOpenHelper.FeedEntry feed : sqlHelper.getFeedsList()) {
            if (feed.isDefault()) {
                currentFeedURL = feed.getUrl();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        this.menu = menu;
        updateMenuFeedList();
        return true;
    }

    private void updateMenuFeedList() {
        menu.clear();
        menu.add(getResources().getString(R.string.MANAGE_FEEDS)).setIcon(android.R.drawable.ic_menu_preferences);
        for (FeedsSQLiteOpenHelper.FeedEntry feed : sqlHelper.getFeedsList()) {
            menu.add(feed.getName());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle().equals(getResources().getString(R.string.MANAGE_FEEDS))) {
            Intent intent = new Intent(FeedActivity.this, FeedsManagmentActivity.class);
            startActivity(intent);
        } else {
            currentFeedURL = sqlHelper.getFeedsByNameMapping().get(item.getTitle().toString()).getUrl();
            loadFeed();
        }
        return true;
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Intent intent = new Intent(FeedActivity.this, EntryWebActivity.class);
        intent.putExtra(EntryWebActivity.ENTRY_KEY, currentFeed.getEntries().get(position));
        startActivity(intent);
    }

    private void loadFeed() {
        if (currentFeedURL == null) {
            TextView feedTitle = (TextView) findViewById(R.id.feedTitle);
            feedTitle.setText(getResources().getString(R.string.THERE_ARE_NO_DEFAULT_FEED));
            return;
        }

        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.LOADING_FEED));
        progressDialog.setCancelable(false);
        progressDialog.show();

        final WeakReference<Activity> activityReference = new WeakReference<Activity>(this);

        new FeedDownloadTask() {
            @Override
            public void onSuccess(Feed feed) {
                feed.setUrl(currentFeedURL);
                Intent intent = new Intent(FeedActivity.this, NewFeedChecker.class);
                intent.putExtra(NewFeedChecker.FEED_KEY, feed);
                intent.putExtra(NewFeedChecker.RECEIVER_KEY, receiver);
                startService(intent);

                setFeed(feed);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception exception) {
                progressDialog.dismiss();
                Activity activity = activityReference.get();
                final String message = exception.getMessage();
                if (activity != null) {
                    final WeakReference<TextView> feedTitleReference = new WeakReference<TextView>((TextView) activity.findViewById(R.id.feedTitle));
                    new AlertDialog.Builder(activity)
                            .setMessage(message)
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.ALERT_OK),
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            TextView feedTitle = feedTitleReference.get();
                                            if (feedTitle != null && feedTitle.getText().length() == 0) {
                                                feedTitle.setText(message);
                                            }
                                        }
                                    }).create().show();
                }
            }
        }.execute(currentFeedURL);
    }

    private void setFeed(Feed feed) {
        currentFeed = feed;
        TextView feedTitle = (TextView) findViewById(R.id.feedTitle);
        feedTitle.setText(feed.getTitle());
        TextView feedDescription = (TextView) findViewById(R.id.feedDescription);
        feedDescription.setText(feed.getDescription());
        ArrayAdapter<FeedEntry> adapter = new FeedEntriesAdapter(this, feed.getEntries());
        setListAdapter(adapter);
    }

    @Override
    public void onReceiveResult(int resultCode, final Bundle resultData) {
        boolean feedIsOutOfDate = resultData.getBoolean(IS_FRESHER_KEY);
        if (feedIsOutOfDate) {
            final Feed newFeed = (Feed) resultData.getSerializable(FEED_KEY);
            if (newFeed.getUrl().equals(currentFeedURL)) {
                Toast toast = Toast.makeText(this, getResources().getString(R.string.ALERT_FEED_UPDATE), Toast.LENGTH_SHORT);
                toast.show();
                setFeed(newFeed);
            }
        }
    }
}
