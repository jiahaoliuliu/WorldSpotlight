package com.worldspotlightapp.android.maincontroller.modules.usermodule;

import android.app.Activity;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;

import java.util.Observer;
import java.util.UUID;

/**
 * This is the module which contains all the data about the user and method to update them
 * 
 * @author Jiahao Liu
 * 
 */
public abstract class AbstractUserDataModuleObservable extends AbstractBaseModuleObservable {

    /**
     * Get the unique identifier of the device
     *
     * @return The Unique identifier of the device
     */
    public abstract UUID getUuid();

    /**
     * Check if the module contains user data and the data is not empty
     * @return
     *     True if the module contains user data
     *     False otherwise
     */
    public abstract boolean hasUserData();

    /**
     * Login using facebook account. This is done using the Parse utility
     * @param observer
     *     The observer to inform when the process is done
     * @param activity
     *     The current running activity
     */
    public abstract void loginWithFacebook(Observer observer, Activity activity);

    /**
     * Special sign up for the people that has logged with Google Plus account.
     * This will create a new user in Parse with those specific data
     *
     * @param observer
     *      The observer to notify when the user has been signed up correctly
     * @param name
     *      The name of the user
     * @param email
     *      The email address of the user
     * @param profilePhotoUrl
     *      The Url for the profile photo
     * @param profileUrl
     *      The Google Plus profile url
     */
    public abstract void signupForGooglePlusUsers(Observer observer, String name, String email, String profilePhotoUrl, String profileUrl);

    /**
     * Change the like status of the user related a certain video.
     *
     * @param likeIt
     *      True if the user like the video
     *      False if the user does not like the video
     * @param videoId
     *      The id of the video object. Note this is the object id in Parse, not the id of the video in YouTube
     */
    public abstract void likeAVideo(boolean likeIt, String videoId);

}
