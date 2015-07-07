
package com.worldspotlightapp.android.maincontroller.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.worldspotlightapp.android.maincontroller.database.daos.VideoDao;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * Data layer of videos. This class is intended to be used to access the database of the
 * application for Video purposes, and converting from internal relational
 * representations to Java model objects. All database accesses to video operations should be
 * done through this class.
 * 
 * @author Jiahao Liu
 * 
 */
public class VideoDataLayer {
    private static final String TAG = "VideoDataLayer";

    private VideoDao mVideoDao;

    public VideoDataLayer() {
        mVideoDao = new VideoDao();
    }

    /**
     * Returns a {@link Cursor} over the Video table that contains a specific Video. If
     * the video does not exist, an empty cursor is returned.
     * 
     * @param objectId
     *            the object id of the video to retrieve.
     * @return The specific Cursor object build based on the data in the database.
     *         Null if there are not video with such id
     */
    public Video getVideoDetails(String objectId) {
        try {
            Cursor dataCursor = mVideoDao.queryData(objectId);
            return mVideoDao.getDataFromCursor(dataCursor, true);
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception while querying video with id " + objectId, e);
            return null;
        }
    }

    /**
     * Get the list of all videos from the database
     * @return
     */
    public List<Video> getListAllVideos() {
        try {
            Cursor dataCursor = mVideoDao.queryAllData();
            return mVideoDao.getListDataFromCursor(dataCursor);
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception while querying list of all videos", e);
            return null;
        }
    }

    public void insertListDataToDatabase(List<Video> videoList) {
        try {
            mVideoDao.insertListDataToDatabase(videoList);
        } catch (SQLiteException e) {
            Log.e(TAG, "Error inserting the list of videos into the database", e);
        }
    }

    public long getVideoCounts() {
        return mVideoDao.getVideosCount();
    }
}
