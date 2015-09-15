package com.worldspotlightapp.android.model;

import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.parse.ParseClassName;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiahaoliuliu on 15/9/7.
 */
@ParseClassName("City")
public class City extends ParseObject{

    private static final String TAG = "City";

    // City
    public static final String PARSE_COLUMN_CITY = "city";

    // Country
    public static final String PARSE_COLUMN_COUNTRY = "country";

    /**
     * Empty constructor required by parse
     */
    public City() {
        super();
    }

    public City(String city, String country) {
        this();
        setCity(city);
        setCountry(country);
    }

    // Object id
    public boolean hasObjectId() {
        return !TextUtils.isEmpty(getObjectId());
    }

    // City
    public boolean hasCity() {
        if (!containsKey(PARSE_COLUMN_CITY)) {
            return false;
        }

        return !TextUtils.isEmpty(getCity());
    }

    public String getCity() {
        return getString(PARSE_COLUMN_CITY);
    }

    private void setCity(String city) {
        if (city == null) {
            Log.e(TAG, "Trying to set the city when the city is null");
            return;
        }

        put(PARSE_COLUMN_CITY, city);
    }

    // Country
    public boolean hasCountry() {
        if (!containsKey(PARSE_COLUMN_COUNTRY)) {
            return false;
        }

        return !TextUtils.isEmpty(getCountry());
    }

    public String getCountry() {
        return getString(PARSE_COLUMN_COUNTRY);
    }

    public void setCountry(String country) {
        if (country == null) {
            Log.e(TAG, "Trying to set the country when the country is null");
            return;
        }

        put(PARSE_COLUMN_COUNTRY, country);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City anotherCity = (City) o;

        // Check if the object id exists.
        if (anotherCity.hasObjectId()) {
            return getObjectId().equals(anotherCity.getObjectId());
            // If the object id does not exists, check the video id
        } else {
            return getCity().equals(anotherCity.getCity()) &&
                getCountry().equals(anotherCity.getCountry());
        }
    }

    @Override
    public int hashCode() {
        int result = 31 * getCity().hashCode();
        result = 31 * result + (getCountry().hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "Video{" +
                "objectId='" + getObjectId() + "\'" +
                "city='" + getCity() + '\'' +
                "country='" + getCountry() + '\'' +
                '}';
    }
}
