package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.Observable;

public class AddAVideoActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "AddAVideoActivity";

    /**
     * The YouTube video id of the video to be added
     */
    private String mVideoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Video.INTENT_KEY_VIDEO_ID)) {
            Log.e(TAG, "You must pass the video id as extra");
            finish();
        }

        mVideoId = extras.getString(Video.INTENT_KEY_VIDEO_ID);
        Log.v(TAG, "Video id received " + mVideoId);

        setContentView(R.layout.activity_add_a_video);
    }

    @Override
    protected void processDataIfExists() {
        // TODO: Implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
    }
}
