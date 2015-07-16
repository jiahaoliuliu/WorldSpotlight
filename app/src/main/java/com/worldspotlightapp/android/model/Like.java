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
    public String toString() {
        return "Likes{" +
                "UserId='" + getUserId() + '\'' +
                ", VideoId='" + getVideoId() + '\'' +
                "}";
    }
}
