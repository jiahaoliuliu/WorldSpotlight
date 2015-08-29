package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
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
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.database.VideoDataLayer;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAddAVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleHashTagsListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleLikedVideosListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.HashTag;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.Secret;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Observer;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

/**
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

    public VideosModuleObserver(Context context, ExecutorService executorService,
                                VideoDataLayer videoDataLayer) {
        mContext = context;
        mExecutorService = executorService;
        mVideoDataLayer = videoDataLayer;
    }

    @Override
    public void requestAllVideos(Observer observer) {

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
            Log.v(TAG, "The list of the video in the database is empty. Retrive the one saved" +
                    "in the local file");
            mVideosList = retrieveVideosListFromRawFile();
            videosListToBeAddedToTheDatabase.addAll(mVideosList);
        }

        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        boolean areExtraVideos = false;
        VideosModuleVideosListResponse videosModuleVideosListResponse =
                new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);
        setChanged();
        notifyObservers(videosModuleVideosListResponse);

        // 2. Retrieve the rest of the videos from the parse server
        // Callback prepared to retrieve all the videos from the parse server
        final FindCallback<Video> findDataFromParseServerCallback = new FindCallback<Video>() {
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
        requestVideoToParse(mVideosList.size(), findDataFromParseServerCallback);
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
        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setSkip(initialPosition);
        query.orderByAscending("updateAt");
        query.setLimit(MAX_PARSE_QUERY_RESULT);
        query.findInBackground(findCallback);
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
            if (title != null && (title.toLowerCase().contains(keyword) || keyword.contains(title))) {
                resultVideosList.add(video);
            } else if (description != null && (description.toLowerCase().contains(keyword) || keyword.contains(description))) {
                resultVideosList.add(video);
            } else if (city != null && (city.toLowerCase().contains(keyword) || keyword.contains(city))) {
                resultVideosList.add(video);
            } else if (country != null && (country.toLowerCase().contains(keyword) || keyword.contains(country))) {
                resultVideosList.add(video);
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
    public void addAVideo(final String videoId, final LatLng videoLocation) {
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
                addAVideo(videoId, title, description, videoLocation);
            }
        }));
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
                } else {
                    VideosModuleHashTagsListResponse videosModuleHashTagsListResponse =
                            new VideosModuleHashTagsListResponse(parseResponse, null);
                    setChanged();
                    notifyObservers(videosModuleHashTagsListResponse);
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
     */
    private void addAVideo(String videoId, String title, String description, LatLng videoLocation) {
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
        addAVideo(videoId, title, description, videoLocation, city, country);
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
     */
    private void addAVideo(final String videoId, String title, String description, LatLng videoLocation, String city, String country) {
        Log.v(TAG, "Adding a video with id " + videoId + ", Title: " + title + ", description " + description +
                ", location " + videoLocation + ", city " + city + ", country " + country);
        final Video video = new Video(title, description, videoId, city, country, videoLocation);
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
}