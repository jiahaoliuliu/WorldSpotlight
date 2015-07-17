package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * This is the basic class which resumes the list of likes that a user has.
 * Note that the video id is the id of the video in Parse, not the id of
 * the video in YouTube
 *
 * Created by jiahaoliuliu on 7/16/15.
 */
@ParseClassName("Likes")
public class Like extends ParseObject {

    // User Id
    public static final String USER_ID = "userId";

    // Video Id
    public static final String VIDEO_ID = "videoId";

    public Like(){};

    public Like(String userId, String videoId) {
        super();
        put(USER_ID, userId);
        put(VIDEO_ID, videoId);
    }

    public String getUserId() {
        return getString(USER_ID);
    }

    public String getVideoId() {
        return getString(VIDEO_ID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        if (!(o instanceof Like)) {
            return false;
        }

        Like that = (Like) o;

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
        return "Likes{" +
                "ObjectId='" + getObjectId() + '\'' +
                ", UserId='" + getUserId() + '\'' +
                ", VideoId='" + getVideoId() + '\'' +
                "}";
    }
}
