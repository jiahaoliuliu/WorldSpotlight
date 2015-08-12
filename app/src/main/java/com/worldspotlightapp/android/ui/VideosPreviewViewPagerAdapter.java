
package com.worldspotlightapp.android.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule.IEventsTrackingModule;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;

public class VideosPreviewViewPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * The list of videos
     */
    private List<Video> mVideosList;

    /**
     * The list of the video's object id.
     * The list is lazy created
     */
    private ArrayList<String> mVideosObjectIdList;

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

    /**
     * Get the list of the video's object id.
     * @return
     *      The list of the video's object id
     */
    public ArrayList<String> getVideosObjectIdList() {
        if (mVideosObjectIdList == null) {
            mVideosObjectIdList = createVideosObjectIdList();
        }

        return mVideosObjectIdList;
    }

    /**
     * Creates the list of the video's object id based on the video list
     * @return
     *      The list of the video's object id
     */
    private ArrayList<String> createVideosObjectIdList() {
        ArrayList<String> result = new ArrayList<String>();
        for (Video video: mVideosList) {
            result.add(video.getObjectId());
        }

        return result;
    }
}
