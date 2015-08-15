
package com.worldspotlightapp.android.ui.videodetails;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.List;

public class VideosDetailsViewPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * The list of videos
     */
    private List<String> mVideosObjectIdList;

    public VideosDetailsViewPagerAdapter(FragmentManager fm,
                                         List<String> videosObjectIdList) {
        super(fm);
        this.mVideosObjectIdList = videosObjectIdList;
    }

    @Override
    public Fragment getItem(int index) {
        // Show show arrow or not
        Fragment videoDetailsFragment =
                VideoDetailsFragment.newInstance(mVideosObjectIdList.get(index));

        return videoDetailsFragment;
    }

    @Override
    public int getCount() {
        return mVideosObjectIdList.size();
    }
}
