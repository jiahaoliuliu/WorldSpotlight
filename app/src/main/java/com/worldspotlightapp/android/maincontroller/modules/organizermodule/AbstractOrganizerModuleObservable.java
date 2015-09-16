package com.worldspotlightapp.android.maincontroller.modules.organizermodule;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;
import com.worldspotlightapp.android.model.City;

import java.util.Observer;

/**
 * This is the module which contains all the data about the user and method to update them
 * 
 * @author Jiahao Liu
 * 
 */
public abstract class AbstractOrganizerModuleObservable extends AbstractBaseModuleObservable {

    /**
     * Retrieve the information about the organizer by the object id
     * @param observer
     *      The observer to notify when the data is ready
     * @param organizerObjectId
     *      The object id of the organizer
     */
    public abstract void retrieveOrganizerInfo(Observer observer, String organizerObjectId);
}
