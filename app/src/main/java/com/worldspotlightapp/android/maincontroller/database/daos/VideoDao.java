package com.worldspotlightapp.android.maincontroller.database.daos;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.worldspotlightapp.android.maincontroller.database.MainDatabase;
import com.worldspotlightapp.android.maincontroller.database.MainDatabase.TableVideo;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;

/**
 * Data access object for Video ({@link com.worldspotlightapp.android.model.Video} class).
 */

public class VideoDao {

    private static final String TAG = "VideoDao";

    private SQLiteDatabase mDatabase;

    public VideoDao() {
        mDatabase = MainDatabase.getDbHelper().getWritableDatabase();
    }

    /**
     * Returns a {@link Cursor} over the video table that contains a specific video. If
     * the video does not exist, an empty cursor is returned.
     *
     * @param objectId
     *        The Object id of the video to retrieve.
     * @return a Cursor pointing to the video.
     * @throws SQLException
     *             if any error while accessing the database.
     */
    public Cursor queryDataByObjectId(String objectId) {
       Cursor cursor = mDatabase.query(TableVideo.TABLE_NAME, null, TableVideo._ID + "=?",
                new String[] {objectId}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    /**
     * Returns a {@link Cursor} over the video table that contains a specific video. If
     * the video does not exist, an empty cursor is returned.
     *
     * @param videoId
     *        The video id of the video to retrieve.
     * @return a Cursor pointing to the video.
     * @throws SQLException
     *             if any error while accessing the database.
     */
    public Cursor queryDataByVideoId(String videoId) {
        Cursor cursor = mDatabase.query(TableVideo.TABLE_NAME, null, TableVideo.VIDEO_ID + "=?",
                new String[] {videoId}, null, null, null);
        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor queryAllData() {
        return mDatabase.rawQuery("select * from " + TableVideo.TABLE_NAME, null);
    }


    /**
     * Given a {@link Cursor} over the video table, this method construct a data
     * {@link com.worldspotlightapp.android.model.Video} objects from the current position of the cursor, and returns it.
     * Note that the cursor's position will not be changed.
     *
     * @param cursor
     *            the cursor to extract {@link com.worldspotlightapp.android.model.Video}s from.
     *            Note that it just retrieve the first element of the cursor. The rest of the elements
     *            will be ignored
     * @param isLastData
     *            If the data get is the last data or not. If so, the cursor will be closed
     * @return the video retrieved from the cursor.
     *         If the cursor is null, then return null
     */
    public Video getDataFromCursor(Cursor cursor, boolean isLastData) {
        if (cursor == null || cursor.getCount() == 0) {
            return null;
        }

        Video video = new Video();

        // ID. The primary key
        video.setObjectId(cursor.getString(cursor.getColumnIndex(TableVideo._ID)));

        // Title
        video.put(Video.PARSE_COLUMN_TITLE, cursor.getString(cursor.getColumnIndex(TableVideo.TITLE)));

        // Description. It could be null
        int descriptionColumnIndex = cursor.getColumnIndex(TableVideo.DESCRIPTION);
        if (!cursor.isNull(descriptionColumnIndex)) {
            video.put(Video.PARSE_COLUMN_DESCRIPTION, cursor.getString(descriptionColumnIndex));
        }

        // Video id
        video.put(Video.PARSE_COLUMN_VIDEO_ID, cursor.getString(cursor.getColumnIndex(TableVideo.VIDEO_ID)));

        // City. It could be null
        int cityColumnIndex = cursor.getColumnIndex(TableVideo.CITY);
        if (!cursor.isNull(cityColumnIndex)) {
            video.put(Video.PARSE_COLUMN_CITY, cursor.getString(cityColumnIndex));
        }

        // Country. It could be null
        int countryColumnIndex = cursor.getColumnIndex(TableVideo.COUNTRY);
        if (!cursor.isNull(countryColumnIndex)) {
            video.put(Video.PARSE_COLUMN_COUNTRY, cursor.getString(countryColumnIndex));
        }

        // Latitude and longitude.
        double latitude = cursor.getDouble(cursor.getColumnIndex(TableVideo.LATITUDE));
        double longitude = cursor.getDouble(cursor.getColumnIndex(TableVideo.LONGITUDE));
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(latitude, longitude);
        video.put(Video.PARSE_COLUMN_LOCATION, parseGeoPoint);

        // HashTags
        String hashTagsJsonArray = cursor.getString(cursor.getColumnIndex(TableVideo.HASH_TAGS_LIST));
        video.setHashTags(hashTagsJsonArray);

        if (isLastData) {
            cursor.close();
        }

        return video;
    }

    /**
     * Get list of data from cursor
     * @param cursor
     *      The cursor to query the data
     * @return
     *      The list of videos in the cursor.
     *      If the cursor is null, return null
     */
    public List<Video> getListDataFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        List<Video> results = new ArrayList<Video>();

        // Ask the cursor to move to position -1, so when
        // cursor.moveToNext is called for the first time
        // the position is 0
        cursor.moveToPosition(-1);

        while (cursor.moveToNext()) {
            results.add(getDataFromCursor(cursor, false));
        }

        cursor.close();

        return results;
    }

    /**
     * Inser a video into the database
     * @param video
     * @return
     */
    public long insertDataToDatabase(Video video) {
        if (video == null) {
            Log.e(TAG, "Error inserting data into the database. The video cannot be null");
            return -1;
        }

        ContentValues contentValues = new ContentValues();

        // ID. The primary key
        contentValues.put(TableVideo._ID, video.getObjectId());

        // Title
        contentValues.put(TableVideo.TITLE, video.getTitle());

        // Description. It could be null
        String description = video.getDescription();
        if (!TextUtils.isEmpty(description)) {
            contentValues.put(TableVideo.DESCRIPTION, description);
        }

        // Video id
        contentValues.put(TableVideo.VIDEO_ID, video.getVideoId());

        // City. It could be null
        String city = video.getCity();
        if (!TextUtils.isEmpty(city)) {
            contentValues.put(TableVideo.CITY, city);
        }

        // Country. It could be null
        String country = video.getCountry();
        if (!TextUtils.isEmpty(country)) {
            contentValues.put(TableVideo.COUNTRY, country);
        }

        // Latitude and longitude.
        LatLng location = video.getPosition();
        contentValues.put(TableVideo.LATITUDE, location.latitude);
        contentValues.put(TableVideo.LONGITUDE, location.longitude);

        // HashTags
        contentValues.put(TableVideo.HASH_TAGS_LIST, video.getHashTagsAsJsonArray());

        return mDatabase.insert(TableVideo.TABLE_NAME, null, contentValues);
    }

    /**
     * Insert a list of data to database
     * @param listVideos
     * @return
     */
    public boolean insertListDataToDatabase(List<Video> listVideos) {
        boolean result = true;

        for (Video video: listVideos) {
            result &= (insertDataToDatabase(video) != -1L);
        }

        return result;
    }

    /**
     * Get the total number of videos in the database
     * @return
     *  The number of videos in the database
     */
    public long getVideosCount() {
        return DatabaseUtils.queryNumEntries(mDatabase, TableVideo.TABLE_NAME);
    }
}