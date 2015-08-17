package com.worldspotlightapp.android.ui.videodetails;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.viewpagerindicator.UnderlinePageIndicator;
import com.worldspotlightapp.android.R;
import com.worldspotlightapp.android.model.Video;
import com.worldspotlightapp.android.ui.AbstractBaseActivity;
import com.worldspotlightapp.android.ui.AbstractBaseActivityObserver;

import java.util.List;
import java.util.Observable;

public class VideoDetailsActivity extends AbstractBaseActivity {

    private static final String TAG = "VideoDetailsActivity";

    /**
     * The intent key for the list of object ids received which belong the list of the video
     * to be displayed
     */
    public static final String INTENT_KEY_VIDEO_LIST_OBJECT_IDS = "com.worldspotlightapp.android.ui.videodetails.VideoDetailsActivity.videoListObjectIds";

    // Data
    private List<String> mVideoObjectIdsList;
    private String mVideoObjectId;

    private FragmentManager mFragmentManager;

    // Views
    private ViewPager mVideosDetailsViewPager;
    private UnderlinePageIndicator mVideosDetailsViewPagerIndicator;

    // Others
    private VideosDetailsViewPagerAdapter mVideosDetailsViewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_details);

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

        // Initialize data
        mFragmentManager = getSupportFragmentManager();

        // Action bar
        mActionBar.setDisplayHomeAsUpEnabled(true);

        // Link the views
        mVideosDetailsViewPager = (ViewPager) findViewById(R.id.videos_details_view_pager);
        mVideosDetailsViewPagerIndicator = (UnderlinePageIndicator) findViewById(R.id.videos_details_view_pager_indicator);

        // Limit the number of fragments loaded offScreen because YouTube Player Fragment does not support it
        mVideosDetailsViewPager.setOffscreenPageLimit(0);

        mVideosDetailsViewPagerAdapter = new VideosDetailsViewPagerAdapter(mFragmentManager, mVideoObjectIdsList);
        mVideosDetailsViewPager.setAdapter(mVideosDetailsViewPagerAdapter);

        // Set the view pager in the view pager indicator
        mVideosDetailsViewPagerIndicator.setViewPager(mVideosDetailsViewPager);
        mVideosDetailsViewPagerIndicator.setFades(false);

        // Set the initial position
        // if the video id is contained inside of the
        int positionInTheList = mVideoObjectIdsList.contains(mVideoObjectId) ?
            mVideoObjectIdsList.indexOf(mVideoObjectId) :
            0;

        // Set the current item
        mVideosDetailsViewPager.setCurrentItem(positionInTheList);
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
