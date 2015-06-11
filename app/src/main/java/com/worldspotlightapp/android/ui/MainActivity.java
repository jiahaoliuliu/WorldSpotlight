package com.worldspotlightapp.android.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private List<Video> mVideosList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Retrive element from background
        ParseQuery<Video> query = ParseQuery.getQuery(Video.class);
        query.findInBackground(new FindCallback<Video>() {
            @Override
            public void done(List<Video> videosList, ParseException e) {
                if (e == null) {
                    Log.v(TAG, "The list of object has been retrieved");
                    mVideosList = videosList;
                    for (Video video: videosList) {
                        Log.v(TAG, video.toString());
                    }
                } else {
                    Log.e(TAG, "Error retrieving data from backend");
                }
            }
        });

    }
}
