package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.HashTag;

import java.util.List;

/**
 * Created by jiahaoliuliu on 8/29/15.
 */
public class VideosModuleHashTagsListResponse extends BaseModuleResponse {

    /**
     * The list of hash tags returned
     */
    private List<HashTag> mHashTagsList;

    /**
     * Compplete constructor.
     * @param parseResponse
     *      The response data. It should be checked before get the data
     * @param hashTagsList
     *      The list of the hash tags to be sent. It could be null if the response has some error
     */
    public VideosModuleHashTagsListResponse(ParseResponse parseResponse, List<HashTag> hashTagsList) {
        super(parseResponse);
        mHashTagsList = hashTagsList;
    }

    public List<HashTag> getHashTagsList() {
        return mHashTagsList;
    }
}
