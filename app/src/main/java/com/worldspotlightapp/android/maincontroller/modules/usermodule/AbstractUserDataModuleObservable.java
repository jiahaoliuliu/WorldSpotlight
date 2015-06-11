package com.worldspotlightapp.android.maincontroller.modules.usermodule;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;

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
}
