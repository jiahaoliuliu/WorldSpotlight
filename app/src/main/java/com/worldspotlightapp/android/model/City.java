package com.worldspotlightapp.android.model;

import com.parse.ParseClassName;
import com.parse.ParseObject;

/**
 * Created by jiahaoliuliu on 15/9/7.
 */
@ParseClassName("City")
public class City extends ParseObject{

    private static final String TAG = "City";

    // Object Id
    public static final String PARSE_COLUMN_OBJECT_ID = "objectId";

    // City
    public static final String PARSE_COLUMN_CITY = "city";

    // Country
    public static final String PARSE_COLUMN_COUNTRY = "country";

    // Organizers
    // TODO:

    /**
     * Empty constructor required by parse
     */
    public City() {
        super();
    }

    public String getCity() {
        return getString(PARSE_COLUMN_CITY);
    }

    public String getCountry() {
        return getString(PARSE_COLUMN_COUNTRY);
    }

    // TODO: Get the list of organizers

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        City anotherCity = (City) o;

        // Check if the object id exists.
        if (anotherCity.has(PARSE_COLUMN_OBJECT_ID)) {
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
                // TODO: Get the pointer to the organizers
                '}';
    }
}
