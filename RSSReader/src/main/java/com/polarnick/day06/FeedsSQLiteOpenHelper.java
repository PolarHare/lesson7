package com.polarnick.day06;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.common.base.Preconditions;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Date: 29.10.13
 *
 * @author Nickolay Polyarniy aka PolarNick
 */
public class FeedsSQLiteOpenHelper extends SQLiteOpenHelper {

    private static final String FEEDS_TABLE = "feeds";

    private static final String FEEDS_NAME_COLUMN = "name";
    private static final String FEEDS_URL_COLUMN = "url";
    private static final String FEEDS_DEFAULT_COLUMN = "isDefault";
    private String[] allColumns = {FEEDS_NAME_COLUMN, FEEDS_URL_COLUMN, FEEDS_DEFAULT_COLUMN};

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_CREATE_QUERY = "create table "
            + FEEDS_TABLE + "("
            + FEEDS_NAME_COLUMN + " text primary key, "
            + FEEDS_URL_COLUMN + " text, "
            + FEEDS_DEFAULT_COLUMN + " text" +
            ");";

    private static final String INSERT_OR_REPLACE_QUERY = "insert or replace into " + FEEDS_TABLE + " " +
            "(" + FEEDS_NAME_COLUMN + ", " + FEEDS_URL_COLUMN + ", " + FEEDS_DEFAULT_COLUMN + ")" +
            "values (?, ?, ?);";

    private static final List<FeedEntry> defaultFeeds = Arrays.asList(
            new FeedEntry("NEWSru.com", "http://feeds.newsru.com/com/www/news/big", true),
            new FeedEntry("Lenta.ru", "http://lenta.ru/rss", false),
            new FeedEntry("Bash.im", "http://bash.im/rss/", false),
            new FeedEntry("IT happens", "http://ithappens.ru/rss/", false),
            new FeedEntry("Хабрахабр", "http://habrahabr.ru/rss/hubs/", false)
    );

    private static FeedsSQLiteOpenHelper instance = null;

    private SQLiteDatabase database;
    private Map<String, FeedEntry> entryByName;

    private FeedsSQLiteOpenHelper(Context context) {
        super(context, FEEDS_TABLE, null, DATABASE_VERSION);
        database = getWritableDatabase();
    }

    public static synchronized FeedsSQLiteOpenHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FeedsSQLiteOpenHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_QUERY);
        int defaultCount = 0;
        for (FeedEntry entry : defaultFeeds) {
            database.execSQL(INSERT_OR_REPLACE_QUERY, new Object[]{entry.getName(), entry.getUrl(), Boolean.toString(entry.isDefault())});
            if (entry.isDefault()) {
                defaultCount++;
            }
        }
        Preconditions.checkState(defaultCount == 1, "Incorrect default feeds list!");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(FeedsSQLiteOpenHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + FEEDS_TABLE);
        onCreate(db);
    }

    /**
     * @return inserted feed. It can differs from given feed - it will be default, if there were no feeds.
     */
    public synchronized FeedEntry insertEntry(FeedEntry entry) {
        if (getEntries().size() == 0) {
            entry = new FeedEntry(entry.getName(), entry.getUrl(), true);
        }
        database.execSQL(INSERT_OR_REPLACE_QUERY, new Object[]{entry.getName(), entry.getUrl(), Boolean.toString(entry.isDefault())});
        getEntries().put(entry.getName(), entry);
        return entry;
    }

    /**
     * @return changed feedEntry. It is so if deleted entry was default, then some of existed feed became default. If
     *         there are no one feed remains, of this feed is not a default - null returns.
     */
    public synchronized FeedEntry deleteEntry(FeedEntry entry) {
        FeedEntry changedFeed = null;
        if (entry.isDefault()) {
            FeedEntry anyFeed = null;
            for (FeedEntry feed : getEntries().values()) {
                if (!feed.getUrl().equals(entry.getUrl())) {
                    anyFeed = feed;
                }
            }
            if (anyFeed != null) {
                anyFeed.setDefault(true);
                changedFeed = anyFeed;
                insertEntry(changedFeed);
            }
        }
        database.delete(FEEDS_TABLE, FEEDS_NAME_COLUMN + " = '" + entry.getName()+"'", null);
        getEntries().remove(entry.getName());
        return changedFeed;
    }

    public synchronized Map<String, FeedEntry> getEntries() {
        if (entryByName == null) {
            entryByName = new HashMap<String, FeedEntry>();
            Cursor cursor = database.query(FEEDS_TABLE, allColumns, null, null, null, null, null);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                FeedEntry entry = cursorToFeed(cursor);
                entryByName.put(entry.getName(), entry);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return entryByName;
    }

    private FeedEntry cursorToFeed(Cursor cursor) {
        return new FeedEntry(cursor.getString(0), cursor.getString(1), Boolean.parseBoolean(cursor.getString(2)));
    }

    public static class FeedEntry {
        private String name;
        private String url;
        private boolean isDefault;

        public FeedEntry(String name, String url, boolean aDefault) {
            this.name = name;
            this.url = url;
            isDefault = aDefault;
        }

        public String getName() {
            return name;
        }

        public String getUrl() {
            return url;
        }

        public boolean isDefault() {
            return isDefault;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public void setDefault(boolean aDefault) {
            isDefault = aDefault;
        }
    }
}
