
package com.worldspotlightapp.android.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.worldspotlightapp.android.model.Video;

import java.util.List;

public class VideosPreviewViewPagerAdapter extends FragmentStatePagerAdapter {

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
}
