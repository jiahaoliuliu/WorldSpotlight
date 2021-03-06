package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleCitiesSetResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleOrganizersListResponse;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.Organize;
import com.worldspotlightapp.android.model.Organizer;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observer;
import java.util.Set;

/**
 *
 * Module used specially to get the list of cities
 *
 * Created by jiahaoliuliu on 15/9/7.
 */
public class CityModuleObservable extends AbstractCityModuleObservable {

    private static final String TAG = "CityModuleObservable";

    private static final int MAX_PARSE_QUERY_RESULT = 500;

    /**
     * The set of cities. Each city is unique
     */
    private Set<City> mCitiesSet;

    /**
     * The set of cities that should be added when the city list is ready.
     * Each city is unique
     */
    private Set<City> mPendingCitySetToBeAdded;

    /**
     * Empty constructor
     */
    public CityModuleObservable() {
        this.mCitiesSet = new HashSet<City>();
        this.mPendingCitySetToBeAdded = new HashSet<City>();

        // Initialize the list of cities
        retrieveAllCitiesList(null);
    }

    @Override
    public void retrieveAllCitiesList(final Observer observer) {
        // Add observer if it is not null
        if (observer != null) {
            addObserver(observer);
        }

        // If the data already exists, return it to the observer
        // if observer is not null. Otherwise just finish
        if (!mCitiesSet.isEmpty()) {
            if (observer != null) {
                CityModuleCitiesSetResponse cityModuleCitiesSetResponse = new CityModuleCitiesSetResponse(
                        new ParseResponse.Builder(null).build(), mCitiesSet);
                setChanged();
                notifyObservers(cityModuleCitiesSetResponse);
            }

            return;
        }

        final FindCallback<City> findCitiesListCallback = new FindCallback<City>() {
            @Override
            public void done(List<City> newCitiesList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "List of cities retrieved from backend " + newCitiesList.size());

                    // Add the content to the list
                    mCitiesSet.addAll(newCitiesList);

                    // if the query has reached the limit number of results
                    if (newCitiesList.size() == MAX_PARSE_QUERY_RESULT) {
                        requestCitiesList(mCitiesSet.size(), this);
                    } else {
                        // Notify the observer when all the cities are retrieved
                        if (observer != null) {
                            CityModuleCitiesSetResponse cityModuleCitiesSetResponse = new CityModuleCitiesSetResponse(
                                    parseResponse, mCitiesSet);
                            setChanged();
                            notifyObservers(cityModuleCitiesSetResponse);
                        }

                        // Check if there is any city that should be added
                        // The city list shouldn't be empty. If so, this will end up in infinite loop
                        if (!mPendingCitySetToBeAdded.isEmpty()) {
                            for (City city : mPendingCitySetToBeAdded) {
                                addNewCityIfNotExisted(city);
                            }

                            // Clear the list
                            mPendingCitySetToBeAdded.clear();
                        }
                    }
                } else {
                    Log.e(TAG, "Error retrieving the list of cities from backend", parseResponse);
                    // Restart the list
                    mCitiesSet = new HashSet<City>();
                }
            }
        };

        requestCitiesList(mCitiesSet.size(), findCitiesListCallback);
    }

    private void requestCitiesList(int initialPosition, FindCallback<City> findCitiesListCallback) {
        // Execute the query
        ParseQuery<City> query = ParseQuery.getQuery(City.class);
        query.setLimit(MAX_PARSE_QUERY_RESULT);
        query.setSkip(initialPosition);
        query.findInBackground(findCitiesListCallback);
    }

    @Override
    public void addNewCityIfNotExisted(final City city) {
        if (!city.hasCity() || !city.hasCountry()) {
            Log.e(TAG, "The city name or the country is null");
            return;
        }

        // If the list of cities is empty, wait it to be updated
        if (mCitiesSet.isEmpty()) {
            mPendingCitySetToBeAdded.add(city);
            return;
        }

        // If the city already exists, not do anything
        if (mCitiesSet.contains(city)) {
            Log.v(TAG, "The city already exists in the backend. Not do anything");
            return;
        }

        // Save the city
        city.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                if (!parseResponse.isError()) {
                    Log.v(TAG, "City added to the backend correctly");
                } else {
                    Log.e(TAG, "Error adding city to the backend.", parseResponse);
                    mCitiesSet.remove(city);
                }
            }
        });
        // Since most of the time it goes ok and there is a short window time since the city
        // is saved, it is better add it and then, if something went wrong, remove it.
        mCitiesSet.add(city);
    }

    @Override
    public void retrieveAllOrganizersOfTheCity(final Observer observer, final String city, final String country) {
        if (city == null || country == null) {
            Log.e(TAG, "The city and the country cannot be null");
            return;
        }

        // Look for the city with object id
        City cityWithObjectId = null;
        if (mCitiesSet != null) {
            for (City existentCity : mCitiesSet) {
                if (city.equals(existentCity.getCity()) && country.equals(existentCity.getCountry())) {
                    cityWithObjectId = existentCity;
                    break;
                }
            }
        }

        // If the city with object id does not exists, try to retrieve it from the backend
        if (cityWithObjectId == null) {
            ParseQuery<City> cityParseQuery = ParseQuery.getQuery(City.class);
            cityParseQuery.whereEqualTo(City.PARSE_COLUMN_CITY, city);
            cityParseQuery.whereEqualTo(City.PARSE_COLUMN_COUNTRY, country);
            cityParseQuery.findInBackground(new FindCallback<City>() {
                @Override
                public void done(List<City> cityList, ParseException e) {
                    ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                    if (!parseResponse.isError()) {
                        if (cityList.isEmpty()) {
                            Log.e(TAG, "No city found with such data " + city + ", " + country);
                            return;
                        }

                        // Retrieve all the organizations using the first element of the list
                        retrieveAllOrganizersOfTheCity(observer, cityList.get(0));
                    } else {
                        Log.e(TAG, "Error searching the city in the backend", parseResponse);
                    }
                }
            });
        } else {
            retrieveAllOrganizersOfTheCity(observer, cityWithObjectId);
        }
    }

    /**
     * Retrieve all the organizers of a specific city
     * @param observer
     *      The observer to notify when the data is ready
     * @param city
     *      The city which the organizer is organizing some event. The object id of the city cannot be null
     */
    private void retrieveAllOrganizersOfTheCity(Observer observer, City city) {
        if (!city.hasObjectId()) {
            throw new IllegalArgumentException("You must pass the city with object id");
        }

        addObserver(observer);

        ParseQuery<Organize> organizerParseQuery = ParseQuery.getQuery(Organize.class);
        organizerParseQuery.whereEqualTo(Organize.PARSE_COLUMN_CITY, city);
        organizerParseQuery.include("organizer");
        organizerParseQuery.findInBackground(new FindCallback<Organize>() {
            @Override
            public void done(List<Organize> organizesList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                // Check if there is any error
                if (!parseResponse.isError()) {
                    Log.v(TAG, organizesList.size() + " organizations found");

                    List<Organizer> organizersList = new ArrayList<Organizer>();
                    // Get the organizer
                    for (Organize organize : organizesList) {
                        Log.v(TAG, organize.getOrganizer() + "");
                        organizersList.add(organize.getOrganizer());
                    }

                    Log.v(TAG, organizersList + "");
                    CityModuleOrganizersListResponse cityModuleOrganizersListResponse =
                            new CityModuleOrganizersListResponse(parseResponse, organizersList);

                    setChanged();
                    notifyObservers(cityModuleOrganizersListResponse);
                } else {
                    Log.e(TAG, "Error retrieving the list of organizers", parseResponse);
                    CityModuleOrganizersListResponse cityModuleOrganizersListResponse =
                            new CityModuleOrganizersListResponse(parseResponse, null);

                    setChanged();
                    notifyObservers(cityModuleOrganizersListResponse);
                }
            }
        });

    }
}
