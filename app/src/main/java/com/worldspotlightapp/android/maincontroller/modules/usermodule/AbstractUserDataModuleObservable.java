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

}
