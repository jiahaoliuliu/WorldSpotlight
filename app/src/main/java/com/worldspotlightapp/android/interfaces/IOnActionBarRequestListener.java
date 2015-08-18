package com.worldspotlightapp.android.interfaces;

import android.support.v7.app.ActionBar;

/**
 * Request the action bar
 * Created by jiahaoliuliu on 15/8/15.
 */
public interface IOnActionBarRequestListener {

    /**
     * Get the support action bar
     * @return
     *      The support action bar
     */
    public ActionBar getActionBarFromActivity();
}
