package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.Like;

import java.util.List;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleLikesListResponse extends BaseModuleResponse {

    private List<Like> mLikesList;

    public UserDataModuleLikesListResponse(ParseResponse parseResponse, List<Like> likesList) {
        super(parseResponse);
        this.mLikesList = likesList;
    }

    public List<Like> getLikesList() {
        return mLikesList;
    }
}
