package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import android.app.Activity;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;
import com.worldspotlightapp.android.model.UserData;

import java.util.Observer;
import java.util.UUID;

/**
 * This is the module which contains all the data about the user and method to update them
 * 
 * @author Jiahao Liu
 * 
 */
public abstract class AbstractCityModuleObservable extends AbstractBaseModuleObservable {

    /**
     * Retrieve the list of existence cities
     * @param observer
     *      The observer to notify when the data is ready.
     */
    public abstract void retrieveCitiesList(Observer observer);
}
