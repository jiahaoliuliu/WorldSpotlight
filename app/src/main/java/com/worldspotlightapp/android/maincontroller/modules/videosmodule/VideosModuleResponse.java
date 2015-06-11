package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleResponse extends BaseModuleResponse {

    private List<Video> mVideosList;

    public VideosModuleResponse(ParseResponse parseResponse, List<Video> videosList) {
        super(parseResponse);
        mVideosList = videosList;
    }

    public List<Video> getVideosList() {
        return mVideosList;
    }
}
