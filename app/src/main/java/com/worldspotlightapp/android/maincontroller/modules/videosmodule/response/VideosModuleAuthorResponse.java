package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleAuthorResponse extends BaseModuleResponse {

    /**
     * The object id of the video which the author has created
     */
    private String mVideoObjectId;

    private Author mAuthor;

    public VideosModuleAuthorResponse(ParseResponse parseResponse, Author author, String videoObjectId) {
        super(parseResponse);
        mAuthor = author;
        mVideoObjectId = videoObjectId;
    }

    public Author getAuthor(){
        return mAuthor;
    }

    public String getVideoObjectId() {
        return mVideoObjectId;
    }
}
