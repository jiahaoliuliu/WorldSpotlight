package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Debug;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.Preferences;
import com.worldspotlightapp.android.maincontroller.Preferences.LongId;
import com.worldspotlightapp.android.maincontroller.database.VideoDataLayer;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAddAVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleHashTagsListByVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleHashTagsListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleLikedVideosListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleUpdateVideosListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.HashTag;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.DebugOptions;
import com.worldspotlightapp.android.utils.Secret;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

/**
 * General module implementation for all the method related with videos.
 *
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleObserver extends AbstractVideosModuleObservable {

    private static final String TAG = "VideosModuleObserver";

    /*
     * This matches only once in whole input,
     * so Scanner.next returns whole InputStream as a String.
     * http://stackoverflow.com/a/5445161/2183804
     */
    private static final String REGEX_INPUT_BOUNDARY_BEGINNING = "\\A";

    private static final String JSON_FILE_RESULTS_KEY = "results";

    private static final int MAX_PARSE_QUERY_RESULT = 500;

    /**
     * The last time the videos list is updated. The value of this variable
     * matches with the last time the videos.json was updated.
     *
     */
    private static final Long LAST_VIDEOS_LIST_UPDATED_TIME = 1441411200000L;

    //Some keys for the Parse Object
    private static final String PARSE_COLUMN_CREATED_AT = "createdAt";
    private static final String PARSE_COLUMN_UPDATED_AT = "updatedAt";

    /**
     * The maximum number of results expected
     */
    private static final int MAX_PARSE_QUERY_RESULT_FOR_HASHTAG = 1000;

    // The list of all the videos
    private List<Video> mVideosList;
    // The list of all the hashTags
    private List<HashTag> mHashTagsList;

    private Context mContext;
    private ExecutorService mExecutorService;
    private VideoDataLayer mVideoDataLayer;
    private Preferences mPreferences;

    // Boolean used to detect if the hashtags
    private boolean mUpdateHashTagsListForAllVideosPending;

    public VideosModuleObserver(Context context, ExecutorService executorService,
                                VideoDataLayer videoDataLayer, Preferences preferences) {
        mContext = context;
        mExecutorService = executorService;
        mVideoDataLayer = videoDataLayer;
        mPreferences = preferences;
    }

    @Override
    public void requestAllVideos(final Observer observer) {

        Log.v(TAG, "All the videos requested from the observer " + observer);

        // Register the observer
        addObserver(observer);

        Log.v(TAG, "The number of observers after add is " + countObservers());

        // if the video list was retrieved before, don't do anything
        if (mVideosList != null) {
            Log.v(TAG, "The list of video is has been cached. Return it");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList, false);

            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        // The list of videos that should be added into the database
        final List<Video> videosListToBeAddedToTheDatabase = new ArrayList<Video>();

        // 1. Retrieve the list of the videos from the database
        mVideosList = mVideoDataLayer.getListAllVideos();
        Log.v(TAG, mVideosList.size() + " retrieved from local database");

        // If the list of videos is empty, retrieve the list of elements from row file
        // and save them into the database
        if (mVideosList.isEmpty()) {
            Log.v(TAG, "The list of the video in the database is empty. Retrieve the ones saved" +
                    "in the local file");
            mVideosList = retrieveVideosListFromRawFile();
            videosListToBeAddedToTheDatabase.addAll(mVideosList);
            // TODO: Remove this
//            Log.v(TAG, "All the videos has been retrieved. Save the needed to the database");
//            saveVideosListToDatabase(videosListToBeAddedToTheDatabase);
            // Remove the list of videos to be added to the database since they are already
            // added
//            videosListToBeAddedToTheDatabase.clear();
        }

        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        boolean areExtraVideos = false;
        VideosModuleVideosListResponse videosModuleVideosListResponse =
                new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);
        setChanged();
        notifyObservers(videosModuleVideosListResponse);

        // 2. Retrieve the rest of the videos from the parse server
        // Callback prepared to retrieve all the videos from the parse server
        final FindCallback<Video> updateVideoIndexFromParseServerCallback = new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                boolean areExtraVideos = true;
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                Log.v(TAG, "List of videos received from the parse server");
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of videos has been correctly retrieved " + videosList.size());
                    // Add all the content to the general videos list so it will be available next time
                    mVideosList.addAll(videosList);

                    // Save the list to be added to the database later
                    videosListToBeAddedToTheDatabase.addAll(videosList);

                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, videosList, areExtraVideos);
                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);

                    // If parse has returned the max number of results, that means there are more
                    // video available. So, request more videos
                    if (videosList.size() == MAX_PARSE_QUERY_RESULT) {
                        Log.v(TAG, MAX_PARSE_QUERY_RESULT + " videos retrieved. Requesting for more");
                        requestVideoToParse(mVideosList.size(), this);
                    } else {
                        Log.v(TAG, "All the videos has been retrieved. Save the needed to the database");
                        saveVideosListToDatabase(videosListToBeAddedToTheDatabase);

                        // Ask parse to update the list of videos
                        SyncVideoInfo(observer);

                        // Print the possible hashtags only not in production
                        if (!DebugOptions.IS_PRODUCTION) {
//                            printPossibleHashTagsFromTheVideo();

                            // Update automatically the hashtags if it is not ready
                            if (mHashTagsList == null) {
                                mUpdateHashTagsListForAllVideosPending = true;
                            } else {
                                updateHashTagsListForAllVideos();
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, null, areExtraVideos);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                }
            }
        };
        requestVideoToParse(mVideosList.size(), updateVideoIndexFromParseServerCallback);
    }

    @Override
    public void requestLikedVideosInfo(Observer observer, List<Like> likesList) {
        // Register the observer
        addObserver(observer);

        List<Video> likedVideos = new ArrayList<Video>();
        for (Like like : likesList) {
            likedVideos.add(getVideoInfo(like.getVideoId()));
        }

        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        VideosModuleLikedVideosListResponse videosModuleLikedVideosListResponse = new VideosModuleLikedVideosListResponse(parseResponse, likedVideos);
        setChanged();
        notifyObservers(videosModuleLikedVideosListResponse);
    }

    private void requestVideoToParse(int initialPosition, FindCallback<Video> findCallback) {
        Log.v(TAG, "Requesting videos to parse. The initial position is " + initialPosition);
        //Retrieve element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setSkip(initialPosition);
        query.orderByAscending(PARSE_COLUMN_CREATED_AT);
        query.setLimit(MAX_PARSE_QUERY_RESULT);
        query.findInBackground(findCallback);
    }

    /**
     * Request a list of video udpated since the last time
     */
    @Override
    public void SyncVideoInfo(Observer observer) {
        addObserver(observer);

        // Check the last updated time
        final Long lastUpdatedTime =
                mPreferences.contains(LongId.VIDEOS_LIST_LAST_UPDATE_TIME) ?
                    mPreferences.get(LongId.VIDEOS_LIST_LAST_UPDATE_TIME) :
                    LAST_VIDEOS_LIST_UPDATED_TIME;

        ParseQuery<Video> requestVideosUpdatedSinceLastTimeQuery = ParseQuery.getQuery(Video.class);
        requestVideosUpdatedSinceLastTimeQuery.whereGreaterThan(PARSE_COLUMN_UPDATED_AT, new Date(lastUpdatedTime));
        requestVideosUpdatedSinceLastTimeQuery.setLimit(MAX_PARSE_QUERY_RESULT);
        requestVideosUpdatedSinceLastTimeQuery.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosListToBeUpdated, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "List of video to be updated received " + videosListToBeUpdated.size());
                    // Update the last updated time
                    mPreferences.set(LongId.VIDEOS_LIST_LAST_UPDATE_TIME, new Date().getTime());

                    // Update the internal list
                    for (Video video : videosListToBeUpdated) {
                        // Only do it if the video list contains it
                        if (mVideosList.contains(video)) {
                            Video videoToBeUpdated = mVideosList.get(mVideosList.indexOf(video));
                            videoToBeUpdated.update(video);
                        }

                        // Update the database
                        mVideoDataLayer.updateVideo(video);
                    }

                    // Notify only if the list of updated
                    if (videosListToBeUpdated.size() > 0) {
                        VideosModuleUpdateVideosListResponse videosModuleUpdateVideosListResponse =
                                new VideosModuleUpdateVideosListResponse(parseResponse, videosListToBeUpdated);
                        setChanged();
                        notifyObservers(videosModuleUpdateVideosListResponse);
                    }
                } else {
                    Log.v(TAG, "Error retrieving the list of videos updated");
                    // TODO: create retry policy
                }
            }
        });
    }

    @Override
    public Video getVideoInfo(String videoObjectId) {
        // Get the video directly from the list of videos in the database
        // The database could be not ready at beginning
        Video videoInfo = mVideoDataLayer.getVideoDetails(videoObjectId);

        if (videoInfo != null) {
            return videoInfo;
        }

        Log.w(TAG, "Video not found in the database. Looking it in the temporal memory");
        // The video info is null, try to get the data from the memory list
        if (mVideosList != null) {
            for (Video video: mVideosList) {
                if (video.getObjectId().equals(videoObjectId)) {
                    Log.v(TAG, "Video found in the temporal memory " + video);
                    return video;
                }
            }
        }

        return null;
    }

    @Override
    public void searchByKeyword(Observer observer, String keyword) {
        // Register the observer
        addObserver(observer);

        if (mVideosList == null) {
            Log.e(TAG, "The list of video is empty");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            // In case of error, do not update the existent list of videos
            boolean areExtraVideos = true;
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);

            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        if (keyword == null || keyword.isEmpty()) {
            Log.e(TAG, "The keyword is empty or null");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            // In case of error, do not update the existent list of videos
            boolean areExtraVideos = true;
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);
            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        List<Video> resultVideosList = new ArrayList<Video>();
        keyword = keyword.toLowerCase();
        for (Video video : mVideosList) {
            // By passing all the characters to lower case, we are looking for the
            // content of the string, instead of looking for Strings which has the
            // same characters in mayus and minus.
            // Looking for the title
            String title = video.getTitle();
            String description = video.getDescription();
            String city = video.getCity();
            String country = video.getCountry();
            ArrayList<String> hashTagsList = video.getHashTags();

            if (!TextUtils.isEmpty(title) && (title.toLowerCase().contains(keyword) || keyword.contains(title))) {
                resultVideosList.add(video);
            } else if (!TextUtils.isEmpty(description) && (description.toLowerCase().contains(keyword) || keyword.contains(description))) {
                resultVideosList.add(video);
            } else if (!TextUtils.isEmpty(city)&& (city.toLowerCase().contains(keyword) || keyword.contains(city))) {
                resultVideosList.add(video);
            } else if (!TextUtils.isEmpty(country) && (country.toLowerCase().contains(keyword) || keyword.contains(country))) {
                resultVideosList.add(video);
            // For other fields
            } else {
                // HashTags
                for (String hashTag : hashTagsList) {
                    if (hashTag.toLowerCase().contains(keyword) || keyword.contains(hashTag.toLowerCase())) {
                        resultVideosList.add(video);
                        break;
                    }
                }
            }
        }

        Log.v(TAG, "Number of videos find " + resultVideosList.size());

        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        boolean areExtraVideos = false;
        VideosModuleVideosListResponse videosModuleVideosListResponse =
                new VideosModuleVideosListResponse(parseResponse, resultVideosList, areExtraVideos);

        setChanged();
        notifyObservers(videosModuleVideosListResponse);
    }

    @Override
    public void requestAuthorInfo(Observer observer, final String videoId) {
        addObserver(observer);
        mExecutorService.execute(new RetrieveAuthorInfoRunnable(videoId, new RequestAuthorInfoCallback() {
            @Override
            public void done(Author author) {
                // In case that the author has some problems, just don't do anything
                if (author != null) {
                    ParseResponse parseResponse = new ParseResponse.Builder(null).build();
                    VideosModuleAuthorResponse videosModuleAuthorResponse = new VideosModuleAuthorResponse(parseResponse, author, videoId);
                    setChanged();
                    notifyObservers(videosModuleAuthorResponse);
                }
            }
        }));
    }

    @Override
    public void addAVideo(final String videoId, final LatLng videoLocation, final ArrayList<String> hashTagsList) {
        Log.v(TAG, "Adding a video with id " + videoId + ", location " + videoLocation);

        boolean videoAlreadyExists = false;
        Video videoToBeAdded = new Video(videoId);
        // Check if the video already existed
        if (mVideosList != null && !mVideosList.isEmpty()) {
            // If the video already exists
            if (mVideosList.contains(videoToBeAdded)) {
                videoAlreadyExists = true;
            }
        // Check the database
        } else {
            if (mVideoDataLayer.hasVideoByVideoId(videoId)) {
                videoAlreadyExists = true;
            }
        }

        // The video already exists
        if (videoAlreadyExists) {
            Log.w(TAG, "The video with video id " + videoId + " already exists.");
            ParseResponse parseResponse = new ParseResponse.Builder(null).statusCode(ParseResponse.ERROR_ADDING_AN_EXISTING_VIDEO).build();
            VideosModuleAddAVideoResponse videosModuleAddAVideoResponse = new VideosModuleAddAVideoResponse(parseResponse, null);
            setChanged();
            notifyObservers(videosModuleAddAVideoResponse);
            return;
        }

        // Get the information about the video
        //      Get the title and description
        mExecutorService.execute(new RetrieveTitleDescriptionRunnable(videoId, new RequestTitleAndDescriptionCallback() {
            @Override
            public void done(String title, String description) {
                addAVideo(videoId, title, description, videoLocation, hashTagsList);
            }
        }));
    }

    @Override
    public void requestHashTagsListForAVideo(Observer observer, final String videoObjectId) {
        Log.v(TAG, "Requesting the hash tags list for the video " + videoObjectId);
        // Add the observer
        addObserver(observer);

        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.getInBackground(videoObjectId, new GetCallback<Video>() {
            @Override
            public void done(Video video, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                Log.v(TAG, "Parse response received " + parseResponse);
                if (!parseResponse.isError()) {
                    Log.v(TAG, "Object correctly retrieved " + video);
                    VideosModuleHashTagsListByVideoResponse videosModuleHashTagsListByVideoResponse =
                            new VideosModuleHashTagsListByVideoResponse(parseResponse, video.getHashTags(), videoObjectId);

                    setChanged();
                    notifyObservers(videosModuleHashTagsListByVideoResponse);
                } else {
                    Log.e(TAG, "Error retrieving the video details");
                    VideosModuleHashTagsListByVideoResponse videosModuleHashTagsListByVideoResponse =
                            new VideosModuleHashTagsListByVideoResponse(parseResponse, null, videoObjectId);

                    setChanged();
                    notifyObservers(videosModuleHashTagsListByVideoResponse);
                }
            }
        });
    }

    @Override
    public void requestAllHashTags(Observer observer) {
        // Register the observer
        addObserver(observer);

        // Return cached list if any
        if (mHashTagsList != null) {
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            VideosModuleHashTagsListResponse videosModuleHashTagsListResponse =
                    new VideosModuleHashTagsListResponse(parseResponse, mHashTagsList);
            setChanged();
            notifyObservers(videosModuleHashTagsListResponse);
            return;
        }

        //Retrieve element from background
        ParseQuery<HashTag> query = ParseQuery.getQuery(HashTag.class);
        query.orderByAscending(HashTag.PARSE_TABLE_COLUMN_HASH_TAG);
        query.setLimit(MAX_PARSE_QUERY_RESULT_FOR_HASHTAG);
        query.findInBackground(new FindCallback<HashTag>() {
            @Override
            public void done(List<HashTag> hashTagsList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    mHashTagsList = hashTagsList;
                    VideosModuleHashTagsListResponse videosModuleHashTagsListResponse =
                            new VideosModuleHashTagsListResponse(parseResponse, mHashTagsList);
                    setChanged();
                    notifyObservers(videosModuleHashTagsListResponse);

                    // Update the list of hashtags in the videos. Only if it is not in production
                    if (
                        !DebugOptions.IS_PRODUCTION &&
                        mUpdateHashTagsListForAllVideosPending
                            ) {
                        updateHashTagsListForAllVideos();
                    }
                } else {
                    VideosModuleHashTagsListResponse videosModuleHashTagsListResponse =
                            new VideosModuleHashTagsListResponse(parseResponse, null);
                    setChanged();
                    notifyObservers(videosModuleHashTagsListResponse);
                }
            }
        });
    }

    @Override
    public void updateHashTagsList(Observer observer, final String videoObjectId, ArrayList<String> hashTagsList) {
        if (TextUtils.isEmpty(videoObjectId)) {
            Log.e(TAG, "The video object id cannot be null");
            return;
        }

        // Update the hashtag for the database
        final Video video = mVideoDataLayer.getVideoDetails(videoObjectId);

        // The video shouldn't be null
        if (video == null) {
            Log.e(TAG, "The video with id " + videoObjectId + " does not exists in the databse.");
            return;
        }

        video.setHashTags(hashTagsList);
        mVideoDataLayer.updateVideo(video);

        // Update the hashtag for the backend
        video.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "Video " + video + " saved correctly");
                } else {
                    Log.e(TAG, "Error saving the video " + video);
                }
            }
        });
    }

    /**
     * Special method used internally to add specific hash tags. This is useful when the hashtag is added automatically
     *
     * @param videoObjectId
     *      The object id of the video where the hash  tag should be added
     * @param hashTagToBeAdded
     *      The hashtag to be added
     */
    private void addHashTag(String videoObjectId, String hashTagToBeAdded) {
        if (TextUtils.isEmpty(videoObjectId)) {
            Log.e(TAG, "The video object id cannot be null");
            return;
        }

        // Update the hashtag for the database
        final Video video = mVideoDataLayer.getVideoDetails(videoObjectId);

        // The video shouldn't be null
        if (video == null) {
            Log.e(TAG, "The video with id " + videoObjectId + " does not exists in the databse.");
            return;
        }

        ArrayList<String> hashTags = video.getHashTags();
        if (hashTags.contains(hashTagToBeAdded)) {
            Log.v(TAG, "The hash tag already exists for this video");
            return;
        }

        hashTags.add(hashTagToBeAdded);
        video.setHashTags(hashTags);
        mVideoDataLayer.updateVideo(video);

        // Update the hashtag for the backend
        video.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "Video " + video + " saved correctly");
                } else {
                    Log.e(TAG, "Error saving the video " + video);
                }
            }
        });

    }

    /**
     * Add a video into the backend. This method might not be running in the main thread
     * @param videoId
     *      The id of the video to be added
     * @param title
     *      The title of the video to be added
     * @param description
     *      The description of the video to be added. Since this is retrieved from YouTube Data API, it does not contain
     *      all the information
     * @param videoLocation
     *      The location where the video was filmed
     * @param hashTagsList
     *      The list of hash tags
     */
    private void addAVideo(String videoId, String title, String description, LatLng videoLocation, ArrayList<String> hashTagsList) {
        Log.v(TAG, "Adding a video with id " + videoId + ", Title: " + title + ", description " + description +
                ", location " + videoLocation);

        String city = "";
        String country = "";
        // Get the city and the country
        Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
        List<Address> addressesList = null;
        try {
            addressesList = geocoder.getFromLocation(videoLocation.latitude, videoLocation.longitude, 1);
        } catch (IOException ioException) {
            // Service not available
            Log.e(TAG, "Error getting the city from the geocoder. Service not available", ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Invalid latitude and longitude
            Log.e(TAG, "Error getting the city form the geocoder. The latitude or/and the longitude are" +
                    "not correct. " + videoLocation.latitude + ", " + videoLocation.longitude + ".", illegalArgumentException);
        }

        // If there was not address retrieved
        if (addressesList != null && !addressesList.isEmpty()) {
            Address address = addressesList.get(0);
            Log.v(TAG, "Address found " + address);
            Log.v(TAG, "The locality is " + address.getLocality());
            Log.v(TAG, "The country is " + address.getCountryName());

            // Set the city if exists
            String locality = address.getLocality();
            if (locality != null) {
                city = locality;
            }

            // Set the country if exists
            String countryName = address.getCountryName();
            if (countryName != null) {
                country = countryName;
            }
        }
        addAVideo(videoId, title, description, videoLocation, city, country, hashTagsList);
    }

    /**
     * Method used to save a video to the backend. The video Id shouldn't exist before.
     * @param videoId
     *      The id of the video in YouTube
     * @param title
     *      The title of the video
     * @param description
     *      The description of the video
     * @param videoLocation
     *      The location where the video was filmed
     * @param city
     *      The city where the video was filmed
     * @param country
     *      The country where the video was filmed.
     * @param hashTagsList
     *      The list of hash tags
     */
    private void addAVideo(final String videoId, String title, String description, LatLng videoLocation, String city, String country, final ArrayList<String> hashTagsList) {
        Log.v(TAG, "Adding a video with id " + videoId + ", Title: " + title + ", description " + description +
                ", location " + videoLocation + ", city " + city + ", country " + country);
        final Video video = new Video(title, description, videoId, city, country, videoLocation, hashTagsList);

        video.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "Video " + video + " added correctly to the backend");
                    // Update the video list
                    mVideosList.add(video);
                    // The database should be updated according to the backend. This is because there could be several user adding
                    // videos at the same time. So, the order the video was added are different. Since we based on the number of existence
                    // videos in the database to update the list of videos, it is more safe do it asking directly to Parse.


                    VideosModuleAddAVideoResponse videosModuleAddAVideoResponse = new VideosModuleAddAVideoResponse(parseResponse, video);
                    setChanged();
                    notifyObservers(videosModuleAddAVideoResponse);
                } else {
                    Log.e(TAG, "Error adding video " + video + " to the backend. ", e);
                    VideosModuleAddAVideoResponse videosModuleAddAVideoResponse = new VideosModuleAddAVideoResponse(parseResponse, null);
                    setChanged();
                    notifyObservers(videosModuleAddAVideoResponse);
                }
            }
        });
    }

    /**
     * Interface created to be passed to {@link RetrieveAuthorInfoRunnable} to inform when the author
     * info is ready
     */
    private interface RequestTitleAndDescriptionCallback {
        void done(String title, String description);
    }

    private class RetrieveTitleDescriptionRunnable implements Runnable {

        private String mVideoId;
        private RequestTitleAndDescriptionCallback mRequestTitleAndDescriptionCallback;

        public RetrieveTitleDescriptionRunnable(String videoId, RequestTitleAndDescriptionCallback requestTitleAndDescriptionCallback) {
            mVideoId = videoId;
            mRequestTitleAndDescriptionCallback = requestTitleAndDescriptionCallback;
        }

        @Override
        public void run() {
            // Set empty title and description as default
            String title = "";
            String description = "";

            YouTube youtube =
                    new YouTube.Builder(new NetHttpTransport(),
                            new JacksonFactory(), new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest httpRequest) throws IOException {
                        }
                    }).setApplicationName(mContext.getString(R.string.app_name)).build();

            try {
                YouTube.Search.List query = youtube.search().list("id,snippet");
                query.setKey(Secret.YOUTUBE_DATA_API_KEY);
                query.setType("video");
                query.setFields("items(id,snippet/title,snippet/description)");
                query.setQ("v=" + mVideoId);
                query.setType("video");
                SearchListResponse response = query.execute();
                List<SearchResult> results = response.getItems();
                Log.v(TAG, "List of search results retrieved. " + results.size());
                for (final SearchResult searchResult : results) {
                    Log.v(TAG, searchResult.toPrettyString());
                    if (!searchResult.getId().getVideoId().equals(mVideoId)) {
                        continue;
                    }

                    // Get the title and description
                    title = searchResult.getSnippet().getTitle();
                    description = searchResult.getSnippet().getDescription();
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not search video data: ", e);
            }

            mRequestTitleAndDescriptionCallback.done(title, description);
        }
    }

    /**
     * Interface created to be passed to {@link RetrieveAuthorInfoRunnable} to inform when the author
     * info is ready
     */
    private interface RequestAuthorInfoCallback {
        void done(Author author);
    }

    private class RetrieveAuthorInfoRunnable implements Runnable {

        private String mVideoId;
        private RequestAuthorInfoCallback mRequestAuthorInfoCallback;

        public RetrieveAuthorInfoRunnable(String videoId, RequestAuthorInfoCallback requestAuthorInfoCallback) {
            mVideoId = videoId;
            mRequestAuthorInfoCallback = requestAuthorInfoCallback;
        }

        @Override
        public void run() {
            Author author = null;

            YouTube youtube =
                    new YouTube.Builder(new NetHttpTransport(),
                            new JacksonFactory(), new HttpRequestInitializer() {
                        @Override
                        public void initialize(HttpRequest httpRequest) throws IOException {
                        }
                    }).setApplicationName(mContext.getString(R.string.app_name)).build();

            try {
                YouTube.Search.List query = youtube.search().list("id,snippet");
                query.setKey(Secret.YOUTUBE_DATA_API_KEY);
                query.setType("video");
                query.setFields("items(id/videoId,snippet/channelId,snippet/channelTitle)");
                query.setQ("v=" + mVideoId);
                query.setType("video");
                SearchListResponse response = query.execute();
                List<SearchResult> results = response.getItems();
                Log.v(TAG, "List of search results retrieved. " + results.size());
                for (final SearchResult searchResult : results) {
                    Log.v(TAG, searchResult.toPrettyString());
                    if (!searchResult.getId().getVideoId().equals(mVideoId)) {
                        continue;
                    }

                    // Get the channel id
                    YouTube.Channels.List queryChannel = youtube.channels().list("id, snippet");
                    queryChannel.setKey(Secret.YOUTUBE_DATA_API_KEY);
                    queryChannel.setFields("items(id,snippet/thumbnails/medium,snippet/title)");
                    queryChannel.setId(searchResult.getSnippet().getChannelId());
                    ChannelListResponse channelListResponse = queryChannel.execute();
                    List<Channel> channelsList = channelListResponse.getItems();
                    Log.v(TAG, "List of channels retrieved " + channelsList);
                    for (final Channel channel : channelsList) {
                        Log.v(TAG, channel.toPrettyString());
                        author = new Author(channel.getId(), channel.getSnippet().getTitle(), channel.getSnippet().getThumbnails().getMedium().getUrl());
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "Could not search video data: ", e);
            }

            mRequestAuthorInfoCallback.done(author);
        }
    }

    private List<Video> retrieveVideosListFromRawFile() {
        InputStream inputStream = mContext.getResources().openRawResource(R.raw.videos);
        List<Video> videosList = new ArrayList<Video>();
        String json = new Scanner(inputStream).useDelimiter(REGEX_INPUT_BOUNDARY_BEGINNING).next();

        // TODO: Replace the follow code with gson
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray jsonArray = jsonObject.getJSONArray(JSON_FILE_RESULTS_KEY);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject videoJsonObject = jsonArray.getJSONObject(i);
                try {
                    Video video = new Video(videoJsonObject);
                    videosList.add(video);
                } catch (JSONException jsonException) {
                    Log.e(TAG, "Error create a vide from json object " + jsonObject, jsonException);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error reading video file", e);
        }
        Log.v(TAG, "The size of the video retrieved from json file is " + videosList.size());
        return videosList;
    }

    /**
     * Save the list of the videos into the database
     * @param videosList
     */
    private void saveVideosListToDatabase(List<Video> videosList) {
        // Execute it in another thread
        mExecutorService.execute(new SaveVideosListToDatabaseRunnable(videosList));
    }

    private class SaveVideosListToDatabaseRunnable implements Runnable {

        private List<Video> mVideosList;

        public SaveVideosListToDatabaseRunnable(List<Video> videosList) {
            super();
            this.mVideosList = videosList;
        }

        @Override
        public void run() {
            mVideoDataLayer.insertListDataToDatabase(mVideosList);
        }
   }

    /**
     * Print all the possible hashtags from the list of the actual videos
     */
    private void printPossibleHashTagsFromTheVideo() {
        // Get the list of hashtags
        HashMap<String, Integer> hashTagsMap = new HashMap<String, Integer>();

        for (Video video: mVideosList) {
            // Get the title
            String title = video.getTitle();
            if (!TextUtils.isEmpty(title)) {
                String[] titles = title.split(" ");
                addWordsToHashMap(hashTagsMap, titles);
            }

            // Ge the description
            String description = video.getDescription();
            if (!TextUtils.isEmpty(description)) {
                String[] descriptions = description.split(" ");
                addWordsToHashMap(hashTagsMap, descriptions);
            }
        }

        // Sort the content
        Map<String, Integer> hashTagsMapSorted = sortHashTagsMap(hashTagsMap);

        // Print the content
        printMap(hashTagsMapSorted);

    }

    private void addWordsToHashMap(HashMap<String, Integer> hashTagsMap, String[] words) {
        for (String word : words) {
            String lowerCaseWord = word.toLowerCase();
            if (hashTagsMap.containsKey(lowerCaseWord)) {
                Integer counter = hashTagsMap.get(lowerCaseWord);
                counter++;
                hashTagsMap.put(lowerCaseWord, counter);
            } else {
                hashTagsMap.put(lowerCaseWord, 1);
            }
        }
    }

    private Map<String, Integer> sortHashTagsMap(HashMap<String, Integer> hashTagsMap) {
        // Convert map to list
        List<Map.Entry<String, Integer>> list =
                new LinkedList<Map.Entry<String, Integer>>(hashTagsMap.entrySet());

        // Sort list with comparator, to compare the map values
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });

        // Convert sorted mpa back to a map
        Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext();) {
            Map.Entry<String, Integer> entry = it.next();
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }

    /**
     * Print the content of a Map, with key and value
     * @param map
     *      The content of the map to be printed
     */
    private void printMap(Map<String, Integer> map) {
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            Log.v(TAG, entry.getKey() + ":" + entry.getValue());
        }
    }

    /**
     * Update the hash tags list for all the videos.
     * Precondition
     * - The list of the videos cannot be empty
     * - The list of the hash tags cannot be empty
     */
    private void updateHashTagsListForAllVideos() {
        if (mHashTagsList == null) {
            Log.e(TAG, "Trying to update the hash tags list for all the videos but the hash tags list is empty");
            return;
        }

        if (mVideosList == null || mVideosList.isEmpty()) {
            Log.e(TAG, "Trying to update the hash tags list for all the videos but the videos list is empty");
            return;
        }

        // Check for each video
        for (Video video :mVideosList) {
            Log.d(TAG, "##############################################################################");
            Log.d(TAG, "Checking for the video " + video);
            for (HashTag hashTag : mHashTagsList) {
                Log.d(TAG, "Checking for the hashtag " + hashTag.getName());
                Log.d(TAG, "-----------------------------------------------------------------------------");
                if (video.shouldAddHashTag(hashTag)) {
                    Log.d(TAG, "The hashtag should be added");
                    addHashTag(video.getObjectId(), hashTag.getName());
                }
            }
        }

        mUpdateHashTagsListForAllVideosPending = false;
    }
}