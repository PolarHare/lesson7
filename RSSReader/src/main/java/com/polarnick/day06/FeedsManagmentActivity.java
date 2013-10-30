package com.polarnick.day06;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.*;
import com.polarnick.rss.Feed;

import java.util.ArrayList;

/**
 * Date: 29.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class FeedsManagmentActivity extends Activity {

    private FeedsSQLiteOpenHelper sqlHelper;
    private FeedsAdapter adapter;
    private ArrayList<FeedsSQLiteOpenHelper.FeedEntry> feeds;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.feeds_view);
        ImageButton addFeedButton = (ImageButton) findViewById(R.id.addFeedButton);
        addFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final FeedsSQLiteOpenHelper.FeedEntry newFeed = new FeedsSQLiteOpenHelper.FeedEntry("", "", false);
                askUserForFeedURLAndSoOn(newFeed, null);
            }
        });
        sqlHelper = FeedsSQLiteOpenHelper.getInstance(this);
        feeds = new ArrayList<FeedsSQLiteOpenHelper.FeedEntry>(sqlHelper.getEntries().values());
        adapter = new FeedsAdapter(this, feeds);
        ListView list = (ListView) findViewById(R.id.feedsList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                FeedsSQLiteOpenHelper.FeedEntry feed = feeds.get(position);
                askUserToEditFeedName(feed, feed.getName(), null);
            }
        });
    }

    /**
     * @param reason can be null or empty
     */
    private void askUserToEditFeedName(final FeedsSQLiteOpenHelper.FeedEntry oldFeed, String oldName, String reason) {
        AlertDialog.Builder editFeedName = new AlertDialog.Builder(this);
        if (reason != null && !reason.isEmpty()) {
            editFeedName.setMessage(reason + "\n" + getResources().getString(R.string.EDIT_FEED_NAME));
        } else {
            editFeedName.setMessage(getResources().getString(R.string.EDIT_FEED_NAME));
        }
        final EditText input = new EditText(this);
        input.setText(oldName);
        editFeedName.setView(input);
        editFeedName.setPositiveButton(getResources().getString(R.string.ALERT_OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String newName = ((TextView) input).getText().toString();
                if (newName.length() == 0) {
                    askUserToEditFeedName(oldFeed, newName, getResources().getString(R.string.FEED_NAME_MUST_NOY_BE_EMPTY));
                } else if (newName.equals(getResources().getString(R.string.MANAGE_FEEDS))) {
                    askUserToEditFeedName(oldFeed, newName, "\"" + getResources().getString(R.string.MANAGE_FEEDS) + "\" "
                            + getResources().getString(R.string.MUST_NOT_BE_A_FEED_NAME));
                } else {
                    for (FeedsSQLiteOpenHelper.FeedEntry feed : feeds) {
                        if (feed.getName().equals(newName) && feed != oldFeed) {
                            askUserToEditFeedName(feed, newName, getResources().getString(R.string.FEED_NAME_ALREADY_USED));
                            return;
                        }
                    }

                    FeedsSQLiteOpenHelper.FeedEntry newDefault = sqlHelper.deleteEntry(oldFeed);
                    if (newDefault != null) {
                        for (FeedsSQLiteOpenHelper.FeedEntry feed : feeds) {
                            if (feed.getName().equals(newDefault.getName())) {
                                feed.setDefault(true);
                            }
                        }
                    }
                    oldFeed.setName(newName);
                    sqlHelper.insertEntry(oldFeed);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        editFeedName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        editFeedName.show();
    }

    /**
     * Will ask user for url, validate it, if it is invalid - re-ask url(with default value - previous entered(invalid)),
     * when url will pass validation - it will ask user to edit feed name(with default value equals to title, retrieved
     * from RSS feed), than validate it, and when validation will be passed - it will add new feed.
     *
     * @param reason can be null or empty. It is added to dialog, as a reason for asking URL. (Normally it can be
     *               "Entered URL is invalid!")
     */
    private void askUserForFeedURLAndSoOn(final FeedsSQLiteOpenHelper.FeedEntry newFeed, String reason) {
        AlertDialog.Builder dialogUrl = new AlertDialog.Builder(this);
        if (reason != null && !reason.isEmpty()) {
            dialogUrl.setMessage(reason + "\n" + getResources().getString(R.string.ENTER_FEED_URL));
        } else {
            dialogUrl.setMessage(getResources().getString(R.string.ENTER_FEED_URL));
        }
        final EditText input = new EditText(this);
        input.setText(newFeed.getUrl());
        dialogUrl.setView(input);
        dialogUrl.setPositiveButton(getResources().getString(R.string.ALERT_OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String url = ((TextView) input).getText().toString();
                String httpPrefix = "http://";
                if (!url.startsWith(httpPrefix)) {
                    url = httpPrefix + url;
                }
                newFeed.setUrl(url);
                if (newFeed.getUrl().isEmpty()) {
                    askUserForFeedURLAndSoOn(newFeed, getResources().getString(R.string.FEED_URL_MUST_NOT_BE_EMPTY));
                } else {
                    checkURLAndSoOn(newFeed);
                }
            }
        });

        dialogUrl.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newFeed.setUrl(null);
            }
        });
        dialogUrl.show();
    }

    /**
     * Will validate URL, setted in given newFeed. If it is invalid - user will be asked to correct this url.
     * If it is valid - user will be asked to edit feed name(with default value equals to title, retrieved
     * from RSS feed), than validate it, and when validation will be passed - it will add new feed.
     */
    private void checkURLAndSoOn(final FeedsSQLiteOpenHelper.FeedEntry newFeed) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.CHECKING_FEED));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new FeedDownloadTask() {
            @Override
            public void onSuccess(Feed feed) {
                newFeed.setName(feed.getTitle());
                progressDialog.dismiss();
                askUserForFeedNameAndSaveNewFeed(newFeed, null);
            }

            @Override
            public void onFailure(Exception exception) {
                progressDialog.dismiss();
                askUserForFeedURLAndSoOn(newFeed, getResources().getString(R.string.THIS_FEED_IS_NOT_SUPPORTED));
            }
        }.execute(newFeed.getUrl());
    }

    private void askUserForFeedNameAndSaveNewFeed(final FeedsSQLiteOpenHelper.FeedEntry newFeed, String reason) {
        AlertDialog.Builder dialogName = new AlertDialog.Builder(this);
        if (reason != null && !reason.isEmpty()) {
            dialogName.setMessage(reason + "\n" + getResources().getString(R.string.ENTER_FEED_NAME));
        } else {
            dialogName.setMessage(getResources().getString(R.string.ENTER_FEED_NAME));
        }
        final EditText input = new EditText(this);
        input.setText(newFeed.getName());
        dialogName.setView(input);
        dialogName.setPositiveButton(getResources().getString(R.string.ALERT_OK), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newFeed.setName(((TextView) input).getText().toString());
                if (newFeed.getName().length() == 0) {
                    askUserForFeedNameAndSaveNewFeed(newFeed, getResources().getString(R.string.FEED_NAME_MUST_NOY_BE_EMPTY));
                } else if (newFeed.getName().equals(getResources().getString(R.string.MANAGE_FEEDS))) {
                    askUserForFeedNameAndSaveNewFeed(newFeed, "\"" + getResources().getString(R.string.MANAGE_FEEDS) + "\" "
                            + getResources().getString(R.string.MUST_NOT_BE_A_FEED_NAME));
                } else {
                    for (FeedsSQLiteOpenHelper.FeedEntry feed : feeds) {
                        if (feed.getName().equals(newFeed.getName())) {
                            askUserForFeedNameAndSaveNewFeed(newFeed, getResources().getString(R.string.FEED_NAME_ALREADY_USED));
                            return;
                        }
                    }
                    sqlHelper.insertEntry(newFeed);
                    if (feeds.size() == 0) {
                        newFeed.setDefault(true);
                    }
                    feeds.add(newFeed);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        dialogName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                newFeed.setName(null);
            }
        });
        dialogName.show();
    }

}
