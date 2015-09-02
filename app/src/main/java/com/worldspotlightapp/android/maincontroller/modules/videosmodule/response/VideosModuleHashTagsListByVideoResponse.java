package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;

import java.util.ArrayList;

/**
 * Created by jiahaoliuliu on 8/29/15.
 */
public class VideosModuleHashTagsListByVideoResponse extends BaseModuleResponse {

    /**
     * The list of hash tags returned
     */
    private ArrayList<String> mHashTagsList;

    /**
     * The object id of the video where the hash tags belongs
     */
    private String mVideoObjectId;

    /**
     * Compplete constructor.
     * @param parseResponse
     *      The response data. It should be checked before get the data
     * @param hashTagsList
     *      The list of the hash tags to be sent. It could be null if the response has some error
     */
    public VideosModuleHashTagsListByVideoResponse(ParseResponse parseResponse, ArrayList<String> hashTagsList, String videoObjectId) {
        super(parseResponse);
        mHashTagsList = hashTagsList;
        this.mVideoObjectId = videoObjectId;
    }

    public ArrayList<String> getHashTagsList() {
        return mHashTagsList;
    }

    public String getVideoObjectId() {
        return mVideoObjectId;
    }
}
