package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;

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
    public void requestVideosList(Observer observer) {

        // Register the observer
        addObserver(observer);
        // If the video list was retrieved before, don't do anything
        if (mVideosList != null) {
            Log.v(TAG, "The list of video is has been cached. Return it");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList);

            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        mVideosList = new ArrayList<Video>();

        //Retrive element from background
        final FindCallback<Video> findCallback = new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of object has been retrieved " + videosList.size());
                    mVideosList.addAll(videosList);
                    if (videosList.size() == MAX_PARSE_QUERY_ALLOWED) {
                        requestVideosToParse(mVideosList.size(), this);
                    } else {
                        VideosModuleVideosListResponse videosModuleVideosListResponse =
                                new VideosModuleVideosListResponse(parseResponse, mVideosList);

                        setChanged();
                        notifyObservers(videosModuleVideosListResponse);
                    }
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, null);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                }
            }
        };

        requestVideosToParse(0, findCallback);
    }

    /**
     * Request all the videos to parse starting from a certain position
     * @param initialPosition
     *      The initial position
     * @param findCallback
     *      The callback to call when Parse returns result
     */
    private void requestVideosToParse(
            int initialPosition,
            FindCallback<Video> findCallback) {
        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.setSkip(initialPosition);
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
        List<Video> resultVideosList = new ArrayList<Video>();

        if (mVideosList == null) {
            Log.e(TAG, "The list of video is empty");
            ParseResponse parseResponse = new ParseResponse.Builder(null).build();
            VideosModuleVideosListResponse videosModuleVideosListResponse =
                    new VideosModuleVideosListResponse(parseResponse, mVideosList);

            setChanged();
            notifyObservers(videosModuleVideosListResponse);
            return;
        }

        keyword=keyword.toLowerCase();
        for (Video video: mVideosList) {
            // By passing all the characters to lower case, we are looking for the
            // content of the string, instead of looking for Strings which has the
            // same characters in mayus and minus.
            // Looking for the title
            if (video.getTitle().toLowerCase().contains(keyword)) {
                resultVideosList.add(video);
            } else if (video.getDescription().toLowerCase().contains(keyword)) {
                resultVideosList.add(video);
            } else if (video.getCity().toLowerCase().contains(keyword) || keyword.contains(video.getCity())){
                resultVideosList.add(video);
            } else if (video.getCountry().toLowerCase().contains(keyword) || keyword.contains(video.getCountry())){
                resultVideosList.add(video);
            }
        }

        Log.v(TAG, "Number of videos find " + resultVideosList.size());

        ParseResponse parseResponse = new ParseResponse.Builder(null).build();
        VideosModuleVideosListResponse videosModuleVideosListResponse =
                new VideosModuleVideosListResponse(parseResponse, resultVideosList);

        setChanged();
        notifyObservers(videosModuleVideosListResponse);
    }
}
