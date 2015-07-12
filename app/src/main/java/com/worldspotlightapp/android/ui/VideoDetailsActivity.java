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
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.utils.Secret;

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
    private CardView mAuthorAndLikeCardView;
    private ImageView mAuthorThumbnailImageView;
    private TextView mAuthorNameTextView;
    private ImageView mLikeImageView;

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

        // Initialize items
        mResponsesStack = new Stack<Object>();
        mPicasso = Picasso.with(mContext);
        mVideosModule.deleteObserver(this);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mAuthorAndLikeCardView = (CardView) findViewById(R.id.author_and_like_card_view);
        mAuthorThumbnailImageView = (ImageView) findViewById(R.id.author_thumbnail_image_view);
        mAuthorNameTextView = (TextView) findViewById(R.id.author_name_text_view);

        mLikeImageView = (ImageView) findViewById(R.id.like_image_view);
        mLikeImageView.setOnClickListener(onClickListener);

        mDescriptionCardView = (CardView) findViewById(R.id.description_card_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment)getSupportFragmentManager().findFragmentById(R.id.youtube_fragment);
        initializeYouTubePlayerFragment();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.like_image_view:
                    likeThisVideo();
                    break;
            }
        }
    };

    /**
     * Method used to like or unlike this video.
     * Only logged user can like a video. So, if a user has not logged in, he cannot
     * like a video.
     */
    private void likeThisVideo() {
        if (!showAlertIfUserHasNotLoggedIn(
                getString(R.string.video_details_activity_user_must_logged_in_to_like))) {
            return;
        }

        // The user has logged in
        // TODO: Like the vi
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Fill the video info if it is empty
        if (mVideo == null) {
            mVideo = mVideosModule.getVideoInfo(mVideoObjectId);

            // Finish if there is any problem with the video
            if (mVideo == null) {
                Log.e(TAG, "Error retrieving the video from the backend. The video with id " + mVideoObjectId + " no existe");
                finish();
            }
            updateVideoDetails();
            mVideosModule.requestAuthorInfo(this, mVideo.getVideoId());
        }
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
            if (response instanceof VideosModuleAuthorResponse) {
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

            if (isInForeground()) {
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
            return;
        }

        mAuthorAndLikeCardView.setVisibility(mIsFullScreen? View.GONE : View.VISIBLE);

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
                    mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN, EventId.FULL_SCREEN, mVideo.getObjectId());

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
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_PRODUCTION, this);
        } else {
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_DEBUG, this);
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
        // Tracking the user
        mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN, EventId.SHARE, mVideo.getObjectId());

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o instanceof VideoDetailsActivity;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
