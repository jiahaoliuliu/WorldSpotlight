package com.worldspotlightapp.android.ui.videodetails;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;
import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.CityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleOrganizersListResponse;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.ScreenId;
import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule.EventId;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleLikeResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleReportResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleUnlikeResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleAuthorResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleHashTagsListByVideoResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Organizer;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.ui.AbstractBaseFragmentObserver;
import com.worldspotlightapp.android.ui.HashTagsListActivity;
import com.worldspotlightapp.android.ui.MainApplication;
import com.worldspotlightapp.android.ui.OrganizerDetailsActivity;
import com.worldspotlightapp.android.ui.OrganizersRecyclerActivity;
import com.worldspotlightapp.android.ui.mainactivity.MainActivity;
import com.worldspotlightapp.android.utils.DebugOptions;
import com.worldspotlightapp.android.utils.HashTagView;
import com.worldspotlightapp.android.utils.Secret;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Observable;
import java.util.Stack;

public class VideoDetailsFragment extends AbstractBaseFragmentObserver implements YouTubePlayer.OnInitializedListener {
    private static final String TAG = "VideoDetailsFragment";

    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final int REQUEST_CODE_HASH_TAGS_LIST_ACTIVITY = 2;

    private static final int MENU_ITEM_SHARE_VIDEO_ID = 1000;

    private String mVideoObjectId;

    // The stack of responses from backend
    private Stack<Object> mResponsesStack;
    private Video mVideo;
    private Author mAuthor;
    private List<Organizer> mOrganizersList;

    // Views
    private CardView mExtraInfoCardView;
    private ImageView mAuthorThumbnailImageView;
    private TextView mAuthorNameTextView;
    private ImageView mReportAVideoImageView;
    private ImageView mLikeImageView;

    // Description
    private CardView mDescriptionCardView;
    private TextView mDescriptionContentTextView;

    // HashTags
    private TextView mHashTagsTextView;
    private TextView mEmptyHashTagsTextView;
    private ImageView mChangeHashTagsImageView;

    // Organizers
    private CardView mOrganizersCardView;
    private RelativeLayout mOrganizer1RelativeLayout;
    private RelativeLayout mOrganizer2RelativeLayout;
    private RelativeLayout mOrganizer3RelativeLayout;
    private RelativeLayout mOrganizer4RelativeLayout;
    private RelativeLayout mOrganizer5RelativeLayout;
    private Button mMoreOrganizersButton;

    private List<RelativeLayout> mOrganizersViewsList;

    // YouTube
    private YouTubePlayerSupportFragment mYoutubePlayerFragment;
    private YouTubePlayerSupportFragment mDummyYoutubePlayerFragment;
    private YouTubePlayer mYouTubePlayer;

    // Check if it was full screen or not
    private boolean mIsFullScreen;

    // Check if the screen orientation is landscape or not
    private boolean mIsLandscape;

    // Others
    private Picasso mPicasso;
    private boolean mIsThisFragmentVisible;

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

        // SetUp the data
        mOrganizersViewsList = new ArrayList<RelativeLayout>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Set the content of the view
        ScrollView videoDetailsFragmentScrollView = (ScrollView)inflater.inflate(R.layout.fragment_video_details, container,
                false);

        mDummyYoutubePlayerFragment = (YouTubePlayerSupportFragment)getChildFragmentManager().findFragmentById(R.id.dummy_youtube_fragment);

        // Link the views
        // Extra info
        mExtraInfoCardView = (CardView) videoDetailsFragmentScrollView.findViewById(R.id.extra_info_card_view);
        mAuthorThumbnailImageView = (ImageView) videoDetailsFragmentScrollView.findViewById(R.id.author_thumbnail_image_view);
        mAuthorNameTextView = (TextView) videoDetailsFragmentScrollView.findViewById(R.id.author_name_text_view);

        mLikeImageView = (ImageView) videoDetailsFragmentScrollView.findViewById(R.id.like_image_view);
        mLikeImageView.setOnClickListener(onClickListener);

        mReportAVideoImageView = (ImageView) videoDetailsFragmentScrollView.findViewById(R.id.report_image_view);
        mReportAVideoImageView.setOnClickListener(onClickListener);

