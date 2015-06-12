package com.worldspotlightapp.android.maincontroller.modules.eventstrackingmodule;

/**
 * Created by jiahaoliuliu on 6/12/15.
 */
public interface IEventsTrackingModule {

    /**
     * Track the app has been initialized
     */
    public abstract void trackAppInitialization();

    /**
     * Track the app has been finalized
     */
    public abstract void trackAppFinalization();


}
