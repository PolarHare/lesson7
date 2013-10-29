package com.polarnick.day06;

import android.app.AlertDialog;
import android.app.ListActivity;
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
public class FeedsManagmentActivity extends ListActivity {

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
                final FeedsSQLiteOpenHelper.FeedEntry newFeed = new FeedsSQLiteOpenHelper.FeedEntry(null, null, false);
                AlertDialog.Builder dialogName = new AlertDialog.Builder(FeedsManagmentActivity.this);
                dialogName.setMessage(getResources().getString(R.string.ENTER_FEED_NAME));
                final EditText input = new EditText(FeedsManagmentActivity.this);
                dialogName.setView(input);
                dialogName.setPositiveButton(getResources().getString(R.string.ALERT_OK), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        newFeed.setName(((TextView) input).getText().toString());
                        if (newFeed.getName().length() == 0) {
                            Toast toast = Toast.makeText(FeedsManagmentActivity.this,
                                    getResources().getString(R.string.FEED_NAME_MUST_NOY_BE_EMPTY), Toast.LENGTH_LONG);
                            toast.show();
                        } else if (newFeed.getName().equals(getResources().getString(R.string.MANAGE_FEEDS))) {
                            Toast toast = Toast.makeText(FeedsManagmentActivity.this,
                                    "\"" + getResources().getString(R.string.MANAGE_FEEDS) + "\" "
                                            + getResources().getString(R.string.MUST_NOT_BE_A_FEED_NAME), Toast.LENGTH_LONG);
                            toast.show();
                        } else {
                            AlertDialog.Builder dialogUrl = new AlertDialog.Builder(FeedsManagmentActivity.this);
                            dialogUrl.setMessage(getResources().getString(R.string.ENTER_FEED_URL));
                            final EditText input = new EditText(FeedsManagmentActivity.this);
                            dialogUrl.setView(input);
                            dialogUrl.setPositiveButton(getResources().getString(R.string.ALERT_OK), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    newFeed.setUrl(((TextView) input).getText().toString());
                                    if (newFeed.getUrl().length() == 0) {
                                        Toast toast = Toast.makeText(FeedsManagmentActivity.this,
                                                getResources().getString(R.string.FEED_URL_MUST_NOY_BE_EMPTY), Toast.LENGTH_LONG);
                                        toast.show();
                                    } else {
                                        checkAndAddFeed(newFeed);
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
                    }
                });

                dialogName.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        newFeed.setName(null);
                    }
                });
                dialogName.show();
            }
        });
        sqlHelper = FeedsSQLiteOpenHelper.getInstance(this);
        feeds = new ArrayList<FeedsSQLiteOpenHelper.FeedEntry>(sqlHelper.getEntries().values());
        adapter = new FeedsAdapter(this, feeds);
        setListAdapter(adapter);
    }

    private void checkAndAddFeed(final FeedsSQLiteOpenHelper.FeedEntry newFeed) {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.CHECKING_FEED));
        progressDialog.setCancelable(false);
        progressDialog.show();

        new FeedDownloadTask() {
            @Override
            public void onSuccess(Feed feed) {
                sqlHelper.insertEntry(newFeed);
                feeds.add(newFeed);
                adapter.notifyDataSetChanged();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Exception exception) {
                progressDialog.dismiss();
                Toast toast = Toast.makeText(FeedsManagmentActivity.this,
                        getResources().getString(R.string.THIS_FEED_IS_NOT_SUPPORTED), Toast.LENGTH_SHORT);
                toast.show();
            }
        }.execute(newFeed.getUrl());
    }

}
