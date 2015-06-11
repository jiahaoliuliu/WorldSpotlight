package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
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
                    VideosModuleResponse videosModuleResponse = new VideosModuleResponse(parseResponse, videosList);

                    setChanged();
                    notifyObservers(videosModuleResponse);
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                    VideosModuleResponse videosModuleResponse = new VideosModuleResponse(parseResponse, null);

                    setChanged();
                    notifyObservers(videosModuleResponse);
                }
            }
        });
    }
}
