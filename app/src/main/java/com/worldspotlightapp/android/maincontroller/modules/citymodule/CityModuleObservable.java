package com.worldspotlightapp.android.maincontroller.modules.citymodule;

import android.util.Log;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.worldspotlightapp.android.maincontroller.modules.ParseResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleCitiesListResponse;
import com.worldspotlightapp.android.maincontroller.modules.citymodule.response.CityModuleOrganizersListResponse;
import com.worldspotlightapp.android.model.City;
import com.worldspotlightapp.android.model.Organizer;

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

    private static final String ORGANIZE_RELATION_TABLE_NAME = "Organize";
    private static final String ORGANIZE_RELATION_TABLE_COLUMN_CITY = "city";

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
    public void addNewCityIfNotExisted(final City city) {
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
                    mCitiesList.add(city);
                } else {
                    Log.e(TAG, "Error adding city to the backend.", parseResponse);
                }
            }
        });
    }

    @Override
    public void retrieveAllOrganizersOfTheCity(final Observer observer, final String city, final String country) {
        if (city == null || country == null) {
            Log.e(TAG, "The city and the country cannot be null");
            return;
        }

        // Look for the city with object id
        City cityWithObjectId = null;
        if (mCitiesList != null) {
            for (City existentCity : mCitiesList) {
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
        if (!city.has(City.PARSE_COLUMN_OBJECT_ID)) {
            throw new IllegalArgumentException("You must pass the city with object id");
        }

        addObserver(observer);

        ParseQuery<Organizer> organizerParseQuery = ParseQuery.getQuery(ORGANIZE_RELATION_TABLE_NAME);
        organizerParseQuery.whereEqualTo(ORGANIZE_RELATION_TABLE_COLUMN_CITY, city);
        organizerParseQuery.findInBackground(new FindCallback<Organizer>() {
            @Override
            public void done(List<Organizer> organizersList, ParseException e) {
                ParseResponse parseResponse = new ParseResponse.Builder(e).build();
                // Check if there is any error
                if (!parseResponse.isError()) {
                    if (organizersList.isEmpty()) {
                        Log.e(TAG, "No organizer found for that city");
                    } else {
                        CityModuleOrganizersListResponse cityModuleOrganizersListResponse =
                                new CityModuleOrganizersListResponse(parseResponse, organizersList);

                        setChanged();
                        notifyObservers(cityModuleOrganizersListResponse);
                    }

                } else {
                    Log.e(TAG, "Error retrieving the list of organizers", parseResponse);
                }
            }
        });

    }
}
