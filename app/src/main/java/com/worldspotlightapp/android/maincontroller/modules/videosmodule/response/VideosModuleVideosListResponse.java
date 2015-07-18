package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Video;

import java.util.List;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleVideosListResponse extends BaseModuleResponse {

    /**
     * The list of videos returned
     */
    private List<Video> mVideosList;

    /**
     * Set if the actual video list contains extra videos that should be concatenated with
     * the existent videos.
     */
    private boolean mAreExtraVideos;

    /**
     * Compplete constructor.
     * @param parseResponse
     *      The response data. It should be checked before get the data
     * @param videosList
     *      The list of the videos to be sent. It could be null if the response has some error
     * @param areExtraVideos
     *      If the list of the video retrieved contains all the videos needed or is just part of extra update.
     */
    public VideosModuleVideosListResponse(ParseResponse parseResponse, List<Video> videosList, boolean areExtraVideos) {
        super(parseResponse);
        mVideosList = videosList;
        mAreExtraVideos = areExtraVideos;
    }

    public List<Video> getVideosList() {
        return mVideosList;
    }

    /**
     * Set if the actual video list contains extra videos that should be concatenated with the existent
     * videos or not.
     * @return
     *      True if the list of the videos should be concatenated with the existent videos
     *      False if the list of the videos should replace the existent list of videos
     */
    public boolean areExtraVideos() {
        return mAreExtraVideos;
    }
}
