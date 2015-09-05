package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleUpdateVideosListResponse extends BaseModuleResponse {

    /**
     * The list of videos to be updated
     */
    private List<Video> mVideosList;

    /**
     * Compplete constructor.
     * @param parseResponse
     *      The response data. It should be checked before get the data
     * @param videosList
     *      The list of the videos to be sent. It could be null if the response has some error
     */
    public VideosModuleUpdateVideosListResponse(ParseResponse parseResponse, List<Video> videosList) {
        super(parseResponse);
        mVideosList = videosList;
    }

    public List<Video> getVideosList() {
        return mVideosList;
    }
}
