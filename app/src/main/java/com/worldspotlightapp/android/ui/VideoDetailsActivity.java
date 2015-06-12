package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.LocalConstants;

import java.util.Observable;

public class VideoDetailsActivity extends AbstractBaseActivityObserver implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "VideoDetailsActivity";
    private static final int RECOVERY_DIALOG_REQUEST = 1;

    private String mVideoObjectId;

    private ParseResponse mParseResponse;
    private Video mVideo;

    // Views
    private CardView mCardView;
    private TextView mDescriptionTextView;

    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;

    // Check if it was full screen or not
    private boolean mIsFullScreen;

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

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mCardView = (CardView) findViewById(R.id.card_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment)getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        initializeYouTubePlaerFragment();

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
        Log.v(TAG, mVideo.toString());

        mActionBar.setTitle(mVideo.getTitle());
        mDescriptionTextView.setText(mVideo.getDescription());

        // If the youtube player has been already initialized
        if (mYouTubePlayer != null) {
            mYouTubePlayer.cueVideo(mVideo.getVideoId());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            mYouTubePlayer = youTubePlayer;
            mYouTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean isFullScreen) {
                    mCardView.setVisibility(isFullScreen? View.GONE : View.VISIBLE);
                    mIsFullScreen = isFullScreen;
                }
            });

            // If the video was ready
            if (mVideo != null) {
                mYouTubePlayer.cueVideo(mVideo.getVideoId());
            }
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), youTubeInitializationResult.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            initializeYouTubePlaerFragment();
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (mIsFullScreen) {
            mYouTubePlayer.setFullscreen(false);
            return;
        }
        super.onBackPressed();
    }

    private void initializeYouTubePlaerFragment() {
        if (MainApplication.IS_PRODUCTION) {
            mYoutubePlayerFragment.initialize(LocalConstants.GOOGLE_API_PRODUCTION, this);
        } else {
            mYoutubePlayerFragment.initialize(LocalConstants.GOOGLE_API_DEBUG, this);
        }
    }
}
