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
     * Get the list of all videos from background.
     * It checks the videos saved in the local database and based
     * on those videos, ask the backend to refresh the list of videos
     * @param observer
     *      The Observer to notify when the videos is ready
     */
    public abstract void requestAllVideos(Observer observer);

    /**
     * Retrieve the information about a video from the backend
     * @param observer
     *      The observer to notify
     * @param videoObjectId
     *      The id of the video object
     */
    public abstract void requestVideoInfo(Observer observer, String videoObjectId);
}
