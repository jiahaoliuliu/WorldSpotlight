package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleCitiesListResponse;
import com.worldspotlightapp.android.model.City;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

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
     * The list of cities
     */
    private List<City> mCitiesList;

    /**
     * Empty constructor
     */
    public CityModuleObservable() {
        this.mCitiesList = new ArrayList<City>();

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
        if (!mCitiesList.isEmpty()) {
            if (observer != null) {
                CityModuleCitiesListResponse cityModuleCitiesListResponse = new CityModuleCitiesListResponse(
                        new ParseResponse.Builder(null).build(), mCitiesList);
                setChanged();
                notifyObservers(cityModuleCitiesListResponse);
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
                    mCitiesList.addAll(newCitiesList);

                    // if the query has reached the limit number of results
                    if (newCitiesList.size() == MAX_PARSE_QUERY_RESULT) {
                        requestCitiesList(mCitiesList.size(), this);
                    } else {
                        // Notify the observer when all the cities are retrieved
                        if (observer != null) {
                            CityModuleCitiesListResponse cityModuleCitiesListResponse = new CityModuleCitiesListResponse(
                                    parseResponse, mCitiesList);
                            setChanged();
                            notifyObservers(cityModuleCitiesListResponse);
                        }
                    }
                } else {
                    Log.e(TAG, "Error retrieving the list of cities from backend", parseResponse);
                    // Restart the list
                    mCitiesList = new ArrayList<City>();
                }
            }
        };

        requestCitiesList(mCitiesList.size(), findCitiesListCallback);
    }

    private void requestCitiesList(int initialPosition, FindCallback<City> findCitiesListCallback) {
        // Execute the query
        ParseQuery<City> query = ParseQuery.getQuery(City.class);
        query.setLimit(MAX_PARSE_QUERY_RESULT);
        query.setSkip(initialPosition);
        query.findInBackground(findCitiesListCallback);
    }

    @Override
    public void addNewCityIfNotExisted(City city) {
        if (!city.hasCity() || !city.hasCountry()) {
            Log.e(TAG, "The city name or the country is null");
            return;
        }

        // If the city already exists, not do anything
        if (mCitiesList.contains(city)) {
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
                }
            }
        });
    }

}
