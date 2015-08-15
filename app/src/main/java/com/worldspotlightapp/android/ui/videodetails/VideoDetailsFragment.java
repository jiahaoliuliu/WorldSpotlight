package com.worldspotlightapp.android.ui.videodetails;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.utils.Secret;

import java.util.Stack;

public class VideoDetailsFragment extends Fragment implements YouTubePlayer.OnInitializedListener {
    private static final String TAG = "VideoDetailsFragment";

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private String mVideoObjectId;

    // The stack of responses from backend
    private Stack<Object> mResponsesStack;
    private Video mVideo;
    private Author mAuthor;

    private Activity mAttachedActivity;

    // Views
    private CardView mExtraInfoCardView;
    private ImageView mAuthorThumbnailImageView;
    private TextView mAuthorNameTextView;
    private ImageView mReportAVideoImageView;
    private ImageView mLikeImageView;

    private CardView mDescriptionCardView;
    private TextView mDescriptionTextView;

    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;

    // Check if it was full screen or not
    private boolean mIsFullScreen;

    // Check if the screen orientation is landscape or not
    private boolean mIsLandscape;

    // Others
    private Picasso mPicasso;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param videoObjectId The object id of the video
     * @return A new instance of fragment VideoDetailsFragment.
     */
    public static VideoDetailsFragment newInstance(String videoObjectId) {
        VideoDetailsFragment fragment = new VideoDetailsFragment();
        Bundle args = new Bundle();
        args.putString(Video.INTENT_KEY_OBJECT_ID, videoObjectId);
        fragment.setArguments(args);
        return fragment;
    }

    public VideoDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle arguments = getArguments();

        if (arguments == null || !arguments.containsKey(Video.INTENT_KEY_OBJECT_ID)) {
            throw new IllegalArgumentException("You must pass the video object id as argument");
        }

        mVideoObjectId = arguments.getString(Video.INTENT_KEY_OBJECT_ID);
        Log.v(TAG, "Video object id received " + mVideoObjectId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set the content of the view
        LinearLayout videoDetailsFragmentLineraLayout = (LinearLayout)inflater.inflate(R.layout.fragment_video_details, container,
                false);

        // Link the views
        mExtraInfoCardView = (CardView) videoDetailsFragmentLineraLayout.findViewById(R.id.extra_info_card_view);
        mAuthorThumbnailImageView = (ImageView) videoDetailsFragmentLineraLayout.findViewById(R.id.author_thumbnail_image_view);
        mAuthorNameTextView = (TextView) videoDetailsFragmentLineraLayout.findViewById(R.id.author_name_text_view);

        mLikeImageView = (ImageView) videoDetailsFragmentLineraLayout.findViewById(R.id.like_image_view);
        mLikeImageView.setOnClickListener(onClickListener);

        mReportAVideoImageView = (ImageView) videoDetailsFragmentLineraLayout.findViewById(R.id.report_image_view);
        mReportAVideoImageView.setOnClickListener(onClickListener);

        mDescriptionCardView = (CardView) videoDetailsFragmentLineraLayout.findViewById(R.id.description_card_view);
        mDescriptionTextView = (TextView) videoDetailsFragmentLineraLayout.findViewById(R.id.description_text_view);
        mYoutubePlayerFragment = (YouTubePlayerSupportFragment)getChildFragmentManager().findFragmentById(R.id.youtube_fragment);

        return videoDetailsFragmentLineraLayout;
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mAttachedActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize items
        mResponsesStack = new Stack<Object>();
        mPicasso = Picasso.with(mAttachedActivity);

        initializeYouTubePlayerFragment();

//        // Get the screen orientation
//        Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
//        int orientation = display.getRotation();
//        if (orientation == Surface.ROTATION_90 || orientation == Surface.ROTATION_270) {
//            Log.v(TAG, "The screen started on landscape mode");
//            mIsLandscape = true;
//            updateScreenViews();
//        } else {
//            Log.v(TAG, "The screen started on portrait mode");
//            mIsLandscape = false;
//            updateScreenViews();
//        }

    }

    //    @Override
//    protected void onResume() {
//        super.onResume();
//
//        // Fill the video info if it is empty
//        if (mVideo == null) {
//            mVideo = mVideosModule.getVideoInfo(mVideoObjectIdsList.get(0));
//
//            // Finish if there is any problem with the video
//            if (mVideo == null) {
//                Log.e(TAG, "Error retrieving the video from the backend. The video with id " + mVideoObjectIdsList + " no existe");
//                finish();
//            } else {
//                updateVideoDetails();
//                Log.v(TAG, "The content of mVideo is " + mVideo);
//                mVideosModule.requestAuthorInfo(this, mVideo.getVideoId());
//            }
//        }
//    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.like_image_view:
//                    likeThisVideo();
                    break;
                case R.id.report_image_view:
//                    reportThisVideo();
            }
        }
    };

