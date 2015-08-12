
package com.worldspotlightapp.android.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

public class VideosPreviewViewPagerAdapter extends FragmentStatePagerAdapter
        implements VideosPreviewFragment.IOnVideosPreviewFragmentClickedListener{

    private List<Video> mVideosList;

    public VideosPreviewViewPagerAdapter(FragmentManager fm,
            List<Video> videosList) {
        super(fm);
        this.mVideosList = videosList;
    }

    @Override
    public Fragment getItem(int index) {
        // Show show arrow or not
        boolean shouldShowArrow = mVideosList.size() > 1;

        Video videoToShow = mVideosList.get(index);
        Fragment videosPreviewFragment =
                VideosPreviewFragment.newInstance(
                        videoToShow.getObjectId(),
                        videoToShow.getThumbnailUrl(),
                        videoToShow.getTitle(),
                        videoToShow.getDescription(),
                        shouldShowArrow);

        return videosPreviewFragment;
    }

    @Override
    public int getCount() {
        return mVideosList.size();
    }

    @Override
    public void onClick(String objectId) {
//                    // Register the event
//                    mEventTrackingModule.trackUserAction(ScreenId.MAIN_SCREEN, EventId.VIDEO_PREVIEW_CLICK, mObjectId);
//
//                    // Start the video details activity
//                    Intent startVideoDetailsActivityIntent = new Intent(mActivity, VideoDetailsActivity.class);
//                    ArrayList<String> objectIdsList = new ArrayList<String>();
//                    objectIdsList.add(mObjectId);
//                    startVideoDetailsActivityIntent.putStringArrayListExtra(VideoDetailsActivity.INTENT_KEY_VIDEO_LIST_OBJECT_IDS,
//                            objectIdsList);
//                    startActivity(startVideoDetailsActivityIntent);
    }
}
