
package com.worldspotlightapp.android.maincontroller.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.jiahaoliuliu.avec.database.daos.CourseDao;
import com.jiahaoliuliu.avec.model.Course;

/**
 * Data layer of videos. This class is intended to be used to access the database of the
 * application for Video purposes, and converting from internal relational
 * representations to Java model objects. All database accesses to course operations should be
 * done through this class.
 * 
 * @author Jiahao Liu
 * 
 */
public class VideoDataLayer {
    private static final String TAG = "VideoDataLayer";

    private CourseDao mCourseDao;

    private VideoDataLayer() {
        mCourseDao = new CourseDao();
    }

    /**
     * SingletonHolder is loaded on the first execution of Singleton.getInstance() or the first
     * access to SingletonHolder.INSTANCE, not before.
     */
    private static class SingletonHolder {
        public static final CourseDataLayer INSTANCE = new CourseDataLayer();
    }

    /**
     * Returns the only instance of this class.
     */
    public static CourseDataLayer getInstance() {
        return SingletonHolder.INSTANCE;
    }

    /**
     * Returns a {@link Cursor} over the Course table that contains a specific Course. If
     * the course does not exist, an empty cursor is returned.
     * 
     * @param courseId
     *            the ID of the course to retrieve.
     * @return The specific Cursor object build based on the data in the database.
     *         Null if there are not course with such id
     */
    public Course getCourse(long courseId) {
        try {
            Cursor dataCursor = mCourseDao.queryData(courseId);
            return mCourseDao.dataFromCursor(dataCursor);
        } catch (SQLiteException e) {
            Log.e(TAG, "Exception while querying course with id " + courseId, e);
            return null;
        }
    }
}
