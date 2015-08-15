
package com.worldspotlightapp.android.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class VideosDetailsViewPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * The list of videos
     */
    private List<String> mVideosIdList;

    public VideosDetailsViewPagerAdapter(FragmentManager fm,
                                         List<String> videosIdList) {
        super(fm);
        this.mVideosIdList = videosIdList;
    }

    @Override
    public Fragment getItem(int index) {
        // Show show arrow or not
        Fragment videoDetailsFragment =
                VideoDetailsFragment.newInstance(mVideosIdList.get(index));

        return videoDetailsFragment;
    }

    @Override
    public int getCount() {
        return mVideosIdList.size();
    }
}
