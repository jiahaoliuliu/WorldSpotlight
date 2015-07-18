package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.response.UserDataModuleLikesListResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleLikedVideosListResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;
import java.util.Observable;
import java.util.Stack;

public class FavouriteVideosListActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "FavouriteVideosList";

    private List<Video> mFavouriteVideosList;

    // Views
    private RecyclerView mFavouritesRecyclerView;
    private RecyclerView.Adapter mFavouritesAdapter;
    private RecyclerView.LayoutManager mFavouritesLayoutManager;
    private LinearLayout mEmptyListLinearLayout;

    /**
     * The set of response retrieved from the modules
     */
    private Stack<Object> mResponsesStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_video_list);

        // The user must logged in
        if (!mUserDataModule.hasUserData()) {
            Log.e(TAG, "The user must logged in to see his favourite lists");
            finish();
        }

        // Initialize data
        mResponsesStack = new Stack<Object>();

        // Link the views
        mFavouritesRecyclerView = (RecyclerView) findViewById(R.id.favourites_recycler_view);
        mEmptyListLinearLayout = (LinearLayout) findViewById(R.id.empty_list_linear_layout);

        // This this setting to improve the performance if you know that changes
        // in content do not change the layout sie of the RecyclerView
        mFavouritesRecyclerView.setHasFixedSize(true);

        // Use a linearLayoutManager
        mFavouritesLayoutManager = new LinearLayoutManager(this);
        mFavouritesRecyclerView.setLayoutManager(mFavouritesLayoutManager);

        // Ask for the list
        mNotificationModule.showLoadingDialog(mContext);
        mUserDataModule.retrieveFavouriteVideosList(this);
    }

    @Override
    protected void processDataIfExists() {
        // 1. Check if the data exists
        // If there were not data received from backend, then
        // Not do anything
        if (mResponsesStack.isEmpty()) {
            return;
        }

        // 2. Process the data
        while (!mResponsesStack.isEmpty()) {
            Object response = mResponsesStack.pop();
            // Checking the type of data
            if (response instanceof UserDataModuleLikesListResponse) {
                UserDataModuleLikesListResponse userDataModuleLikesListResponse = (UserDataModuleLikesListResponse) response;
                // If the list of videos received are extra videos to be added to the list of existence videos
                ParseResponse parseResponse = userDataModuleLikesListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mVideosModule.requestLikedVideosInfo(this, userDataModuleLikesListResponse.getLikesList());
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                    Log.v(TAG, "Dismissing the loading dialog");
                    mNotificationModule.dismissLoadingDialog();
                }
            } else if (response instanceof VideosModuleLikedVideosListResponse) {
                VideosModuleLikedVideosListResponse videosModuleLikedVideosListResponse = (VideosModuleLikedVideosListResponse) response;
                // If the list of videos received are extra videos to be added to the list of existence videos
                ParseResponse parseResponse = videosModuleLikedVideosListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mFavouriteVideosList = videosModuleLikedVideosListResponse.getVideosList();
                    updateContent();
                } else {
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                }

                Log.v(TAG, "Dismissing the loading dialog");
                mNotificationModule.dismissLoadingDialog();
            }
        }

        // 3. Remove the responses
        // Not do anything. Because the list of the response is a stack. Once all the responses has been pop out,
        // there is not need to clean them
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.v(TAG, data + " received from " + observable);
        if (observable instanceof UserDataModuleObservable || observable instanceof VideosModuleObserver) {
            mResponsesStack.add(data);

            if (isInForeground()) {
                processDataIfExists();
            }

            observable.deleteObserver(this);
        }
    }

    /**
     * Update the content shown. In this case is the list of favourite videos
     */
    private void updateContent() {
        if (mFavouriteVideosList == null || mFavouriteVideosList.isEmpty()) {
            Log.v(TAG, "The list of favourite videos is either null nor empty. Showing empty screen");
            mFavouritesRecyclerView.setVisibility(View.GONE);
            mEmptyListLinearLayout.setVisibility(View.VISIBLE);
            return;
        }

        // The list of favourite videos is not empty
        Log.v(TAG, "Showing the list of favourite videos " + mFavouriteVideosList);
        mFavouritesAdapter = new VideosListAdapter(mContext, mFavouriteVideosList);
        mFavouritesRecyclerView.setAdapter(mFavouritesAdapter);

        // Set the views
        mFavouritesRecyclerView.setVisibility(View.VISIBLE);
        mEmptyListLinearLayout.setVisibility(View.GONE);
    }
}
