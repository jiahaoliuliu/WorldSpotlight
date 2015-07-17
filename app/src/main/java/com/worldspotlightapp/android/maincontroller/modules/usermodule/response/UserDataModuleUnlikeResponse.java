package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Like;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleUnlikeResponse extends BaseModuleResponse {

    private Like mLike;

    public UserDataModuleUnlikeResponse(ParseResponse parseResponse, Like like) {
        super(parseResponse);
        this.mLike = like;
    }

    public Like getLike() {
        return mLike;
    }
}