        // Description
        mDescriptionCardView = (CardView) videoDetailsFragmentScrollView.findViewById(R.id.description_card_view);
        mDescriptionContentTextView = (TextView) videoDetailsFragmentScrollView.findViewById(R.id.description_content_text_view);

        // Hash tag
        mEmptyHashTagsTextView = (TextView) videoDetailsFragmentScrollView.findViewById(R.id.empty_hash_tag_text_view);
        mEmptyHashTagsTextView.setOnClickListener(onClickListener);

        mHashTagsTextView = (TextView) videoDetailsFragmentScrollView.findViewById(R.id.hashtags_text_view);

        mChangeHashTagsImageView = (ImageView) videoDetailsFragmentScrollView.findViewById(R.id.change_hashtag_image_view);
        mChangeHashTagsImageView.setOnClickListener(onClickListener);

        // Organizers
        mOrganizersCardView = (CardView) videoDetailsFragmentScrollView.findViewById(R.id.organizers_card_view);

        mOrganizer1RelativeLayout = (RelativeLayout) videoDetailsFragmentScrollView.findViewById(R.id.organizer_1_layout);
        mOrganizersViewsList.add(mOrganizer1RelativeLayout);
        mOrganizer1RelativeLayout.setOnClickListener(onClickListener);

        mOrganizer2RelativeLayout = (RelativeLayout) videoDetailsFragmentScrollView.findViewById(R.id.organizer_2_layout);
        mOrganizersViewsList.add(mOrganizer2RelativeLayout);
        mOrganizer2RelativeLayout.setOnClickListener(onClickListener);

        mOrganizer3RelativeLayout = (RelativeLayout) videoDetailsFragmentScrollView.findViewById(R.id.organizer_3_layout);
        mOrganizersViewsList.add(mOrganizer3RelativeLayout);
        mOrganizer3RelativeLayout.setOnClickListener(onClickListener);

        mOrganizer4RelativeLayout = (RelativeLayout) videoDetailsFragmentScrollView.findViewById(R.id.organizer_4_layout);
        mOrganizersViewsList.add(mOrganizer4RelativeLayout);
        mOrganizer4RelativeLayout.setOnClickListener(onClickListener);

        mOrganizer5RelativeLayout = (RelativeLayout) videoDetailsFragmentScrollView.findViewById(R.id.organizer_5_layout);
        mOrganizersViewsList.add(mOrganizer5RelativeLayout);
        mOrganizer5RelativeLayout.setOnClickListener(onClickListener);

        mMoreOrganizersButton = (Button) videoDetailsFragmentScrollView.findViewById(R.id.more_organizers_button);
        mMoreOrganizersButton.setOnClickListener(onClickListener);

        return videoDetailsFragmentScrollView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Initialize items
        mResponsesStack = new Stack<Object>();
        mPicasso = Picasso.with(mAttachedActivity);

        // TODO: Capture the screen orientation when the fragment starts
//        // Get the screen orientation
//        Display display = ((WindowManager) mAttachedActivity.getSystemService(mAttachedActivity.WINDOW_SERVICE)).getDefaultDisplay();
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

    @Override
    public void onResume() {
        super.onResume();

        // Fill the video info if it is empty
        if (mVideo == null) {
            mVideo = mVideosModule.getVideoInfo(mVideoObjectId);

            Log.d(TAG, "Video info retrieved for the video id " + mVideoObjectId + " is " + mVideo);

            // Finish if there is any problem with the video
            if (mVideo == null) {
                Log.e(TAG, "Error retrieving the video from the backend. The video with id " + mVideoObjectId + " no existe");
                mAttachedActivity.finish();
            } else {
                Log.v(TAG, "The content of mVideo is " + mVideo);
                updateVideoDetails();
                mVideosModule.requestAuthorInfo(this, mVideo.getVideoId());
            }

            // Update the list of hash tags
            updateHashTagsView();

            // Update the hash tags from the backend
            mVideosModule.requestHashTagsListForAVideo(this, mVideoObjectId);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "Fragment detached");
    }

