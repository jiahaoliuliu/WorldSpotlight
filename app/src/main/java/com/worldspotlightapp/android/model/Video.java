package com.worldspotlightapp.android.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;
import com.parse.ParseClassName;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
@ParseClassName("Video")
public class Video extends ParseObject implements ClusterItem {

    private static final String PARSE_COLUMN_TITLE = "title";

    private static final String PARSE_COLUMN_DESCRIPTION = "description";

    private static final String PARSE_COLUMN_VIDEO_ID = "videoId";

    private static final String PARSE_COLUMN_LOCATION = "location";
    private LatLng mLocation;

    /**
     * The thumbnail url of the video. This is generated based on the video id
     */
    private String mThumbnailUrl;

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
    public String toString() {
        return "Video{" +
                "title='" + getTitle() + '\'' +
                "description='" + getDescription() + '\'' +
                "videoId='" + getVideoId() + '\'' +
                "thumbnailUrl='" + getThumbnailUrl() + '\'' +
                "location='" + getLocation() + '\'' +
                '}';
    }

    @Override
    public LatLng getPosition() {
        if (mLocation == null) {
            ParseGeoPoint parseGeoPoint = getParseGeoPoint(PARSE_COLUMN_LOCATION);
            mLocation = new LatLng(parseGeoPoint.getLatitude(), parseGeoPoint.getLongitude());
        }

        return mLocation;
    }
}
