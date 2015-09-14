package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.organizermodule.OrganizerModuleObserver;
import com.worldspotlightapp.android.maincontroller.modules.organizermodule.response.OrganizerModuleOrganizerResponse;
import com.worldspotlightapp.android.model.Organizer;

import java.util.Observable;
import java.util.Stack;

public class OrganizerDetailsActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "OrganizerDetails";

    // The organizer
    private String mOrganizerObjectId;
    private Organizer mOrganizer;

    // Internal data
    private Picasso mPicasso;
    private Stack<Object> mResponsesStack;

    // The list of views
    private ImageView mBigLogoImageView;
    private ImageView mSmallLogoImageView;
    private CardView mDescriptionCardView;
    private TextView mDescriptionTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_details);

        // Get the organizer object id from the intent
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Organizer.INTENT_KEY_OBJECT_ID)) {
            throw new IllegalArgumentException("You must pass the organizer object id through the intent");
        }

        mOrganizerObjectId = extras.getString(Organizer.INTENT_KEY_OBJECT_ID);
        Log.v(TAG, "The object id got from the intent is " + mOrganizerObjectId);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Internal data
        mPicasso = Picasso.with(mContext);
        mResponsesStack = new Stack<Object>();

        // Link the views
        mBigLogoImageView = (ImageView) findViewById(R.id.big_logo_image_view);
        mSmallLogoImageView = (ImageView) findViewById(R.id.small_logo_image_view);
        mDescriptionCardView = (CardView) findViewById(R.id.description_card_view);
        mDescriptionTextView = (TextView) findViewById(R.id.description_text_view);

        // Request for the organizer data
        mNotificationModule.showLoadingDialog(mContext);
        mOrganizerModuleObservable.retrieveOrganizerInfo(this, mOrganizerObjectId);

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
            if (response instanceof OrganizerModuleOrganizerResponse) {
                OrganizerModuleOrganizerResponse organizerModuleOrganizerResponse = (OrganizerModuleOrganizerResponse) response;
                ParseResponse parseResponse = organizerModuleOrganizerResponse.getParseResponse();
                if (!parseResponse.isError()) {
                    mOrganizer = organizerModuleOrganizerResponse.getOrganizer();
                    updateView();
                } else {
                    // Display the notification to the user
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
        if (observable instanceof OrganizerModuleObserver) {
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

    /**
     * Update the view based on the organizer
     */
    private void updateView() {
        if (mOrganizer == null) {
            Log.e(TAG, "Trying to update the view of the organizer when the organizer is null");
            return;
        }

        // Set the name
        if (mOrganizer.hasName()) {
            mActionBar.setTitle(mOrganizer.getName());
        }

        // Update the logo
        if (mOrganizer.hasLogoUrl()) {
            mPicasso.load(mOrganizer.getLogoUrl()).into(mBigLogoImageView);
            mPicasso.load(mOrganizer.getLogoUrl()).into(mSmallLogoImageView);
        }

        // Update the description
        if (mOrganizer.hasDescription()) {
            mDescriptionCardView.setVisibility(View.VISIBLE);
            mDescriptionTextView.setText(mOrganizer.getDescription());
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
}
