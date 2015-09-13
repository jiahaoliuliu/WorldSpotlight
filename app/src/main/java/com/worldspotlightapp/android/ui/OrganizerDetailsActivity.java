package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Organizer;

import java.util.Observable;

public class OrganizerDetailsActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "OrganizerDetails";

    private String mOrganizerObjectId;

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

    }

    @Override
    protected void processDataIfExists() {
        // TODO: implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: implement this
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