//    /**
//     * Method used to like or unlike this video.
//     * Only logged user can like a video. So, if a user has not logged in, he cannot
//     * like a video.
//     */
//    private void likeThisVideo() {
//        if (!showAlertIfUserHasNotLoggedIn(
//                getString(R.string.video_details_activity_user_must_logged_in_to_like))) {
//            return;
//        }
//
//        // The user has logged in
//        boolean likeThisVideo = mLikeImageView.getDrawable().getConstantState() == getResources().getDrawable(R.drawable.ic_like_star).getConstantState();
//        Log.v(TAG, "The user like this video? " + likeThisVideo);
//        mUserDataModule.likeAVideo(this, likeThisVideo, mVideo.getObjectId());
//        mEventTrackingModule.trackUserAction(IEventsTrackingModule.ScreenId.VIDEO_DETAILS_SCREEN, IEventsTrackingModule.EventId.LIKE_A_VIDEO, mVideo.getObjectId(), likeThisVideo);
//    }

//    private void reportThisVideo() {
//        if (!showAlertIfUserHasNotLoggedIn(
//                getString(R.string.video_details_activity_user_must_logged_in_to_report))) {
//            return;
//        }
//
//        // The user has logged in
//        mUserDataModule.reportAVideo(this, mVideo.getObjectId());
//        mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN, EventId.REPORT_A_VIDEO, mVideo.getObjectId());
//    }

//    @Override
//    public void update(Observable observable, Object o) {
//        Log.v(TAG, "Update received from " + observable);
//        if (observable instanceof VideosModuleObserver || observable instanceof UserDataModuleObservable) {
//
//            // Add the data to the list of responses
//            mResponsesStack.push(o);
//
//            if (isInForeground()) {
//                processDataIfExists();
//            }
//
//            // The VideoDetailsActivity will listen constantly to the changes on the video details
//            //observable.deleteObserver(this);
//        }
//    }
//
//    /**
//     * Show details about the video
//     */
//    private void updateVideoDetails() {
//        if (mVideo == null) {
//            Log.e(TAG, "It is not possible to update the video details when the video info does not exists");
//            return;
//        }
//
//        Log.v(TAG, "Updating video details of " + mVideo.toString());
//
//        // Title
//        mActionBar.setTitle(mVideo.getTitle());
//
//        // Likes
//        if(mUserDataModule.doesUserLikeThisVideo(mVideo.getObjectId())) {
//            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star_filled));
//        } else {
//            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star));
//        }
//
//        // Description
//        mDescriptionTextView.setText(mVideo.getDescription());
//
//        // If the youtube player has been already initialized
//        if (mYouTubePlayer != null) {
//            mYouTubePlayer.cueVideo(mVideo.getVideoId());
//        }
//    }
//
//    private void updateAuthorInfo() {
//        Log.v(TAG, "Updating author info");
//        if (mAuthor == null){
//            return;
//        }
//
//        mPicasso.load(mAuthor.getThumbnailUrl()).into(mAuthorThumbnailImageView);
//        mAuthorNameTextView.setText(mAuthor.getName());
//    }
//
    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean wasRestored) {
        if (!wasRestored) {
            mYouTubePlayer = youTubePlayer;
            mYouTubePlayer.setOnFullscreenListener(new YouTubePlayer.OnFullscreenListener() {
                @Override
                public void onFullscreen(boolean isFullScreen) {
//                    mEventTrackingModule.trackUserAction(IEventsTrackingModule.ScreenId.VIDEO_DETAILS_SCREEN, IEventsTrackingModule.EventId.FULL_SCREEN, mVideo.getObjectId());
                    mIsFullScreen = isFullScreen;
//                    updateScreenViews();
                }
            });

            mYouTubePlayer.cueVideo("piH5_aP0fY8");
//            // If the video was ready
//            if (mVideo != null) {
//                mYouTubePlayer.cueVideo(mVideo.getVideoId());
//            }
        }
    }

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
        if (youTubeInitializationResult.isUserRecoverableError()) {
            youTubeInitializationResult.getErrorDialog(mAttachedActivity, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.error_player), youTubeInitializationResult.toString());
            Toast.makeText(mAttachedActivity, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == RECOVERY_DIALOG_REQUEST) {
//            // Retry initialization if user performed a recovery action
//            initializeYouTubePlayerFragment();
//            return;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    @Override
//    public void onBackPressed() {
//        if (mIsFullScreen) {
//            mYouTubePlayer.setFullscreen(false);
//            return;
//        }
//        super.onBackPressed();
//    }
//
    private void initializeYouTubePlayerFragment() {
        if (MainApplication.IS_PRODUCTION) {
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_PRODUCTION, this);
        } else {
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_DEBUG, this);
        }
    }

