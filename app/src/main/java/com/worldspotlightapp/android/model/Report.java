package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * This is the basic class which resumes the report that a user has done.
 * Note that the video id is the id of the video in Parse, not the id of
 * the video in YouTube
 *
 * Created by jiahaoliuliu on 8/02/15.
 */
@ParseClassName("Report")
public class Report extends ParseObject {

    // User Id
    public static final String PARSE_TABLE_COLUMN_USER_ID = "userId";

    // Video Id
    public static final String PARSE_TABLE_COLUMN_VIDEO_ID = "videoId";

    public Report(){};

    public Report(String userId, String videoId) {
        super();
        put(PARSE_TABLE_COLUMN_USER_ID, userId);
        put(PARSE_TABLE_COLUMN_VIDEO_ID, videoId);
    }

    public String getUserId() {
        return getString(PARSE_TABLE_COLUMN_USER_ID);
    }

    public String getVideoId() {
        return getString(PARSE_TABLE_COLUMN_VIDEO_ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!(o instanceof Report)) {
            return false;
        }

        Report that = (Report) o;

        // Check the user id
        if (getUserId() != null ?
                !getUserId().equals(that.getUserId()) : that.getUserId() != null) {
            return false;
        }

        // Check the video id
        if (getVideoId() != null ?
                !getVideoId().equals(that.getVideoId()) : that.getVideoId() != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String toString() {
        return "Report{" +
                "ObjectId='" + getObjectId() + '\'' +
                ", UserId='" + getUserId() + '\'' +
                ", VideoId='" + getVideoId() + '\'' +
                "}";
    }
}
