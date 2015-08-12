package com.worldspotlightapp.android.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;

import java.util.List;
import java.util.Observable;

public class VideoDetailsActivity extends AbstractBaseActivityObserver {

    private static final String TAG = "VideoDetailsActivity";
    private static final int RECOVERY_DIALOG_REQUEST = 1;
    private static final int MENU_ITEM_SHARE_VIDEO_ID = 1000;

    /**
     * The intent key for the list of object ids received which belong the list of the video
     * to be displayed
     */
    public static final String INTENT_KEY_VIDEO_LIST_OBJECT_IDS = "com.worldspotlightapp.android.ui.VideoDetailsActivity.videoListObjectIds";

    private List<String> mVideoObjectIdsList;
    private String mVideoObjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_video_details);

        // Retrieve the video ids list from the intent
        Bundle extras = getIntent().getExtras();
        if (extras == null || !extras.containsKey(INTENT_KEY_VIDEO_LIST_OBJECT_IDS)) {
            throw new IllegalArgumentException("You must pass the video ids list using intent");
        }
        mVideoObjectIdsList = extras.getStringArrayList(INTENT_KEY_VIDEO_LIST_OBJECT_IDS);
        Log.d(TAG, "The list of the video ids are " + mVideoObjectIdsList);

        // Retrieve the video object id from the intent
        if (extras == null || !extras.containsKey(Video.INTENT_KEY_OBJECT_ID)) {
            throw new IllegalArgumentException("You must pass the video id using intent");
        }
        mVideoObjectId = extras.getString(Video.INTENT_KEY_OBJECT_ID);
        Log.d(TAG, "The id of the video is " + mVideoObjectId);

        mVideosModule.deleteObserver(this);
        mUserDataModule.deleteObserver(this);

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

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
//                shareThisVideo();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    @Override
    protected void processDataIfExists() {
        // TODO: Implement this
    }

    @Override
    public void update(Observable observable, Object data) {
        // TODO: Implement this
    }
}
