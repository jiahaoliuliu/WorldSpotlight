package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.Observable;

public class AddAVideoActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "AddAVideoActivity";

    private static final int MENU_ITEM_ADD_A_VIDEO_ID = 1000;

    /**
     * The YouTube video id of the video to be added
     */
    private String mVideoId;

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(Video.INTENT_KEY_VIDEO_ID)) {
            Log.e(TAG, "You must pass the video id as extra");
            finish();
        }

        mVideoId = extras.getString(Video.INTENT_KEY_VIDEO_ID);
        Log.v(TAG, "Video id received " + mVideoId);

        setContentView(R.layout.activity_add_a_video);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();

    }

    @Override
    protected void processDataIfExists() {
        // TODO: Implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
    }

    // Action bar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Update user profile
        MenuItem menuItemAddAVideo = menu.add(Menu.NONE, MENU_ITEM_ADD_A_VIDEO_ID, Menu
                .NONE, R.string.action_bar_done)
                .setIcon(R.drawable.ic_action_done);
        menuItemAddAVideo.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.v(TAG, "home button pressed");
                onBackPressed();
                return true;
            case MENU_ITEM_ADD_A_VIDEO_ID:
                addThisVideo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addThisVideo() {
        // TDOO: Implement this
    }
}
