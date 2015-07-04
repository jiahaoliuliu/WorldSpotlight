package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleVideoResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.LocalConstants;

import java.util.Observable;
import java.util.Stack;

public class VideoDetailsActivity extends AbstractBaseActivityObserver implements YouTubePlayer.OnInitializedListener {

    private static final String TAG = "VideoDetailsActivity";
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final int MENU_ITEM_SHARE_VIDEO_ID = 1000;

    private String mVideoObjectId;

    // The stack of responses from backend
    private Stack<Object> mResponsesStack;
    private Video mVideo;
    private Author mAuthor;

    // Views
    private CardView mAuthorCardView;
    private ImageView mAuthorThumbnailImageView;
    private TextView mAuthorNameTextView;

    private CardView mDescriptionCardView;
    private TextView mDescriptionTextView;

    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;

    // Check if it was full screen or not
    private boolean mIsFullScreen;

    // Others
    private Picasso mPicasso;

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

        mResponsesStack = new Stack<Object>();
        mPicasso = Picasso.with(mContext);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mAuthorCardView = (CardView) findViewById(R.id.author_card_view);
        mAuthorThumbnailImageView = (ImageView) findViewById(R.id.author_thumbnail_image_view);
        mAuthorNameTextView = (TextView) findViewById(R.id.author_name_text_view);

        mDescriptionCardView = (CardView) findViewById(R.id.description_card_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment)getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        initializeYouTubePlayerFragment();

        // Retrieve the data
        mNotificationModule.showLoadingDialog(mContext);
        mVideosModule.requestVideoInfo(this, mVideoObjectId);
    }

    @Override
    protected void processDataIfExists() {
        Log.v(TAG, "Processing data is exists");
        // 1. Check if the data exists
        // If there were not data received from backend, then
        // Not do anything
        if (mResponsesStack.isEmpty()) {
            return;
        }

        // 2. Process the data
        while(!mResponsesStack.isEmpty()) {
            Object response = mResponsesStack.pop();
            Log.v(TAG, "Response get " + response);
            if (response instanceof VideosModuleVideoResponse) {
                Log.v(TAG, "VideoModuleVideoResponse received");
                VideosModuleVideoResponse videosModuleVideoResponse = (VideosModuleVideoResponse) response;
                ParseResponse parseResponse = videosModuleVideoResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mVideo = videosModuleVideoResponse.getVideo();
                    updateVideoDetails();
                } else {
                    // Some error happened
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                    finish();
                }
            } else if (response instanceof VideosModuleAuthorResponse) {
                Log.v(TAG, "videos module author response received");
                VideosModuleAuthorResponse videosModuleAuthorResponse = (VideosModuleAuthorResponse) response;
                ParseResponse parseResponse = videosModuleAuthorResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mAuthor = videosModuleAuthorResponse.getAuthor();
                    // Ensure the follow code run on the UI thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            updateAuthorInfo();
                        }
                    });
                } else {
                    // Some error happened
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                }
            }
        }

        mNotificationModule.dismissLoadingDialog();

        // 3. Remove the responses
        // Since we are using a stack, there is not need to remove the responses
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Update received from " + observable);
        if (observable instanceof VideosModuleObserver) {

            // Add the data to the list of responses
            mResponsesStack.push(o);

            // Specific method that only could be triggered when the video is available
            if (o instanceof VideosModuleVideoResponse) {
                Log.v(TAG, "Video received");
                VideosModuleVideoResponse videosModuleVideoResponse = (VideosModuleVideoResponse) o;
                if (!videosModuleVideoResponse.getParseResponse().isError()) {
                    Log.v(TAG, "Requesting the author info");
                    mVideosModule.requestAuthorInfo(this, videosModuleVideoResponse.getVideo().getVideoId());
                }
            }

            if (mIsInForeground) {
                processDataIfExists();
            }

            // The VideoDetailsActivity will listen constantly to the changes on the video details
            //observable.deleteObserver(this);
        }
    }

    /**
     * Show details about the video
     */
    private void updateVideoDetails() {
        Log.v(TAG, mVideo.toString());

        mActionBar.setTitle(mVideo.getTitle());
        mDescriptionTextView.setText(mVideo.getDescription());

        // If the youtube player has been already initialized
        if (mYouTubePlayer != null) {
            mYouTubePlayer.cueVideo(mVideo.getVideoId());
        }
    }

    private void updateAuthorInfo() {
        Log.v(TAG, "Updating author info");
        if (mAuthor == null){
            mAuthorCardView.setVisibility(View.GONE);
            return;
        }

        mAuthorCardView.setVisibility(mIsFullScreen? View.GONE : View.VISIBLE);

        mPicasso.load(mAuthor.getThumbnailUrl()).into(mAuthorThumbnailImageView);
        mAuthorNameTextView.setText(mAuthor.getName());
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            mYouTubePlayer = youTubePlayer;
            mYouTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean isFullScreen) {
                    mDescriptionCardView.setVisibility(isFullScreen? View.GONE : View.VISIBLE);
                    mIsFullScreen = isFullScreen;

                    // Update author card view
                    updateAuthorInfo();
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
            initializeYouTubePlayerFragment();
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

    private void initializeYouTubePlayerFragment() {
        if (MainApplication.IS_PRODUCTION) {
            mYoutubePlayerFragment.initialize(LocalConstants.GOOGLE_API_PRODUCTION, this);
        } else {
            mYoutubePlayerFragment.initialize(LocalConstants.GOOGLE_API_DEBUG, this);
        }
    }

    // Action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Update user profile
        MenuItem menuItemShareVideo = menu.add(Menu.NONE, MENU_ITEM_SHARE_VIDEO_ID, Menu
                .NONE, R.string.action_bar_share)
                .setIcon(R.drawable.ic_action_share);
        menuItemShareVideo.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                onBackPressed();
                return true;
            case MENU_ITEM_SHARE_VIDEO_ID:
                shareThisVideo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareThisVideo() {
        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");

        // Subject/Title
        String subject = getString(R.string.share_subject, "\n\n" + mVideo.getTitle());
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        String videoDescription = mVideo.getDescription();
        String shareBody =
                videoDescription != null?
                        videoDescription + "\n\n" + mVideo.getVideoUrl():
                        mVideo.getVideoUrl();
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_title)));
    }

}
