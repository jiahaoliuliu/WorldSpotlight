package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.content.Context;
import android.util.Log;

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
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.database.VideoDataLayer;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.Secret;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observer;
import java.util.concurrent.ExecutorService;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleObserver extends AbstractVideosModuleObservable {

    private static final String TAG = "VideosModuleObserver";
    private static final int MAX_PARSE_QUERY_RESULT = 2000;
    private static final int MAX_PARSE_QUERY_ALLOWED = 1000;

    // The list of all the videos
    private List<Video> mVideosList;

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

        // 1. Retrieve the list of the videos from the database
        mVideosList = mVideoDataLayer.getListAllVideos();
        Log.v(TAG, mVideosList.size() + " retrieved from local database");
        // TODO: Rename ParseResponse to something else
        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        boolean areExtraVideos = false;
        VideosModuleVideosListResponse videosModuleVideosListResponse =
                new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);
        setChanged();
        notifyObservers(videosModuleVideosListResponse);

        // 2. Retrieve the rest of the videos from the parse server
        final List<Video> videosListFromParseServer = new ArrayList<Video>();
        // Callback prepared to retrieve all the videos from the parse server
        final FindCallback<Video> findDataFromParseServerCallback = new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                boolean areExtraVideos = true;
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                Log.v(TAG, "List of videos received from the parse server");
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of videos has been correctly retrieved " + videosList.size());
                    // Save the query results
                    mVideoDataLayer.insertListDataToDatabase(videosList);
                    // Add all the content to the general videos list so it will be available next time
                    mVideosList.addAll(videosList);
                    // Add the video list to the temporal video list so it could be returned to the observer
                    videosListFromParseServer.addAll(videosList);
                    if (videosList.size() == MAX_PARSE_QUERY_ALLOWED) {
                        requestVideoToParse(mVideosList.size(), this);
                    } else {
                        VideosModuleVideosListResponse videosModuleVideosListResponse =
                                new VideosModuleVideosListResponse(parseResponse, videosListFromParseServer, areExtraVideos);
                        setChanged();
                        notifyObservers(videosModuleVideosListResponse);
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

    private void requestVideoToParse(int initialPosition, FindCallback<Video> findCallback) {
        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setSkip(initialPosition);
        query.orderByAscending("updateAt");
        query.setLimit(MAX_PARSE_QUERY_RESULT);
        query.findInBackground(findCallback);
    }

    @Override
    public void requestVideoInfo(Observer observer, String videoObjectId) {
        // Register the observer
        addObserver(observer);

        // Retrieve element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.whereEqualTo(Video.PARSE_COLUMN_OBJECT_ID, videoObjectId);
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> list, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The video has been retrieved " + list);
                    if (list.size() >= 1) {
                        VideosModuleVideoResponse videosModuleVideoResponse =
                                new VideosModuleVideoResponse(parseResponse, list.get(0));

                        setChanged();
                        notifyObservers(videosModuleVideoResponse);
                    } else {
                        parseResponse =
                                new ParseResponse.Builder(e).statusCode(ParseResponse.ERROR_VIDEO_NOT_FOUND).build();
                        VideosModuleVideoResponse videosModuleVideoResponse =
                                new VideosModuleVideoResponse(parseResponse, null);

                        setChanged();
                        notifyObservers(videosModuleVideoResponse);
                    }
                } else {
                    Log.v(TAG, "There was some error retrieveing the video");

                    VideosModuleVideoResponse videosModuleVideoResponse =
                            new VideosModuleVideoResponse(parseResponse, null);

                    setChanged();
                    notifyObservers(videosModuleVideoResponse);
                }
            }
        });
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
                if (author != null) {
                    ParseResponse parseResponse = new ParseResponse.Builder(null).build();
                    VideosModuleAuthorResponse videosModuleAuthorResponse = new VideosModuleAuthorResponse(parseResponse, author);
                    setChanged();
                    notifyObservers(videosModuleAuthorResponse);
                }
            }
        }));
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
                        public void initialize(HttpRequest httpRequest) throws IOException{}
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

    /**
     * Interface created to be passed to {@link RetrieveAuthorInfoRunnable} to inform when the author
     * info is ready
     */
    private interface RequestAuthorInfoCallback {
        void done(Author author);
    }
}