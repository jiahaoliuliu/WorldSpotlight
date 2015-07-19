package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.UserData;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleUserResponse extends BaseModuleResponse {

    private UserData mUserData;

    public UserDataModuleUserResponse(ParseResponse parseResponse, UserData userData) {
        super(parseResponse);
        this.mUserData = userData;
    }

    public UserData getUser() {
        return mUserData;
    }
}
