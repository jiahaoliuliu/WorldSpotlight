package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideosListResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;
import java.util.Observer;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleObserver extends AbstractVideosModuleObservable {

    private static final String TAG = "VideosModuleObserver";

    @Override
    public void requestVideosList(Observer observer) {

        // Register the observer
        addObserver(observer);

        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "The list of object has been retrieved");
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, videosList);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                    VideosModuleVideosListResponse videosModuleVideosListResponse =
                            new VideosModuleVideosListResponse(parseResponse, null);

                    setChanged();
                    notifyObservers(videosModuleVideosListResponse);
                }
            }
        });
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
