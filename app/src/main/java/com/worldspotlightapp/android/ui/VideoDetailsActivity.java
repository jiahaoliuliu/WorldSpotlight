package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.IllegalFormatCodePointException;
import java.util.Observable;

public class VideoDetailsActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "VideoDetailsActivity";

    private static final String GOOGLE_API = "AIzaSyB4nkNAMhIa-JXE3lxqPyk9GaEQqhpn_Q8";

    private String mVideoObjectId;

    private ParseResponse mParseResponse;
    private Video mVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

        // Retrieve the video id from the intent
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Video.INTENT_KEY_OBJECT_ID)) {
            throw new IllegalArgumentException("You must pass the video id using intent");
        }
        mVideoObjectId = extras.getString(Video.INTENT_KEY_OBJECT_ID);

        // Retrieve the data
        mNotificationModule.showLoadingDialog(mContext);
        mVideosModule.requestVideoInfo(this, mVideoObjectId);

    }

    @Override
    protected void processDataIfExists() {
        // 1. Check if the data exists
        // If there were not data received from backend, then
        // Not do anything
        if (mParseResponse == null) {
            return;
        }

        // 2. Process the data
        if (!mParseResponse.isError()) {
            showVideoDetails();
        } else {
            // Some error happend
            mNotificationModule.showToast(mParseResponse.getHumanRedableResponseMessage(mContext), true);
            finish();
        }

        mNotificationModule.dismissLoadingDialog();

        // 3. Remove the answers
        mParseResponse = null;
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Update received from " + observable);
        if (observable instanceof VideosModuleObserver) {
            if (o instanceof VideosModuleVideoResponse) {
                VideosModuleVideoResponse videosModuleVideoResponse = (VideosModuleVideoResponse)o;
                mVideo = videosModuleVideoResponse.getVideo();
                mParseResponse = videosModuleVideoResponse.getParseResponse();

                if (mIsInForeground) {
                    processDataIfExists();
                }

                observable.deleteObserver(this);
            }
        }
    }

    /**
     * Show details about the video
     */
    private void showVideoDetails() {
        // TODO: Implement this
        Log.v(TAG, mVideo.toString());
    }
}
