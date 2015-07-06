package com.worldspotlightapp.android.maincontroller.database.daos;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.jiahaoliuliu.avec.database.MainDatabase;
import com.jiahaoliuliu.avec.database.MainDatabase.TableCourse;
import com.jiahaoliuliu.avec.model.Course;
import com.jiahaoliuliu.avec.model.basictype.Price;

/**
 * Data access object for Course ({@Link Course} class).
 */

public class VideoDao {
    private SQLiteDatabase mDatabase;

    public VideoDao() {
        mDatabase = MainDatabase.getDbHelper().getReadableDatabase();
    }

    /**
     * Returns a {@link Cursor} over the course table that contains a specific Course. If
     * the course does not exist, an empty cursor is returned.
     *
     * @param id
     *            the ID of the course to retrieve.
     * @return a Cursor pointing to the course.
     * @throws SQLException
     *             if any error while accessing the database.
     */
    public Cursor queryData(long id) {
        return mDatabase.query(TableCourse.TABLE_NAME, null, TableCourse._ID + "=?",
                new String[] {
                        Long.toString(id)
                }, null, null, null);
    }

    /**
     * Given a {@link Cursor} over the course table, this method construct a data
     * {@link com.jiahaoliuliu.avec.model.Course} objects from the current position of the cursor, and returns it. Note that
     * the cursor's position will not be changed.
     *
     * @param cursor
     *            the cursor to extract {@link com.jiahaoliuliu.avec.model.Course}s from.
     *            Note that it just retrieve the first element of the cursor. The rest of the elements
     *            will be ignored
     * @return the course retrieved from the cursor.
     *         If the cursor is null, then return null
     */
    public Course dataFromCursor(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        // Ask the cursor to move to the first position.
        cursor.moveToFirst();

        Course course = new Course();
        // ID. The primary key
        course.set_id(cursor.getLong(cursor.getColumnIndex(TableCourse._ID)));

        // Name. It cannot be null
        course.setName(cursor.getString(cursor.getColumnIndex(TableCourse.NAME)));

        // Price. It could be null
        int priceColumnIndex = cursor.getColumnIndex(TableCourse.PRICE);
        if (!cursor.isNull(priceColumnIndex)) {
            course.setPrice(new Price(cursor.getInt(priceColumnIndex)));
        }

        // Image Id. It could be null
        int imageIdColumnIndex = cursor.getColumnIndex(TableCourse.IMAGE_ID);
        if (!cursor.isNull(imageIdColumnIndex)) {
            course.setImageId(cursor.getString(imageIdColumnIndex));
        }

        // Short description. It could be null
        int shortDescriptionColumnIndex = cursor.getColumnIndex(TableCourse.SHORT_DESCRIPTION);
        if (!cursor.isNull(shortDescriptionColumnIndex)) {
            course.setShortDescription(cursor.getString(shortDescriptionColumnIndex));
        }

        return course;
    }
}
