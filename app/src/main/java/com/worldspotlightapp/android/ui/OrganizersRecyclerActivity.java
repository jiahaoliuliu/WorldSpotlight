package com.worldspotlightapp.android.ui;

import android.content.pm.ActivityInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.CityModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleOrganizersListResponse;
import com.worldspotlightapp.android.maincontroller.modules.usermodule.UserDataModuleObservable;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.VideosModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.videosmodule.response.VideosModuleLikedVideosListResponse;
import com.worldspotlightapp.android.model.Organizer;
import com.worldspotlightapp.android.model.Video;

import java.util.List;
import java.util.Objects;
import java.util.Observable;
import java.util.Stack;

public class OrganizersRecyclerActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "OrganizersListActivity";

    // Internal data
    private String mCity;
    private String mCountry;
    private List<Organizer> mOrganizersList;

    // The views
    private RecyclerView mOrganizersListRecyclerView;
    private OrganizersRecyclerAdapter mOrganizersRecyclerAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // The stack of responses from backend
    private Stack<Object> mResponsesStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lock the screen orientation
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        setContentView(R.layout.activity_organizers_recycler);

        // Get the city and the country
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Video.INTENT_KEY_CITY) ||
                !extras.containsKey(Video.INTENT_KEY_COUNTRY)) {
            throw new IllegalArgumentException("You must pass the city and the country through the intent");
        }

        // Set the internal data
        mCity = extras.getString(Video.INTENT_KEY_CITY);
        Log.v(TAG, "The city received is " + mCity);

        mCountry = extras.getString(Video.INTENT_KEY_COUNTRY);
        Log.v(TAG, "The country received is " + mCountry);

        mResponsesStack = new Stack<Object>();

        // Link the views
        mOrganizersListRecyclerView = (RecyclerView) findViewById(R.id.organizers_recycler_view);

        // This this setting to improve the performance if you know that changes
        // in content do not change the layout sie of the RecyclerView
        mOrganizersListRecyclerView.setHasFixedSize(true);

        // Use a linearLayoutManager
        mLayoutManager = new LinearLayoutManager(this);
        mOrganizersListRecyclerView.setLayoutManager(mLayoutManager);

        mNotificationModule.showLoadingDialog(mContext);
        mCityModuleObservable.retrieveAllOrganizersOfTheCity(this, mCity, mCountry);
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
            if (response instanceof CityModuleOrganizersListResponse) {
                CityModuleOrganizersListResponse cityModuleOrganizersListResponse = (CityModuleOrganizersListResponse) response;
                ParseResponse parseResponse = cityModuleOrganizersListResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mOrganizersList = cityModuleOrganizersListResponse.getOrganizersList();
                    Log.v(TAG, "The list of organizers received has " + mOrganizersList.size() + " organizers.");
                    Log.v(TAG, "The list of organizers is " + mOrganizersList);
                    mOrganizersRecyclerAdapter = new OrganizersRecyclerAdapter(mContext, mOrganizersList);
                    mOrganizersListRecyclerView.setAdapter(mOrganizersRecyclerAdapter);
                } else {
                    Log.e(TAG, "Error retrieving the list of Organizers for this city");
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
    public void update(Observable observable, Object data) {
        Log.v(TAG, "Data received from " + observable + ", Object:" + data);
        if (observable instanceof CityModuleObservable) {
            // Add the data to the list of responses
            mResponsesStack.push(data);

            if (isInForeground()) {
                Log.v(TAG, "This activity is in foreground. Processing data if exists");
                processDataIfExists();
            } else {
                Log.v(TAG, "This activity is not in foreground. Not do anything");
            }

            observable.deleteObserver(this);
        }
    }
}
