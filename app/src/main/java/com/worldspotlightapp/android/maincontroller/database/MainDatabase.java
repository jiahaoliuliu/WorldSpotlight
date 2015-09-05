package com.worldspotlightapp.android.maincontroller.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.worldspotlightapp.android.ui.MainApplication;

/**
 * Class used to store all the tables and its columns. It also uses the DBHelper to instantiate the database
 * @author Jiahao Liu
 * 
 */
public class MainDatabase {

    private static final String TAG = "MainDatabase";

    /**
     * Name of the database.
     */
    public static final String DB_NAME = "MainDatabase";

    /**
     * Current version of the database.
     * 1. Simple video format
     * 2. Added the column hashTagsList into Video table
     */
    public static final int VERSION = 2;

    /**
     * The table of Videos
     *
     * @author Jiahao Liu
     *
     */
    public interface TableVideo {
        public static final String TABLE_NAME = "Video";

        /**
         * The id of each row. It is the primary key of the table
         */
        public static final String _ID = "objectId";

        /**
         * The title of the video
         */
        public static final String TITLE = "title";

        /**
         * The description of the video
         */
        public static final String DESCRIPTION = "description";

        /**
         * The id of the video in YouTube
         */
        public static final String VIDEO_ID = "videoId";

        /**
         * The city where the video was filmed
         */
        public static final String CITY = "city";

        /**
         * The country where the video was filmed
         */
        public static final String COUNTRY = "country";

        /**
         * The latitude of the video where it was filmed
         */
        public static final String LATITUDE = "latitude";

        /**
         * The longitude of the video where it was filmed
         */
        public static final String LONGITUDE = "longitude";

        /**
         * The list of hashtags stored as json array
         */
        public static final String HASH_TAGS_LIST = "hashTagsList";

        public static final String CREATE = "CREATE TABLE IF NOT EXISTS "  + TABLE_NAME + " (" +
            _ID + " TEXT PRIMARY KEY NOT NULL" +
            ", " + TITLE + " TEXT NOT NULL" +
            ", " + DESCRIPTION + " TEXT" +
            ", " + VIDEO_ID + " TEXT NOT NULL" +
            ", " + CITY + " TEXT" +
            ", " + COUNTRY + " TEXT" +
            ", " + LATITUDE + " DOUBLE" +
            ", " + LONGITUDE + " DOUBLE" +
            ", " + HASH_TAGS_LIST + " TEXT" +
            ");";

        public static final String[] COLUMNS = {
                _ID, TITLE, DESCRIPTION, VIDEO_ID, CITY, COUNTRY, LATITUDE, LONGITUDE, HASH_TAGS_LIST
        };

        // TODO: Initialize database with some videos
        public static final String INITIALIZE_DATABASE = "";

        public static final String DROP = "DROP TABLE IF EXISTS " + TABLE_NAME;
    }

    /**
     * Removes all the tables of the database.
     */
    public static void deleteTables() {
        SQLiteDatabase db = SingletonHolder.INSTANCE.getWritableDatabase();
        db.execSQL(TableVideo.DROP);
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first
     * access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        /**
         * The only instance of {@link OpenDbHelper} there is.
         */
        private static final OpenDbHelper INSTANCE = new OpenDbHelper(MainApplication.getInstance());
    }

    /**
     * Returns the only instance of {@link OpenDbHelper}.
     */
    public static OpenDbHelper getDbHelper() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * A {@link SQLiteOpenHelper} that gives access to the application's database. This class cannot
     * not be directly instantiated by users. Its only instance is obtained via the
     * {@link OpenDbHelper#getDbHelper()} method.
     * 
     * @author Jiahao Liu
     * 
     */
    public static class OpenDbHelper extends SQLiteOpenHelper {
        private OpenDbHelper(Context context) {
            super(context, DB_NAME, null, VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(TableVideo.CREATE);

            // Create Index if needed
//            executeMultipleQuery(db, TableVideo.CREATE_INDEX);

            // Insert data
//            executeMultipleQuery(db, TableVideo.INITIALIZE_DATABASE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // TODO: Update the database
            // Drop the actual table
            db.execSQL(TableVideo.DROP);
            // Create all the data again
            onCreate(db);
        }

        @Override
        public void onOpen(SQLiteDatabase db) {
            super.onOpen(db);
            db.execSQL("PRAGMA foreign_keys=ON;");
        }

        private void executeMultipleQuery(SQLiteDatabase db, String multipleQueries) {
            String[] queries = multipleQueries.split("\n");
            for(String query : queries) {
                Log.v(TAG, "Query: " + query);
                db.execSQL(query);
            }
        }
    }
}