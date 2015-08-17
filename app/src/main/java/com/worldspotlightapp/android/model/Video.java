package com.worldspotlightapp.android.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * This is a class which contains the information of the table
 * Video in Parse. It is a subclass of the class ParseObject, so
 * the parse query could return it directly.
 * Created by jiahaoliuliu on 6/12/15.
 */
@ParseClassName("Video")
public class Video extends ParseObject implements ClusterItem {

    private static final String TAG = "Video";

    // Object Id
    public static final String INTENT_KEY_OBJECT_ID = "com.worldspotlightapp.android.model.video.objectid";
    public static final String PARSE_COLUMN_OBJECT_ID = "objectId";

    public static final String INTENT_KEY_TITLE = "com.worldspotlightapp.android.model.video.title";
    public static final String PARSE_COLUMN_TITLE = "title";

    public static final String INTENT_KEY_DESCRIPTION = "com.worldspotlightapp.android.model.video.description";
    public static final String PARSE_COLUMN_DESCRIPTION = "description";

    public static final String PARSE_COLUMN_VIDEO_ID = "videoId";

    private static final String VIDEO_URL_PREFIX = "http://www.worldspotlightapp.com/video/";
    private String mVideoUrl;

    public static final String INTENT_KEY_CITY = "com.worldspotlightapp.android.model.video.city";
    public static final String PARSE_COLUMN_CITY = "city";

    public static final String INTENT_KEY_COUNTRY = "com.worldspotlightapp.android.model.video.country";
    public static final String PARSE_COLUMN_COUNTRY = "country";

    public static final String PARSE_COLUMN_LOCATION = "location";
    private LatLng mLocation;
    private static final String PARSE_COLUMN_LOCATION_LATITUDE= "latitude";
    private static final String PARSE_COLUMN_LOCATION_LONGITUDE= "longitude";

    /**
     * The thumbnail url of the video. This is generated based on the video id
     */
    public static final String INTENT_KEY_THUMBNAIL_URL = "com.worldspotlightapp.android.model.video.thumbnailUrl";
    private String mThumbnailUrl;

    /**
     * Special thumbnail for the video list. It must have good quality
     */
    private String mVideoListThumbnailUrl;

    /**
     * The empty constructor
     */
    public Video(){
        super();
    }

    /**
     * Constructor from json object. The json object are from Parse,
     * so the name of the column of each one of the fields is a field
     * in JSON Object
     * @param jsonObject
     *      The json object where to get all the fields
     */
    public Video(JSONObject jsonObject) throws JSONException {
        super();

        if (jsonObject == null) {
            throw new JSONException("The json object cannot be null");
        }

        // Object Id. This cannot be null
        String objectId = jsonObject.getString(PARSE_COLUMN_OBJECT_ID);
        setObjectId(objectId);

        // Title. This cannot be null
        String title = jsonObject.getString(PARSE_COLUMN_TITLE);
        setTitle(title);

        // Description
        String description = jsonObject.getString(PARSE_COLUMN_DESCRIPTION);
        setDescription(description);

        // Video Id
        String videoId = jsonObject.getString(PARSE_COLUMN_VIDEO_ID);
        setVideoId(videoId);

        // City
        String city = jsonObject.getString(PARSE_COLUMN_CITY);
        setCity(city);

        // Country
        String country = jsonObject.getString(PARSE_COLUMN_COUNTRY);


        // Location
        JSONObject location = jsonObject.getJSONObject(PARSE_COLUMN_LOCATION);
        double latitude = location.getDouble(PARSE_COLUMN_LOCATION_LATITUDE);
        double longitude = location.getDouble(PARSE_COLUMN_LOCATION_LONGITUDE);
        setPosition(latitude, longitude);
    }

    private void setTitle(String title) {
        if (title == null) {
            return;
        }

        put(PARSE_COLUMN_TITLE, title);
    }

    public String getTitle() {
        return getString(PARSE_COLUMN_TITLE);
    }

    private void setDescription(String description) {
        if (description == null) {
            return;
        }

        put(PARSE_COLUMN_DESCRIPTION, description);
    }

    public String getDescription() {
        return getString(PARSE_COLUMN_DESCRIPTION);
    }

    private void setVideoId(String videoId) {
        if (videoId == null) {
            return;
        }

        put(PARSE_COLUMN_VIDEO_ID, videoId);
    }

    public String getVideoId() {
        return getString(PARSE_COLUMN_VIDEO_ID);
    }

    private void setCity(String city) {
        if (city == null) {
            return;
        }

        put(PARSE_COLUMN_CITY, city);
    }

    public String getCity(){ return getString(PARSE_COLUMN_CITY); }

    private void setCountry(String country) {
        if (country == null) {
            return;
        }

        put(PARSE_COLUMN_COUNTRY, country);
    }

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

    /**
     * Get the url of the video for the video list. The quality should be good
     * @return
     *      A url of a good quality of the video for the video list
     */
    public String getVideoListThumbnailUrl() {
        String videoId = getVideoId();
        if (mVideoListThumbnailUrl == null && videoId != null) {
            mVideoListThumbnailUrl = generateVideoListThumbnailUrl(videoId);
        }

        return mVideoListThumbnailUrl;
    }

    /**
     * Generate a good quality the thumbnail url based on the video id.
     * This is an example of thumbnail url
     *     https://i.ytimg.com/vi/ECIUilEq5DM/hqdefault.jpg
     * @param videoId
     *     The id of the video to used to generate the thumbnail url
     * @return
     *     Null if videoId is null
     *     Url of the valid url if videoId is not null
     */
    private String generateVideoListThumbnailUrl(String videoId) {
        if (videoId == null) {
            return null;
        }
        return "https://i.ytimg.com/vi/" + videoId + "/maxresdefault.jpg";
    }

    private void setPosition(double latitude, double longitude) {
        ParseGeoPoint parseGeoPoint = new ParseGeoPoint(latitude, longitude);
        put(PARSE_COLUMN_LOCATION, parseGeoPoint);
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
                "objectId='" + getObjectId() + "\'" +
                "title='" + getTitle() + '\'' +
                "description='" + getDescription() + '\'' +
                "city='" + getCity() + '\'' +
                "country='" + getCountry() + '\'' +
                "videoId='" + getVideoId() + '\'' +
                "thumbnailUrl='" + getThumbnailUrl() + '\'' +
                "location='" + getPosition() + '\'' +
                '}';
    }
}
