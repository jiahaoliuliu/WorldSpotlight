package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Video;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleAddAVideoResponse extends BaseModuleResponse {

    private Video mVideo;

    public VideosModuleAddAVideoResponse(ParseResponse parseResponse, Video video) {
        super(parseResponse);
        mVideo = video;
    }

    public Video getVideo() {
        return mVideo;
    }
}