    private View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.like_image_view:
                    likeThisVideo();
                    break;
                case R.id.report_image_view:
                    reportThisVideo();
                    break;
                case R.id.empty_hash_tag_text_view:
                case R.id.change_hashtag_image_view:
                    launchHashTagsListActivity();
                    break;
                case R.id.organizer_1_layout:
                    launchOrganizerDetailsActivity(0);
                    break;
                case R.id.organizer_2_layout:
                    launchOrganizerDetailsActivity(1);
                    break;
                case R.id.organizer_3_layout:
                    launchOrganizerDetailsActivity(2);
                    break;
                case R.id.organizer_4_layout:
                    launchOrganizerDetailsActivity(3);
                    break;
                case R.id.organizer_5_layout:
                    launchOrganizerDetailsActivity(4);
                    break;
                case R.id.more_organizers_button:
                    launchOrganizersListActivity();
                    break;
            }
        }
    };

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser) {
            Log.v(TAG, "The fragment with video object id " + mVideoObjectId + " is visible to the user");

            mIsThisFragmentVisible = true;

            // Set the real youtube player fragment
            mYoutubePlayerFragment = YouTubePlayerSupportFragment.newInstance();
            getChildFragmentManager().beginTransaction().replace(R.id.youtube_fragment_container, mYoutubePlayerFragment).commit();

            initializeYouTubePlayerFragment();

            // Set the title. The action bar and the video could be null
            if (mActionBar != null && mVideo != null) {
                mActionBar.setTitle(mVideo.getTitle());
            }

        } else {
            Log.v(TAG, "The fragment with video object id " + mVideoObjectId + " is not longer visible to the user");
            if (!mIsActivityCreated) {
                Log.w(TAG, "The activity has not been created yet. Not do anything");
                return;
            }

            mIsThisFragmentVisible = false;

            // Remove the actual youtube fragment because Youtube fragment cannot be used more in more than one places at
            // the same time.
            getChildFragmentManager().beginTransaction().remove(mYoutubePlayerFragment).commit();
            mYoutubePlayerFragment = null;
        }
    }

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
        boolean likeThisVideo = mLikeImageView.getDrawable().getConstantState() ==
                                getResources().getDrawable(R.drawable.ic_like_star).getConstantState();
        Log.v(TAG, "The user like this video? " + likeThisVideo);
        mUserDataModule.likeAVideo(this, likeThisVideo, mVideo.getObjectId());
        mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN, EventId.LIKE_A_VIDEO, mVideo.getObjectId(), likeThisVideo);
    }

    private void reportThisVideo() {
        if (!showAlertIfUserHasNotLoggedIn(
                getString(R.string.video_details_activity_user_must_logged_in_to_report))) {
            return;
        }

        // The user has logged in
        mUserDataModule.reportAVideo(this, mVideo.getObjectId());
        mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN, EventId.REPORT_A_VIDEO, mVideo.getObjectId());
    }

    @Override
    public void update(Observable observable, Object o) {
        Log.v(TAG, "Update received from " + observable);
        if (observable instanceof VideosModuleObserver || observable instanceof UserDataModuleObservable
                || observable instanceof CityModuleObservable) {

            // Add the data to the list of responses
            mResponsesStack.push(o);

            if (isInForeground()) {
                processDataIfExists();
            }

            // The VideoDetailsFragment will listen constantly to the changes on the video details
            //observable.deleteObserver(this);
        }
    }

    /**
     * Show details about the video
     */
    private void updateVideoDetails() {
        if (mVideo == null) {
            Log.e(TAG, "It is not possible to update the video details when the video info does not exists");
            return;
        }

        Log.v(TAG, "Updating video details of " + mVideo.toString());

        // Action bar
        if (mIsThisFragmentVisible) {
            mActionBar.setTitle(mVideo.getTitle());
        }

        // Likes
        if(mUserDataModule.doesUserLikeThisVideo(mVideo.getObjectId())) {
            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star_filled));
        } else {
            mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star));
        }

        // Description
        mDescriptionContentTextView.setText(mVideo.getDescription());

        // If the youtube player has been already initialized
        if (mYouTubePlayer != null) {
            mYouTubePlayer.cueVideo(mVideo.getVideoId());
        }

        // Refresh the list of organizers
        mCityModuleObservable.retrieveAllOrganizersOfTheCity(this, mVideo.getCity(), mVideo.getCountry());
    }

    private void updateAuthorInfo() {
        Log.v(TAG, "Updating author info");
        if (mAuthor == null){
            return;
        }

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
                    mIsFullScreen = isFullScreen;
                    updateScreenViews();
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
            youTubeInitializationResult.getErrorDialog(mAttachedActivity, RECOVERY_DIALOG_REQUEST).show();
        } else {
            // In case that the fragment is not in foreground
            if (mAttachedActivity != null) {
                String errorMessage = String.format(getString(R.string.error_player), youTubeInitializationResult.toString());
                Toast.makeText(mAttachedActivity, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }

// TODO: Implement this for the fragment
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (requestCode == RECOVERY_DIALOG_REQUEST) {
//            // Retry initialization if user performed a recovery action
//            initializeYouTubePlayerFragment();
//            return;
//        }
//        super.onActivityResult(requestCode, resultCode, data);
//    }

// TODO: Listen to the back button
//    @Override
//    public void onBackPressed() {
//        if (mIsFullScreen) {
//            mYouTubePlayer.setFullscreen(false);
//            return;
//        }
//        super.onBackPressed();
//    }

    private void initializeYouTubePlayerFragment() {
        if (mYoutubePlayerFragment == null) {
            Log.w(TAG, "Trying to initialize youtube player fragment when it is null");
            return;
        }

        if (DebugOptions.IS_PRODUCTION) {
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_PRODUCTION, this);
        } else {
            mYoutubePlayerFragment.initialize(Secret.GOOGLE_API_DEBUG, this);
        }
    }

    @Override
    protected void processDataIfExists() {
        Log.v(TAG, "Processing data if exists: " + mResponsesStack.size());
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
            // Author info
            if (response instanceof VideosModuleAuthorResponse) {
                Log.v(TAG, "videos module author response received");
                VideosModuleAuthorResponse videosModuleAuthorResponse = (VideosModuleAuthorResponse) response;
                // There could be other fragments requesting the author info at the same time, so it is important
                // to verify the author is correct by checking the video object id
                String videoObjectId = videosModuleAuthorResponse.getVideoObjectId();
                Log.v(TAG, "The video object id received is " + videoObjectId + " and my video is " + mVideo);
                if (videoObjectId != null && mVideo != null && videoObjectId.equals(mVideo.getVideoId())) {
                    ParseResponse parseResponse = videosModuleAuthorResponse.getParseResponse();
                    if (!parseResponse.isError()) {
                        mAuthor = videosModuleAuthorResponse.getAuthor();
                        // Ensure the follow code run on the UI thread
                        mAttachedActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateAuthorInfo();
                            }
                        });
                    } else {
                        // Some error happened
                        mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mAttachedActivity), true);
                    }
                }
                // Like info
            } else if (response instanceof UserDataModuleLikeResponse) {
                Log.v(TAG, "User data module like response received");
                UserDataModuleLikeResponse userDataModuleLikeResponse = (UserDataModuleLikeResponse) response;
                ParseResponse parseResponse = userDataModuleLikeResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    Like like = userDataModuleLikeResponse.getLike();
                    String myVideoId = mVideo == null? null : mVideo.getObjectId();
                    if (myVideoId != null && myVideoId.equals(like.getVideoId())) {
                        mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star_filled));
                    }
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mAttachedActivity), true);
                }
                // Unlike info
            } else if (response instanceof UserDataModuleUnlikeResponse) {
                Log.v(TAG, "User data module unlike response received");
                UserDataModuleUnlikeResponse userDataModuleUnlikeResponse = (UserDataModuleUnlikeResponse) response;
                ParseResponse parseResponse = userDataModuleUnlikeResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    Like like = userDataModuleUnlikeResponse.getLike();
                    String myVideoId = mVideo == null? null : mVideo.getObjectId();
                    if (myVideoId != null && myVideoId.equals(like.getVideoId())) {
                        mLikeImageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_like_star));
                    }
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mAttachedActivity), true);
                }
            } else if (response instanceof UserDataModuleReportResponse) {
                Log.v(TAG, "User data module report response received");
                UserDataModuleReportResponse userDataModuleReportResponse = (UserDataModuleReportResponse)response;
                ParseResponse parseResponse = userDataModuleReportResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    // The report has been sent correctly
                    mNotificationModule.showToast(R.string.video_details_activity_report_sent_correctly, true);
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mAttachedActivity), true);
                }
            } else if (response instanceof VideosModuleHashTagsListByVideoResponse) {
                Log.v(TAG, "Hash tags list by video response received");
                VideosModuleHashTagsListByVideoResponse videosModuleHashTagsListByVideoResponse =
                        (VideosModuleHashTagsListByVideoResponse) response;
                ParseResponse parseResponse = videosModuleHashTagsListByVideoResponse.getParseResponse();

                // Update the data if there is not error, the object ids are equal and
                // the video is not null
                if (!parseResponse.isError()
                        && mVideoObjectId.equals(videosModuleHashTagsListByVideoResponse.getVideoObjectId())
                        && mVideo != null) {
                    Log.v(TAG, "Hash list retrieved correctly from the backend");
                    Collection<String> oldHashTagsList = new ArrayList<String>(mVideo.getHashTags());
                    Log.v(TAG, "The old hash tags list is " + oldHashTagsList);
                    Collection<String> newHashTagsList = new ArrayList<String>(videosModuleHashTagsListByVideoResponse.getHashTagsList());
                    Log.v(TAG, "The new hash tags list is " + newHashTagsList);
                    oldHashTagsList.removeAll(newHashTagsList);
                    newHashTagsList.removeAll(oldHashTagsList);

                    if (!oldHashTagsList.isEmpty() || !newHashTagsList.isEmpty()) {
                        Log.v(TAG, "The hash tags list has been changed. Updating the views");
                        mVideo.setHashTags(videosModuleHashTagsListByVideoResponse.getHashTagsList());
                        updateHashTagsView();
                    }
                }
            } else if (response instanceof CityModuleOrganizersListResponse) {
                Log.v(TAG, "Organizers list received");
                CityModuleOrganizersListResponse cityModuleOrganizersListResponse =
                        (CityModuleOrganizersListResponse) response;
                ParseResponse parseResponse = cityModuleOrganizersListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mOrganizersList = cityModuleOrganizersListResponse.getOrganizersList();
                    Log.v(TAG, "Organizers list received " + mOrganizersList);
                    updateOrganizersCardView();
                } else {
                    Log.v(TAG, "Error getting the list of organizers");
                }
            }
        }

        mNotificationModule.dismissLoadingDialog();

        // 3. Remove the responses
        // Since we are using a stack, there is not need to remove the responses
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // Check the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.v(TAG, "The screen is now on landscape. Hidding the elements");
            mIsLandscape = true;
            updateScreenViews();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            Log.v(TAG, "The screen is now on portrait. Showing the elements");
            mIsLandscape = false;
            updateScreenViews();
        }
    }

    /**
     * Method used to update the views in the screen depending the screen orientation and if the YouTube fragment
     * is in full screen or not.
     * This method does not allow the user to exit full screen mode while in landscape
     *
     * If the screen is in Landscape mode or the YouTube fragment is in the full screen mode, the YouTube fragment
     * should be the unique element of the screen.
     *
     * The rest of the elements, including the actionbar should be hidden.
     *
     * If the screen is in Portrait mode and not in the full screen mode, all the elements should appear
     *
     */
    private void updateScreenViews() {
        // If the YouTube fragment is in full screen or landscape mode
        if (mIsFullScreen || mIsLandscape) {
            mActionBar.hide();
            mExtraInfoCardView.setVisibility(View.GONE);
            mDescriptionCardView.setVisibility(View.GONE);
            mHashTagsTextView.setVisibility(View.GONE);
            mOrganizersCardView.setVisibility(View.GONE);
            // Set it as full screen when it was not in full screen
            if (mYouTubePlayer != null && !mIsFullScreen) {
                mYouTubePlayer.setFullscreen(true);
            }
        } else {
            mActionBar.show();
            mExtraInfoCardView.setVisibility(View.VISIBLE);
            mDescriptionCardView.setVisibility(View.VISIBLE);
            mHashTagsTextView.setVisibility(View.VISIBLE);
            updateOrganizersCardView();
            if (mYouTubePlayer != null) {
                mYouTubePlayer.setFullscreen(false);
            }
        }
    }

    /**
     * Show the organizers card view only if it is needed.
     * The previous state must be Gone
     */
    private void updateOrganizersCardView() {
        mOrganizersCardView.setVisibility(View.GONE);

        // If the user has not received the organizers list or if it is empty
        // keep the organizers card view hidden
        if (mOrganizersList == null || mOrganizersList.isEmpty()) {
            return;
        }

        // Show the organizers card view only when there are at least one organizer
        mOrganizersCardView.setVisibility(View.VISIBLE);

        // Fill each one of the organizers
        for (int i = 0; i < mOrganizersList.size() && i < mOrganizersViewsList.size(); i++) {
            Organizer organizer = mOrganizersList.get(i);
            RelativeLayout organizerView = mOrganizersViewsList.get(i);
            fillOrganizerView(organizer, organizerView);
        }

        // Show more button when there are more organizers to be shown
        if (mOrganizersList.size() > mOrganizersViewsList.size()) {
            mMoreOrganizersButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Fill the content of the organizer view using the data in the organizer
     * @param organizer
     * @param organizerView
     */
    private void fillOrganizerView(Organizer organizer, RelativeLayout organizerView) {
        if (organizerView == null) {
            Log.w(TAG, "Trying to fill the organizer view with organizer but the organizer view is null");
            return;
        }

        // Set the whole view as visible
        organizerView.setVisibility(View.VISIBLE);

        // Image
        // When the data is received, Picasso could not be ready
        if (mPicasso != null && organizer.hasLogoUrl()) {
            ImageView logoImageView = (ImageView) organizerView.findViewById(R.id.logo_image_view);
            mPicasso.load(organizer.getLogoUrl()).into(logoImageView);
        }

        // Title
        if (organizer.hasName()) {
            TextView nameTextView = (TextView) organizerView.findViewById(R.id.name_text_view);
            nameTextView.setText(organizer.getName());
        }

        // Description
        if (organizer.hasDescription()) {
            TextView descriptionTextView = (TextView) organizerView.findViewById(R.id.description_text_view);
            descriptionTextView.setText(organizer.getDescription());
        }

    }

    // Action bar
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        // Update user profile
        MenuItem menuItemShareVideo = menu.add(Menu.NONE, MENU_ITEM_SHARE_VIDEO_ID, Menu
                .NONE, R.string.action_bar_share)
                .setIcon(R.drawable.ic_action_share);
        menuItemShareVideo.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                mAttachedActivity.onBackPressed();
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
        mEventTrackingModule.trackUserAction(ScreenId.VIDEO_DETAILS_SCREEN,
                                            EventId.SHARE, mVideo.getObjectId());

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

    // Show the list of hash tags
    private void launchHashTagsListActivity() {
        Intent startHashTagsListActivityIntent = new Intent(mAttachedActivity, HashTagsListActivity.class);
        startHashTagsListActivityIntent.putExtra(Video.INTENT_KEY_OBJECT_ID, mVideoObjectId);
        startHashTagsListActivityIntent.putStringArrayListExtra(HashTagsListActivity.INTENT_KEY_SELECTED_HASH_TAGS_LIST, mVideo.getHashTags());
        startActivityForResult(startHashTagsListActivityIntent, REQUEST_CODE_HASH_TAGS_LIST_ACTIVITY);
    }

    // Show the list of organizers
    private void launchOrganizersListActivity() {
        if (mVideo == null) {
            Log.e(TAG, "Trying to launch the organizers list when the video is null");
            return;
        }

        if (!mVideo.hasCity() || !mVideo.hasCountry()) {
            Log.e(TAG, "The video must have city and the country");
            return;
        }

        Intent startHashTagsListActivityIntent = new Intent(mAttachedActivity, OrganizersRecyclerActivity.class);
        startHashTagsListActivityIntent.putExtra(Video.INTENT_KEY_CITY, mVideo.getCity());
        startHashTagsListActivityIntent.putExtra(Video.INTENT_KEY_COUNTRY, mVideo.getCountry());
        startActivity(startHashTagsListActivityIntent);
    }

    /**
     * Launch the activity which shows the organizer details
     * @param organizerPosition
     *      The position of the organizer in the list
     */
    private void launchOrganizerDetailsActivity(int organizerPosition) {
        // Precondition: The organizers list should not be null
        if (mOrganizersList == null) {
            Log.e(TAG, "Trying to show the organizer detail when the organizer list is null");
            return;
        }

        // Precondition: The index should not be bigger than the position
        if (organizerPosition + 1 > mOrganizersList.size()) {
            Log.e(TAG, "Trying to shwo the organizer detail when the position " + organizerPosition + "(+1) is bigger than the list " + mOrganizersList);
            return;
        }

        Organizer organizer = mOrganizersList.get(organizerPosition);
        String organizerObjectId = organizer.getObjectId();

        Intent startOrganizerDetailsActivityIntent = new Intent(mAttachedActivity, OrganizerDetailsActivity.class);
        startOrganizerDetailsActivityIntent.putExtra(Organizer.INTENT_KEY_OBJECT_ID, organizerObjectId);
        startActivity(startOrganizerDetailsActivityIntent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_HASH_TAGS_LIST_ACTIVITY) {
            if (resultCode == Activity.RESULT_OK) {
                String videoObjectId = data.getStringExtra(Video.INTENT_KEY_OBJECT_ID);
                if (!mVideoObjectId.equals(videoObjectId)) {
                    Log.w(TAG, "Wrong video object id received");
                    return;
                }

                ArrayList<String> selectedHashTagsList = data.getStringArrayListExtra(HashTagsListActivity.INTENT_KEY_SELECTED_HASH_TAGS_LIST);
                Log.v(TAG, "The list of hash selected received is " + selectedHashTagsList);
                updateHashTagsList(selectedHashTagsList);
            }
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateHashTagsList(ArrayList<String> selectedHashTagsList) {
        mVideo.setHashTags(selectedHashTagsList);
        updateHashTagsView();
        mVideosModule.updateHashTagsList(this, mVideoObjectId, selectedHashTagsList);
    }

    private void updateHashTagsView() {
        if (mVideo == null) {
            Log.e(TAG, "Cannot update the hash tags view when the video is null");
            return;
        }

        ArrayList<String> hashTagsList = mVideo.getHashTags();
        if (hashTagsList.isEmpty()) {
            mEmptyHashTagsTextView.setVisibility(View.VISIBLE);
            mHashTagsTextView.setVisibility(View.GONE);
            return;
        } else {
            mEmptyHashTagsTextView.setVisibility(View.GONE);
            mHashTagsTextView.setVisibility(View.VISIBLE);
        }

        // Remove the previous hashtags
        mHashTagsTextView.setText("");

        // Add # in front of all the words
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < hashTagsList.size(); i++) {
            // TODO: Remove the white space at the beginning of the first word
            stringBuilder.append(" #" + hashTagsList.get(i));
        }

        String hashTagsListStrings = stringBuilder.toString();
        SpannableString spannableString = new SpannableString(hashTagsListStrings);

        String[] hashtagsArray = stringBuilder.toString().split(" #");

        // The initial position is 0
        int position = 0;
        for (final String hashTag : hashtagsArray) {
            spannableString.setSpan(
                    new HashTagView(mAttachedActivity) {
                        @Override
                        public void onClick(View widget) {
                            String originalWord = hashTag.replaceFirst(" #", "");
                            Toast.makeText(mAttachedActivity, originalWord, Toast.LENGTH_SHORT).show();

                            // Return the keyword to the main activity
                            Intent resultIntent = new Intent();
                            resultIntent.putExtra(MainActivity.INTENT_KEY_KEYWORD, originalWord);
                            mAttachedActivity.setResult(Activity.RESULT_OK, resultIntent);
                            mAttachedActivity.finish();
                        }
                    },
                    // Position - 1 to include the hash
                    position == 0 ? position : position - 1 ,
                    position + hashTag.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Update position
            position += hashTag.length();
            // The position has to increase 2 because the separator is " #"
            position += 2;
        }

        mHashTagsTextView.setText(spannableString);
        mHashTagsTextView.setMovementMethod(LinkMovementMethod.getInstance());
        mHashTagsTextView.setHighlightColor(Color.TRANSPARENT);
    }
}
