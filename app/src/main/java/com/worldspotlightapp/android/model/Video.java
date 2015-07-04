package com.worldspotlightapp.android.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * This is a class which contains the information of the table
 * Video in Parse. It is a subclass of the class ParseObject, so
 * the parse query could return it directly.
 * Created by jiahaoliuliu on 6/12/15.
 */
@ParseClassName("Video")
public class Video extends ParseObject implements ClusterItem {
    // Object Id
    public static final String INTENT_KEY_OBJECT_ID = "com.worldspotlightapp.android.model.video.objectid";
    public static final String PARSE_COLUMN_OBJECT_ID = "objectId";

    public static final String INTENT_KEY_TITLE = "com.worldspotlightapp.android.model.video.title";
    public static final String PARSE_COLUMN_TITLE = "title";

    public static final String INTENT_KEY_DESCRIPTION = "com.worldspotlightapp.android.model.video.description";
    public static final String PARSE_COLUMN_DESCRIPTION = "description";

    private static final String PARSE_COLUMN_VIDEO_ID = "videoId";

    private static final String VIDEO_URL_PREFIX = "http://www.worldspotlightapp.com/video/";
    private String mVideoUrl;

    private static final String PARSE_COLUMN_LOCATION = "location";
    private LatLng mLocation;

    public static final String INTENT_KEY_COUNTRY = "com.worldspotlightapp.android.model.video.country";
    public static final String PARSE_COLUMN_COUNTRY = "country";

    public static final String INTENT_KEY_CITY = "com.worldspotlightapp.android.model.video.city";
    public static final String PARSE_COLUMN_CITY = "city";

    /**
     * The thumbnail url of the video. This is generated based on the video id
     */
    public static final String INTENT_KEY_THUMBNAIL_URL = "com.worldspotlightapp.android.model.video.thumbnailUrl";
    private String mThumbnailUrl;

    // Author of the video
    private Author mAuthor;

    /**
     * The empty constructor
     */
    public Video(){}

    public String getTitle() {
        return getString(PARSE_COLUMN_TITLE);
    }

    public String getDescription() {
        return getString(PARSE_COLUMN_DESCRIPTION);
    }

    public String getVideoId() {
        return getString(PARSE_COLUMN_VIDEO_ID);
    }

    public String getCity(){ return getString(PARSE_COLUMN_CITY); }

    public String getCountry(){ return getString(PARSE_COLUMN_COUNTRY); }

    public String getThumbnailUrl() {
        String videoId = getVideoId();

        if (mThumbnailUrl == null && videoId != null) {
            mThumbnailUrl = generateThumbnailUrl(videoId);
        }

        return mThumbnailUrl;
    }

    /**
     * Generate the thumbnail url based on the video id.
     * This is an example of thumbnail url
     *     https://i.ytimg.com/vi/ECIUilEq5DM/mqdefault.jpg
     * @param videoId
     *     The id of the video to used to generate the thumbnail url
     * @return
     *     Null if videoId is null
     *     Url of the valid url if videoId is not null
     */
    private String generateThumbnailUrl(String videoId) {
        if (videoId == null) {
            return null;
        }
        return "https://i.ytimg.com/vi/" + videoId + "/mqdefault.jpg";
    }

    public ParseGeoPoint getLocation() {
        return getParseGeoPoint(PARSE_COLUMN_LOCATION);
    }

    @Override
    public LatLng getPosition() {
        if (mLocation == null) {
            ParseGeoPoint parseGeoPoint = getParseGeoPoint(PARSE_COLUMN_LOCATION);
            mLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
        }

        return mLocation;
    }

    public String getVideoUrl() {
        if (mVideoUrl == null) {
            mVideoUrl = VIDEO_URL_PREFIX + getObjectId();
        }

        return mVideoUrl;
    }

    // Author of the video


    public Author getAuthor() {
        return mAuthor;
    }

    public void setAuthor(Author author) {
        this.mAuthor = author;
    }

    public boolean hasAuthor() {
        return getAuthor() != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Video video = (Video) o;

        return getObjectId().equals(video.getObjectId());

    }

    @Override
    public int hashCode() {
        int result = mVideoUrl != null ? mVideoUrl.hashCode() : 0;
        result = 31 * result + (mLocation != null ? mLocation.hashCode() : 0);
        result = 31 * result + (mThumbnailUrl != null ? mThumbnailUrl.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Video{" +
                "title='" + getTitle() + '\'' +
                "description='" + getDescription() + '\'' +
                "city='" + getCity() + '\'' +
                "country='" + getCountry() + '\'' +
                "videoId='" + getVideoId() + '\'' +
                "thumbnailUrl='" + getThumbnailUrl() + '\'' +
                "location='" + getLocation() + '\'' +
                '}';
    }
}
