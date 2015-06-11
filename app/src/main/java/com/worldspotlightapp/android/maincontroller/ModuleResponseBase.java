package com.worldspotlightapp.android.maincontroller;

import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;

/**
 * Created by jiahaoliuliu on 5/26/15.
 *
 * The basic class response from the modules
 *
 */
public abstract class ModuleResponseBase {

    private ParseResponse mParseResponse;

    public ModuleResponseBase(ParseResponse parseResponse) {
        this.mParseResponse = parseResponse;
    }

    public ParseResponse getParseResponse() {
        return mParseResponse;
    }
}