//    @Override
//    protected void processDataIfExists() {
//        Log.v(TAG, "Processing data is exists");
//        // 1. Check if the data exists
//        // If there were not data received from backend, then
//        // Not do anything
//        if (mResponsesStack.isEmpty()) {
//            return;
//        }
//
//        // 2. Process the data
//        while(!mResponsesStack.isEmpty()) {
//            Object response = mResponsesStack.pop();
//            Log.v(TAG, "Response get " + response);
//            // Author info
//            if (response instanceof VideosModuleAuthorResponse) {
//                Log.v(TAG, "videos module author response received");
//                VideosModuleAuthorResponse videosModuleAuthorResponse = (VideosModuleAuthorResponse) response;
//                ParseResponse parseResponse = videosModuleAuthorResponse.getParseResponse();
//                if (!parseResponse.isError()) {
//                    mAuthor = videosModuleAuthorResponse.getAuthor();
//                    // Ensure the follow code run on the UI thread
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            updateAuthorInfo();
//                        }
//                    });
//                } else {
//                    // Some error happened
//                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
//                }
//                // Like info
//            } else if (response instanceof UserDataModuleLikeResponse) {
//                Log.v(TAG, "User data module like response received");
//                UserDataModuleLikeResponse userDataModuleLikeResponse = (UserDataModuleLikeResponse) response;
//                ParseResponse parseResponse = userDataModuleLikeResponse.getParseResponse();
//                if (!parseResponse.isError()) {
//                    Like like = userDataModuleLikeResponse.getLike();
//                    String myVideoId = mVideo == null? null : mVideo.getObjectId();
//                    if (myVideoId != null && myVideoId.equals(like.getVideoId())) {
//                        mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star_filled));
//                    }
//                } else {
//                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
//                }
//                // Unlike info
//            } else if (response instanceof UserDataModuleUnlikeResponse) {
//                Log.v(TAG, "User data module unlike response received");
//                UserDataModuleUnlikeResponse userDataModuleUnlikeResponse = (UserDataModuleUnlikeResponse) response;
//                ParseResponse parseResponse = userDataModuleUnlikeResponse.getParseResponse();
//                if (!parseResponse.isError()) {
//                    Like like = userDataModuleUnlikeResponse.getLike();
//                    String myVideoId = mVideo == null? null : mVideo.getObjectId();
//                    if (myVideoId != null && myVideoId.equals(like.getVideoId())) {
//                        mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star));
//                    }
//                } else {
//                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
//                }
//            } else if (response instanceof UserDataModuleReportResponse) {
//                Log.v(TAG, "User data module report response received");
//                UserDataModuleReportResponse userDataModuleReportResponse = (UserDataModuleReportResponse)response;
//                ParseResponse parseResponse = userDataModuleReportResponse.getParseResponse();
//                if (!parseResponse.isError()) {
//                    // The report has been sent correctly
//                    mNotificationModule.showToast(R.string.video_details_activity_report_sent_correctly, true);
//                } else {
//                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
//                }
//            }
//        }
//
//        mNotificationModule.dismissLoadingDialog();
//
//        // 3. Remove the responses
//        // Since we are using a stack, there is not need to remove the responses
//    }
//

//    private void shareThisVideo() {
//        // Tracking the user
//        mEventTrackingModule.trackUserAction(IEventsTrackingModule.ScreenId.VIDEO_DETAILS_SCREEN, IEventsTrackingModule.EventId.SHARE, mVideo.getObjectId());
//
//        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
//        sharingIntent.setType("text/plain");
//
//        // Subject/Title
//        String subject = getString(R.string.share_subject, "\n\n" + mVideo.getTitle());
//        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
//        String videoDescription = mVideo.getDescription();
//        String shareBody =
//                videoDescription != null?
//                        videoDescription + "\n\n" + mVideo.getVideoUrl():
//                        mVideo.getVideoUrl();
//        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
//        startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_title)));
//    }
//
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Check the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            Log.v(TAG, "The screen is now on landscape. Hidding the elements");
//            mIsLandscape = true;
//            updateScreenViews();
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
//            Log.v(TAG, "The screen is now on portrait. Showing the elements");
//            mIsLandscape = false;
//            updateScreenViews();
//        }
//    }
//

//    /**
//     * Method used to update the views in the screen depending the screen orientation and if the YouTube fragment
//     * is in full screen or not.
//     * This method does not allow the user to exit full screen mode while in landscape
//     *
//     * If the screen is in Landscape mode or the YouTube fragment is in the full screen mode, the YouTube fragment
//     * should be the unique element of the screen.
//     *
//     * The rest of the elements, including the actionbar should be hidden.
//     *
//     * If the screen is in Portrait mode and not in the full screen mode, all the elements should appear
//     *
//     */
//    private void updateScreenViews() {
//        // If the YouTube fragment is in full screen or landscape mode
//        if (mIsFullScreen || mIsLandscape) {
//            mActionBar.hide();
//            mExtraInfoCardView.setVisibility(View.GONE);
//            mDescriptionCardView.setVisibility(View.GONE);
//            // Set it as full screen when it was not in full screen
//            if (mYouTubePlayer != null && !mIsFullScreen) {
//                mYouTubePlayer.setFullscreen(true);
//            }
//        } else {
//            mActionBar.show();
//            mExtraInfoCardView.setVisibility(View.VISIBLE);
//            mDescriptionCardView.setVisibility(View.VISIBLE);
//            if (mYouTubePlayer != null) {
//                mYouTubePlayer.setFullscreen(false);
//            }
//        }
//
//    }


}
