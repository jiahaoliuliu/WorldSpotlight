package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.User;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleLikeResponse extends BaseModuleResponse {

    private Like mLike;

    public UserDataModuleLikeResponse(ParseResponse parseResponse, Like like) {
        super(parseResponse);
        this.mLike = like;
    }

    public Like getLike() {
        return mLike;
    }
}
