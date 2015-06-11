package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;

import java.util.Observer;
import java.util.UUID;

/**
 * This is the module which contains all the data about the user and method to update them
 * 
 * @author Jiahao Liu
 * 
 */
public abstract class AbstractVideosModuleObservable extends AbstractBaseModuleObservable {

    /**
     * Get the list of videos from background
     * @param observer
     *      The Observer to notify when the videos is ready
     */
    public abstract void requestVideosList(Observer observer);
}
