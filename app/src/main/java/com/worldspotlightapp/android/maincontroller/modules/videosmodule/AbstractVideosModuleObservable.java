package com.worldspotlightapp.android.maincontroller.modules.videosmodule;

import com.google.android.gms.maps.model.LatLng;
import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;
import com.worldspotlightapp.android.model.Like;
import com.worldspotlightapp.android.model.Video;

import java.util.ArrayList;
import java.util.List;
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
     * Given a list of likes, retrieve all the information about the videos in the list
     * @param observer
     *      The observer to notify when the data is ready
     * @param likesList
     *      The list of likes
     */
    public abstract void requestLikedVideosInfo(Observer observer, List<Like> likesList);


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

    /**
     * Add a new video to the database.
     * The video shouldn't already exists in the database.
     * This method is aim to be invoked by {@link com.worldspotlightapp.android.ui.AddAVideoActivity},
     * which once the invocation is done, will be MainActivity the one which handle the notifications.
     * Since MainActivity is subscribed to this module, there is not need to call it here.
     * @param videoId
     *      The id of the video
     * @param videoLocation
     *      The location of the video
     * @param hashTagsList
     *      The list of hash tags
     */
    public abstract void addAVideo(String videoId, LatLng videoLocation, ArrayList<String> hashTagsList);

    /**
     * Request the list of all the hashtags
     * @param observer
     *      The observer to notify when the data is ready
     */
    public abstract void requestAllHashTags(Observer observer);

    /**
     * Update the list of hash tags of a video.
     * @param observer
     *      The observer to notify when the list of hash tags has been updated
     * @param videoObjectId
     *      The object id of the video to be updated
     * @param hashTagsList
     *      The list of the names of the hash tags to be updated
     */
    public abstract void updateHashTagsList(Observer observer, String videoObjectId, ArrayList<String> hashTagsList);
}
