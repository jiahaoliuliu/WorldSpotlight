package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import android.app.Activity;

import com.worldspotlightapp.android.maincontroller.AbstractBaseModuleObservable;
import com.worldspotlightapp.android.model.City;
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
    public abstract void retrieveAllCitiesList(Observer observer);

    /**
     * Add a new city if it does not exists in the backend before.
     *
     * Note this method does not return any data to the observer
     * @param city
     *      The city to be added
     */
    public abstract void addNewCityIfNotExisted(City city);

    /**
     * Retrieve all the organizers from a specific city
     * @param observer
     *      The observer to notify when the data is ready
     * @param city
     *      The city where the organizers has something organized
     * @param country
     *      The country where the city is
     */
    public abstract void retrieveAllOrganizersOfTheCity(Observer observer, String city, String country);
}
