package com.worldspotlightapp.android.model;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    // Title
    public static final String INTENT_KEY_TITLE = "com.worldspotlightapp.android.model.video.title";
    public static final String PARSE_COLUMN_TITLE = "title";

    // Description
    public static final String INTENT_KEY_DESCRIPTION = "com.worldspotlightapp.android.model.video.description";
    public static final String PARSE_COLUMN_DESCRIPTION = "description";

    // Video id
    public static final String INTENT_KEY_VIDEO_ID = "com.worldspotlight.android.model.video.videoid";
    public static final String PARSE_COLUMN_VIDEO_ID = "videoId";

    private static final String VIDEO_URL_PREFIX = "http://www.worldspotlightapp.com/video/";
    private String mVideoUrl;

    // City
    public static final String INTENT_KEY_CITY = "com.worldspotlightapp.android.model.video.city";
    public static final String PARSE_COLUMN_CITY = "city";

    // Country
    public static final String INTENT_KEY_COUNTRY = "com.worldspotlightapp.android.model.video.country";
    public static final String PARSE_COLUMN_COUNTRY = "country";

    // Location
    public static final String PARSE_COLUMN_LOCATION = "location";
    private LatLng mLocation;
    private static final String PARSE_COLUMN_LOCATION_LATITUDE= "latitude";
    private static final String PARSE_COLUMN_LOCATION_LONGITUDE= "longitude";

    // HashTags
    public static final String PARSE_COLUMN_HASH_TAGS = "hashTags";
    public List<String> mHashTags;

    /**
     * The thumbnail url of the video. This is generated based on the video id
     */
    public static final String INTENT_KEY_THUMBNAIL_URL = "com.worldspotlightapp.android.model.video.thumbnailUrl";
    private String mThumbnailUrl;

    /**
     * Special thumbnail for the video list. It must have good quality
     */
    private String mVideoListThumbnailUrl;

    // Others
    private Gson gson;

    /**
     * The empty constructor
     */
    public Video(){
        super();
        gson = new Gson();

    }

    /**
     * Special constructor to set just the video Id to check if a
     * video is equal to another. This is used in the follow cases
     * - When add a new video. Check if the video already exists
     * @param videoId
     *      The id of the video in YouTube
     */
    public Video(String videoId) {
        this();
        setVideoId(videoId);
    }

    /**
     * Constructor to create instance of the video from the app. Note that the video id is not
     * set by now.
     *
     * @param title
     *      The title of the video
     * @param description
     *      The description of the video
     * @param videoId
     *      The id of the video in YouTube
     * @param city
     *      The city where the video was filmed
     * @param country
     *      The country where the video was filmed
     * @param position
     *      The geoposition where the video was filmed
     */
    public Video(String title, String description, String videoId, String city, String country, LatLng position, List<String> hashTags) {
        this();
        setTitle(title);
        setDescription(description);
        setVideoId(videoId);
        setCity(city);
        setCountry(country);
        setPosition(position.latitude, position.longitude);
        setHashTags(hashTags);
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
        setCountry(country);

        // Location
        JSONObject location = jsonObject.getJSONObject(PARSE_COLUMN_LOCATION);
        double latitude = location.getDouble(PARSE_COLUMN_LOCATION_LATITUDE);
        double longitude = location.getDouble(PARSE_COLUMN_LOCATION_LONGITUDE);
        setPosition(latitude, longitude);

        // HashTags
        JSONArray hashTagsJsonArray = jsonObject.getJSONArray(PARSE_COLUMN_HASH_TAGS);
        setHashTags(hashTagsJsonArray.toString());
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
        if (mLocation == null && has(PARSE_COLUMN_LOCATION)) {
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

    // HashTag
    public List<String> getHashTags() {
        if (mHashTags == null) {
            mHashTags = retrieveHashTags();
        }

        return mHashTags;
    }

    public String getHashTagsAsJsonArray() {
        if (mHashTags == null) {
            mHashTags = retrieveHashTags();
        }

        return gson.toJson(mHashTags);

    }

    public void setHashTags(List<String> hashTags) {
        // Refresh the list of hashtags
        getHashTags();

        for (String hashTag : hashTags) {
            if (!mHashTags.contains(hashTag)) {
                mHashTags.add(hashTag);
            }
        }

        put(PARSE_COLUMN_HASH_TAGS, mHashTags);
    }

    /**
     * Set the hash tags list with a hash tags list as JSON Array
     * @param jsonArrayHashTags
     *      The String which is the json array of hash tags
     */
    public void setHashTags(String jsonArrayHashTags) {
        // Refresh the list of hash tags
        getHashTags();

        // TODO: Check if it works for the list of items
        // TODO: Check if it works for a empty list
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        mHashTags = gson.fromJson(jsonArrayHashTags, type);

        put(PARSE_COLUMN_HASH_TAGS, mHashTags);
    }

    /**
     * Retrieve the list of hashtags, which is saved as json array
     */
    private List<String> retrieveHashTags() {

        JSONArray hashTagsJsonArray = getJSONArray(PARSE_COLUMN_HASH_TAGS);

        // TODO: Check if it works for the list of items
        // TODO: Check if it works for a empty list
        Type type = new TypeToken<ArrayList<String>>(){}.getType();
        List<String> hashTagsList = gson.fromJson(hashTagsJsonArray.toString(), type);

        Log.v(TAG, "The origina list of hash tags is " + hashTagsJsonArray + ", and the converted is " + hashTagsList);

//        String hashTag = null;
//        for (int i = 0; i < hashTagsJsonArray.length(); i++) {
//            try {
//                hashTag = hashTagsJsonArray.getString(i);
//                hashTagsList.add(hashTag);
//            } catch (JSONException e) {
//                Log.w(TAG, "Error getting the hash tag of the position " + i, e);
//            }
//        }

        return hashTagsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Video anotherVideo = (Video) o;

        // Check if the object id exists.
        if (anotherVideo.has(PARSE_COLUMN_OBJECT_ID)) {
            return getObjectId().equals(anotherVideo.getObjectId());
        // If the object id does not exists, check the video id
        } else {
            return getVideoId().equals(anotherVideo.getVideoId());
        }
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
                "hashTags='" + getHashTags() + '\'' +
                '}';
    }
}
