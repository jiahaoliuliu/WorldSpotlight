package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleObserver extends AbstractVideosModuleObservable {

    private static final String TAG = "VideosModuleObserver";
    private static final int MAX_PARSE_QUERY_RESULT = 2000;
    private static final int MAX_PARSE_QUERY_ALLOWED = 1000;

    // The list of all the videos
    private List<Video> mVideosList;

    @Override
    public void requestAllVideos(Observer observer) {

        // Register the observer
        addObserver(observer);

        // if the video list was retrieved before, don't do anything
        if (mVideosList != null) {
            Log.v(TAG, "The list of video is has been cached. Return it");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList, true);

            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        // Callback prepared to retrieve all the videos from the parse server
        final FindCallback<Video> findDataFromParseServerCallback = new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                boolean areExtraVideos = true;
                boolean fromLocalDatabase = false;
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                Log.v(TAG, "List of videos received from the parse server");
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of videos has been correctly retrieved " + videosList.size());
                    // Cache the query results
                    ParseObject.pinAllInBackground(videosList);
                    mVideosList.addAll(videosList);
                    if (videosList.size() == MAX_PARSE_QUERY_ALLOWED) {
                        requestVideoToParse(mVideosList.size(), this, fromLocalDatabase);
                    } else {
                        VideosModuleVideosListResponse videosModuleVideosListResponse =
                                new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);

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

        // 1. Retrieve the list of videos from the local database
        final FindCallback<Video> findDataFromLocalDatabaseCallback = new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                boolean areExtraVideos = false;
                // Once the videos are retrieved from the local database, it is time to retrieve them from
                // the parse server
                boolean fromLocalDatabase = false;
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                Log.v(TAG, "Response received from the local database " + parseResponse);
                if (!parseResponse.isError()) {
                    Log.v(TAG, "Videos list received correctly " + videosList.size());
                    mVideosList = new ArrayList<Video>(videosList);
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, mVideosList, areExtraVideos);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                    // 2. Ask the backend for more videos
                    requestVideoToParse(mVideosList.size(), findDataFromParseServerCallback, fromLocalDatabase);
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, null, areExtraVideos);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                }
            }
        };

        // Start retrieving the data from the lcoal database
        boolean fromLocalDatabase = true;
        requestVideoToParse(0, findDataFromLocalDatabaseCallback, fromLocalDatabase);

    }

    private void requestVideoToParse(int initialPosition, FindCallback<Video> findCallback, boolean fromLocalDatabase) {
        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setSkip(initialPosition);
        query.orderByAscending("updateAt");

        // If it is from the local database, set it
        if (fromLocalDatabase) {
            query.fromLocalDatastore();
        // If it is not from local database, set the maximum limit
        } else {
            query.setLimit(MAX_PARSE_QUERY_RESULT);
        }
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
}
