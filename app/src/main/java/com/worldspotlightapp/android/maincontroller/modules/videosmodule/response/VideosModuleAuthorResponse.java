package com.worldspotlightapp.android.maincontroller.modules.videosmodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Author;
import com.worldspotlightapp.android.model.Video;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public class VideosModuleAuthorResponse extends BaseModuleResponse {

    private Author mAuthor;

    public VideosModuleAuthorResponse(ParseResponse parseResponse, Author author) {
        super(parseResponse);
        mAuthor = author;
    }

    public Author getAuthor(){
        return mAuthor;
    }
}
