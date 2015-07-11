package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;
import com.worldspotlightapp.android.model.Video;

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
     * Retrieve the information about a video from the database.
     * Note this is a synchronous method
     * @param videoObjectId
     *      The id of the video object
     */
    public abstract Video getVideoInfo(String videoObjectId);

    /**
     * Search the videos by keyword. Return a list of video which either the
     * title either the description contains the keyword
     * @param observer
     *      The observer to inform when the data is ready
     * @param keyword
     *      The keyword to be searched
     */
    public abstract void searchByKeyword(Observer observer, String keyword);

    /**
     * Get the author info based on the video id
     * @param observer
     *      The observer to be notified when the data is ready
     * @param videoId
     *      The id of the video which the author is looking for
     */
    public abstract void requestAuthorInfo(Observer observer, String videoId);
}
