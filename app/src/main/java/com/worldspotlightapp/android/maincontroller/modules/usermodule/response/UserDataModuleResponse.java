package com.worldspotlightapp.android.maincontroller.modules.usermodule.response;

import com.worldspotlightapp.android.maincontroller.BaseModuleResponse;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.model.User;

/**
 * Created by jiahaoliuliu on 2/20/15.
 */
public class UserDataModuleResponse extends BaseModuleResponse {

    private User mUser;

    public UserDataModuleResponse(ParseResponse parseResponse, User user) {
        super(parseResponse);
        this.mUser = user;
    }

    public User getUser() {
        return mUser;
    }
}
