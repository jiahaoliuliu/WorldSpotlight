package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleLikedVideosListResponse extends VideosModuleVideosListResponse {

    /**
     * Simple constructor. The boolean areExtraVideos will be set as false
     * @param parseResponse
     *      The response data. It should be checked before get the data
     * @param videosList
     *      The list of the videos to be sent. It could be null if the response has some error
     */
    public VideosModuleLikedVideosListResponse(ParseResponse parseResponse, List<Video> videosList) {
        super(parseResponse, videosList, false);
    }
}
