package com.worldspotlightapp.android.ui;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleHashTagsListResponse;
import com.worldspotlightapp.android.model.HashTag;
import com.worldspotlightapp.android.ui.videodetails.HashTagsListAdapter;

import java.util.List;
import java.util.Observable;
import java.util.Stack;

public class HashTagsListActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "HashTagsActivity";

    /**
     * The content of the selected items to be send back to the previous activity
     */
    public static final String INTENT_KEY_SELECTED_HASH_TAGS_List = "com.worldspotlight.android.ui.HashTagsActivity.SelectedHashTagsList";

    private RecyclerView mRecyclerView;
    private HashTagsListAdapter mHashTagsListAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private List<HashTag> mHashTagsList;

    /**
     * The set of response retrieved from the modules
     */
    private Stack<Object> mResponsesStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_hash_tags_list);

        mResponsesStack = new Stack<Object>();

        // Delete all the possible instance of this observer
        mVideosModule.deleteObserver(this);

        // Set the action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the elements
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);


        // This this setting to improve the performance if you know that changes
        // in content do not change the layout sie of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // Use a linearLayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mNotificationModule.showLoadingDialog(mContext);
        mVideosModule.requestAllHashTags(this);
    }

    @Override
    public void update(Observable observable, Object data) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + data);
        if (observable instanceof VideosModuleObserver) {
            // Add the data to the list of responses
            mResponsesStack.push(data);

            if (isInForeground()) {
                Log.v(TAG, "This activity is in foreground. Processing data if exists");
                processDataIfExists();
            } else {
                Log.v(TAG, "This activity is not in foreground. Not do anything");
            }

            // For now it is not needed
            //observable.deleteObserver(this);
        }
    }

    @Override
    protected void processDataIfExists() {
        Log.v(TAG, "Processing data if exists. Is the activity in foreground " + isInForeground());

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
            if (response instanceof VideosModuleHashTagsListResponse) {
                VideosModuleHashTagsListResponse videosModuleHashTagsListResponse = (VideosModuleHashTagsListResponse) response;
                ParseResponse parseResponse = videosModuleHashTagsListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "hash tags list received. " + videosModuleHashTagsListResponse.getHashTagsList());
                    mHashTagsList = videosModuleHashTagsListResponse.getHashTagsList();
                    mHashTagsListAdapter = new HashTagsListAdapter(mHashTagsList);
                    mRecyclerView.setAdapter(mHashTagsListAdapter);
                } else {
                    Log.w(TAG, "Error getting hash tags list", parseResponse.getCause());
                    mNotificationModule.showToast(parseResponse.getHumanRedableResponseMessage(mContext), true);
                }
            }

            Log.v(TAG, "Dismissing the loading dialog");
            mNotificationModule.dismissLoadingDialog();

            // 3. Remove the responses
            // Not do anything. Because the list of the response is a stack. Once all the responses has been pop out,
            // there is not need to clean them
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
    public void onBackPressed() {
        // Send the data back to the Video details activity
        Intent resultIntent = new Intent();
        if (mHashTagsListAdapter != null) {
            resultIntent.putStringArrayListExtra(INTENT_KEY_SELECTED_HASH_TAGS_List, mHashTagsListAdapter.getSelectedHashTagsList());
        }
        setResult(RESULT_OK, resultIntent);
        super.onBackPressed();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        return o instanceof HashTagsListActivity;
    }

    @Override
    public int hashCode() {
        return 0;
    }
}
